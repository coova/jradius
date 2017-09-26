package net.jradius.tls;

import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;

/**
 * TLS 1.0 RSA key exchange.
 */
class TlsRSAKeyExchange implements TlsKeyExchange
{
    private final TlsProtocolHandler handler;
    private final CertificateVerifyer verifyer;

    private AsymmetricKeyParameter serverPublicKey = null;

    private RSAKeyParameters rsaServerPublicKey = null;

    private byte[] premasterSecret;

    TlsRSAKeyExchange(TlsProtocolHandler handler, CertificateVerifyer verifyer)
    {
        this.handler = handler;
        this.verifyer = verifyer;
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

        if (!(this.serverPublicKey instanceof RSAKeyParameters))
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_certificate_unknown);
        }
        validateKeyUsage(x509Cert, KeyUsage.keyEncipherment);
        this.rsaServerPublicKey = validateRSAPublicKey((RSAKeyParameters)this.serverPublicKey);

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
        // OK
    }

    public void processServerKeyExchange(InputStream is, SecurityParameters securityParameters)
        throws IOException
    {
        handler.failWithError(TlsProtocolHandler.AL_fatal, TlsProtocolHandler.AP_unexpected_message);
    }

    public byte[] generateClientKeyExchange() throws IOException
    {
        /*
         * Choose a PremasterSecret and send it encrypted to the server
         */
        premasterSecret = new byte[48];
        handler.getRandom().nextBytes(premasterSecret);
        TlsUtils.writeVersion(premasterSecret, 0);

        PKCS1Encoding encoding = new PKCS1Encoding(new RSABlindedEngine());
        encoding.init(true, new ParametersWithRandom(this.rsaServerPublicKey, handler.getRandom()));

        try
        {
            return encoding.processBlock(premasterSecret, 0, premasterSecret.length);
        }
        catch (InvalidCipherTextException e)
        {
            /*
             * This should never happen, only during decryption.
             */
            handler.failWithError(TlsProtocolHandler.AL_fatal, TlsProtocolHandler.AP_internal_error);
            return null; // Unreachable!
        }
    }

    public byte[] generatePremasterSecret() throws IOException
    {
        byte[] tmp = this.premasterSecret;
        this.premasterSecret = null;
        return tmp;
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

//    private void processRSAServerKeyExchange(InputStream is, Signer signer) throws IOException
//    {
//        InputStream sigIn = is;
//        if (signer != null)
//        {
//            sigIn = new SignerInputStream(is, signer);
//        }
//
//        byte[] modulusBytes = TlsUtils.readOpaque16(sigIn);
//        byte[] exponentBytes = TlsUtils.readOpaque16(sigIn);
//
//        if (signer != null)
//        {
//            byte[] sigByte = TlsUtils.readOpaque16(is);
//
//            if (!signer.verifySignature(sigByte))
//            {
//                handler.failWithError(TlsProtocolHandler.AL_fatal,
//                    TlsProtocolHandler.AP_bad_certificate);
//            }
//        }
//
//        BigInteger modulus = new BigInteger(1, modulusBytes);
//        BigInteger exponent = new BigInteger(1, exponentBytes);
//
//        this.rsaServerPublicKey = validateRSAPublicKey(new RSAKeyParameters(false, modulus,
//            exponent));
//    }

    private RSAKeyParameters validateRSAPublicKey(RSAKeyParameters key) throws IOException
    {
        // TODO What is the minimum bit length required?
//        key.getModulus().bitLength();

        if (!key.getExponent().isProbablePrime(2))
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal,
                TlsProtocolHandler.AP_illegal_parameter);
        }

        return key;
    }

    public Algorithm getAlgorithm() {
        return Algorithm.KE_RSA;
    }
}
