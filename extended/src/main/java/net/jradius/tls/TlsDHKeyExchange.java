package net.jradius.tls;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.agreement.DHBasicAgreement;
import org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator;
import org.bouncycastle.crypto.io.SignerInputStream;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.util.BigIntegers;

/**
 * TLS 1.0 DH key exchange.
 */
class TlsDHKeyExchange implements TlsKeyExchange
{
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);

    private final TlsProtocolHandler handler;
    private final CertificateVerifyer verifyer;
    private final Algorithm algorithm;
    private TlsSigner tlsSigner;

    private AsymmetricKeyParameter serverPublicKey = null;

    private DHPublicKeyParameters dhAgreeServerPublicKey = null;
    private AsymmetricCipherKeyPair dhAgreeClientKeyPair = null;

    TlsDHKeyExchange(TlsProtocolHandler handler, CertificateVerifyer verifyer, Algorithm keyExchange)
    {
        switch (keyExchange)
        {
            case KE_DH_RSA:
            case KE_DH_DSS:
                this.tlsSigner = null;
                break;
            case KE_DHE_RSA:
                this.tlsSigner = new TlsRSASigner();
                break;
            case KE_DHE_DSS:
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
        handler.failWithError(TlsProtocolHandler.AL_fatal, TlsProtocolHandler.AP_unexpected_message);
    }

    public void processServerCertificate(CertificateChain serverCertificate) throws IOException
    {
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

        // TODO Should the 'instanceof' tests be replaces with stricter checks on keyInfo.getAlgorithmId()?

        switch (this.algorithm)
        {
            case KE_DH_DSS:
                if (!(this.serverPublicKey instanceof DHPublicKeyParameters))
                {
                    handler.failWithError(TlsProtocolHandler.AL_fatal,
                        TlsProtocolHandler.AP_certificate_unknown);
                }
                // TODO The algorithm used to sign the certificate should be DSS.
//                x509Cert.getSignatureAlgorithm();
                this.dhAgreeServerPublicKey = validateDHPublicKey((DHPublicKeyParameters)this.serverPublicKey);
                break;
            case KE_DH_RSA:
                if (!(this.serverPublicKey instanceof DHPublicKeyParameters))
                {
                    handler.failWithError(TlsProtocolHandler.AL_fatal,
                        TlsProtocolHandler.AP_certificate_unknown);
                }
                // TODO The algorithm used to sign the certificate should be RSA.
//              x509Cert.getSignatureAlgorithm();
                this.dhAgreeServerPublicKey = validateDHPublicKey((DHPublicKeyParameters)this.serverPublicKey);
                break;
            case KE_DHE_RSA:
                if (!(this.serverPublicKey instanceof RSAKeyParameters))
                {
                    handler.failWithError(TlsProtocolHandler.AL_fatal,
                        TlsProtocolHandler.AP_certificate_unknown);
                }
                validateKeyUsage(x509Cert, KeyUsage.digitalSignature);
                break;
            case KE_DHE_DSS:
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
        if (tlsSigner != null)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_unexpected_message);
        }
    }

    public void processServerKeyExchange(InputStream is, SecurityParameters securityParameters)
        throws IOException
    {
        if (tlsSigner == null)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_unexpected_message);
        }

        InputStream sigIn = is;
        Signer signer = null;

        if (tlsSigner != null)
        {
            signer = initSigner(tlsSigner, securityParameters);
            sigIn = new SignerInputStream(is, signer);
        }

        byte[] pBytes = TlsUtils.readOpaque16(sigIn);
        byte[] gBytes = TlsUtils.readOpaque16(sigIn);
        byte[] YsBytes = TlsUtils.readOpaque16(sigIn);

        if (signer != null)
        {
            byte[] sigByte = TlsUtils.readOpaque16(is);

            if (!signer.verifySignature(sigByte))
            {
                handler.failWithError(TlsProtocolHandler.AL_fatal,
                    TlsProtocolHandler.AP_bad_certificate);
            }
        }

        BigInteger p = new BigInteger(1, pBytes);
        BigInteger g = new BigInteger(1, gBytes);
        BigInteger Ys = new BigInteger(1, YsBytes);

        this.dhAgreeServerPublicKey = validateDHPublicKey(new DHPublicKeyParameters(Ys,
            new DHParameters(p, g)));
    }

    public byte[] generateClientKeyExchange() throws IOException
    {
        // TODO RFC 2246 7.4.72
        /*
         * If the client certificate already contains a suitable Diffie-Hellman key, then
         * Yc is implicit and does not need to be sent again. In this case, the Client Key
         * Exchange message will be sent, but will be empty.
         */
//        return new byte[0];

        /*
         * Generate a keypair (using parameters from server key) and send the public value
         * to the server.
         */
        DHBasicKeyPairGenerator dhGen = new DHBasicKeyPairGenerator();
        dhGen.init(new DHKeyGenerationParameters(handler.getRandom(),
            dhAgreeServerPublicKey.getParameters()));
        this.dhAgreeClientKeyPair = dhGen.generateKeyPair();
        BigInteger Yc = ((DHPublicKeyParameters)dhAgreeClientKeyPair.getPublic()).getY();
        return BigIntegers.asUnsignedByteArray(Yc);
    }

    public byte[] generatePremasterSecret() throws IOException
    {
        /*
         * Diffie-Hellman basic key agreement
         */
        DHBasicAgreement dhAgree = new DHBasicAgreement();
        dhAgree.init(dhAgreeClientKeyPair.getPrivate());
        BigInteger agreement = dhAgree.calculateAgreement(dhAgreeServerPublicKey);
        return BigIntegers.asUnsignedByteArray(agreement);
    }

    private void validateKeyUsage(Certificate c, int keyUsageBits) throws IOException
    {
        Extensions exts = c.getTBSCertificate().getExtensions();
        if (exts != null)
        {
            KeyUsage ku = KeyUsage.fromExtensions(exts);
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

    private DHPublicKeyParameters validateDHPublicKey(DHPublicKeyParameters key) throws IOException
    {
        BigInteger Y = key.getY();
        DHParameters params = key.getParameters();
        BigInteger p = params.getP();
        BigInteger g = params.getG();

        if (!p.isProbablePrime(2))
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_illegal_parameter);
        }
        if (g.compareTo(TWO) < 0 || g.compareTo(p.subtract(TWO)) > 0)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_illegal_parameter);
        }
        if (Y.compareTo(TWO) < 0 || Y.compareTo(p.subtract(ONE)) > 0)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_illegal_parameter);
        }

        // TODO See RFC 2631 for more discussion of Diffie-Hellman validation

        return key;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
