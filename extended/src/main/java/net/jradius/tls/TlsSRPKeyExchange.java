package net.jradius.tls;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.agreement.srp.SRP6Client;
import org.bouncycastle.crypto.agreement.srp.SRP6Util;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.io.SignerInputStream;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.util.BigIntegers;

/**
 * TLS 1.1 SRP key exchange.
 */
class TlsSRPKeyExchange implements TlsKeyExchange
{
    private final TlsProtocolHandler handler;
    private final CertificateVerifyer verifyer;
    private final Algorithm algorithm;
    private TlsSigner tlsSigner;

    private AsymmetricKeyParameter serverPublicKey = null;

    // TODO Need a way of providing these
    private final byte[] SRP_identity = null;
    private final byte[] SRP_password = null;

    private byte[] s = null;
    private BigInteger B = null;
    private final SRP6Client srpClient = new SRP6Client();

    TlsSRPKeyExchange(TlsProtocolHandler handler, CertificateVerifyer verifyer, Algorithm keyExchange)
    {
        switch (keyExchange)
        {
            case KE_SRP:
                this.tlsSigner = null;
                break;
            case KE_SRP_RSA:
                this.tlsSigner = new TlsRSASigner();
                break;
            case KE_SRP_DSS:
                this.tlsSigner = new TlsDSSSigner();
                break;
            default:
                throw new IllegalArgumentException("unsupported key exchange algorithm");
        }

        this.handler = handler;
        this.verifyer = verifyer;
        this.algorithm = keyExchange;
    }

    public void skipServerCertificate() throws IOException
    {
        if (tlsSigner != null)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_unexpected_message);
        }
    }

    public void processServerCertificate(CertificateChain serverCertificate) throws IOException
    {
        if (tlsSigner == null)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_unexpected_message);
        }

        Certificate x509Cert = serverCertificate.certs[0];
        SubjectPublicKeyInfo keyInfo = x509Cert.getSubjectPublicKeyInfo();

        try
        {
            this.serverPublicKey = PublicKeyFactory.createKey(keyInfo);
        }
        catch (RuntimeException e)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_unsupported_certificate);
        }

        // Sanity check the PublicKeyFactory
        if (this.serverPublicKey.isPrivate())
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal, TlsProtocolHandler.AP_internal_error);
        }

        // TODO
        /*
         * Perform various checks per RFC2246 7.4.2: "Unless otherwise specified, the
         * signing algorithm for the certificate must be the same as the algorithm for the
         * certificate key."
         */
        switch (this.algorithm)
        {
            case KE_SRP_RSA:
                if (!(this.serverPublicKey instanceof RSAKeyParameters))
                {
                    handler.failWithError(TlsProtocolHandler.AL_fatal,
                        TlsProtocolHandler.AP_certificate_unknown);
                }
                validateKeyUsage(x509Cert, KeyUsage.digitalSignature);
                break;
            case KE_SRP_DSS:
                if (!(this.serverPublicKey instanceof DSAPublicKeyParameters))
                {
                    handler.failWithError(TlsProtocolHandler.AL_fatal,
                        TlsProtocolHandler.AP_certificate_unknown);
                }
                break;
            default:
                handler.failWithError(TlsProtocolHandler.AL_fatal,
                    TlsProtocolHandler.AP_unsupported_certificate);
        }

        /*
         * Verify them.
         */
        if (!this.verifyer.isValid(serverCertificate.getCerts()))
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal, TlsProtocolHandler.AP_user_canceled);
        }
    }

    public void skipServerKeyExchange() throws IOException
    {
        handler.failWithError(TlsProtocolHandler.AL_fatal, TlsProtocolHandler.AP_unexpected_message);
    }

    public void processServerKeyExchange(InputStream is, SecurityParameters securityParameters)
        throws IOException
    {
        InputStream sigIn = is;
        Signer signer = null;

        if (tlsSigner != null)
        {
            signer = initSigner(tlsSigner, securityParameters);
            sigIn = new SignerInputStream(is, signer);
        }

        byte[] NBytes = TlsUtils.readOpaque16(sigIn);
        byte[] gBytes = TlsUtils.readOpaque16(sigIn);
        byte[] sBytes = TlsUtils.readOpaque8(sigIn);
        byte[] BBytes = TlsUtils.readOpaque16(sigIn);

        if (signer != null)
        {
            byte[] sigByte = TlsUtils.readOpaque16(is);

            if (!signer.verifySignature(sigByte))
            {
                handler.failWithError(TlsProtocolHandler.AL_fatal,
                    TlsProtocolHandler.AP_bad_certificate);
            }
        }

        BigInteger N = new BigInteger(1, NBytes);
        BigInteger g = new BigInteger(1, gBytes);

        // TODO Validate group parameters (see RFC 5054)
//        handler.failWithError(TlsProtocolHandler.AL_fatal,
//            TlsProtocolHandler.AP_insufficient_security);

        this.s = sBytes;

        /*
         * RFC 5054 2.5.3: The client MUST abort the handshake with an "illegal_parameter"
         * alert if B % N = 0.
         */
        try
        {
            this.B = SRP6Util.validatePublicValue(N, new BigInteger(1, BBytes));
        }
        catch (CryptoException e)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_illegal_parameter);
        }

        this.srpClient.init(N, g, new SHA1Digest(), handler.getRandom());
    }

    public byte[] generateClientKeyExchange() throws IOException
    {
        return BigIntegers.asUnsignedByteArray(srpClient.generateClientCredentials(s,
            this.SRP_identity, this.SRP_password));
    }

    public byte[] generatePremasterSecret() throws IOException
    {
        try
        {
            // TODO Check if this needs to be a fixed size
            return BigIntegers.asUnsignedByteArray(srpClient.calculateSecret(B));
        }
        catch (CryptoException e)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_illegal_parameter);
            return null; // Unreachable!
        }
    }

    private void validateKeyUsage(Certificate c, int keyUsageBits) throws IOException
    {
        Extensions exts = c.getTBSCertificate().getExtensions();
        if (exts != null)
        {
            KeyUsage ku = KeyUsage.getInstance(exts);
            int bits = ku.getBytes()[0] & 0xff;
            if ((bits & keyUsageBits) != keyUsageBits)
            {
                handler.failWithError(TlsProtocolHandler.AL_fatal,
                    TlsProtocolHandler.AP_certificate_unknown);
            }
        }
    }

    private Signer initSigner(TlsSigner tlsSigner, SecurityParameters securityParameters)
    {
        Signer signer = tlsSigner.createVerifyer(this.serverPublicKey);
        signer.update(securityParameters.clientRandom, 0, securityParameters.clientRandom.length);
        signer.update(securityParameters.serverRandom, 0, securityParameters.serverRandom.length);
        return signer;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
