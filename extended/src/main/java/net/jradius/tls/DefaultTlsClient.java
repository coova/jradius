package net.jradius.tls;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;

import net.jradius.tls.TlsKeyExchange.Algorithm;

public class DefaultTlsClient implements TlsClient
{
    // TODO Add runtime support for this check?
    /*
     * RFC 2246 9. In the absence of an application profile standard specifying otherwise,
     * a TLS compliant application MUST implement the cipher suite
     * TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA.
     */
    private static final int TLS_RSA_WITH_3DES_EDE_CBC_SHA = 0x000A;
    private static final int TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA = 0x000D;
    private static final int TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA = 0x0010;
    private static final int TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = 0x0013;
    private static final int TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = 0x0016;

    // RFC 3268
    private static final int TLS_RSA_WITH_AES_128_CBC_SHA = 0x002F;
    private static final int TLS_DH_DSS_WITH_AES_128_CBC_SHA = 0x0030;
    private static final int TLS_DH_RSA_WITH_AES_128_CBC_SHA = 0x0031;
    private static final int TLS_DHE_DSS_WITH_AES_128_CBC_SHA = 0x0032;
    private static final int TLS_DHE_RSA_WITH_AES_128_CBC_SHA = 0x0033;
    private static final int TLS_RSA_WITH_AES_256_CBC_SHA = 0x0035;
    private static final int TLS_DH_DSS_WITH_AES_256_CBC_SHA = 0x0036;
    private static final int TLS_DH_RSA_WITH_AES_256_CBC_SHA = 0x0037;
    private static final int TLS_DHE_DSS_WITH_AES_256_CBC_SHA = 0x0038;
    private static final int TLS_DHE_RSA_WITH_AES_256_CBC_SHA = 0x0039;

    // RFC 4279
//    private static final int TLS_PSK_WITH_3DES_EDE_CBC_SHA = 0x008B;
//    private static final int TLS_PSK_WITH_AES_128_CBC_SHA = 0x008C;
//    private static final int TLS_PSK_WITH_AES_256_CBC_SHA = 0x008D;
//    private static final int TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA = 0x008F;
//    private static final int TLS_DHE_PSK_WITH_AES_128_CBC_SHA = 0x0090;
//    private static final int TLS_DHE_PSK_WITH_AES_256_CBC_SHA = 0x0091;
//    private static final int TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA = 0x0093;
//    private static final int TLS_RSA_PSK_WITH_AES_128_CBC_SHA = 0x0094;
//    private static final int TLS_RSA_PSK_WITH_AES_256_CBC_SHA = 0x0095;

    // RFC 5054
    private static final int TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA = 0xC01A;
    private static final int TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA = 0xC01B;
    private static final int TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA = 0xC01C;
    private static final int TLS_SRP_SHA_WITH_AES_128_CBC_SHA = 0xC01D;
    private static final int TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA = 0xC01E;
    private static final int TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA = 0xC01F;
    private static final int TLS_SRP_SHA_WITH_AES_256_CBC_SHA = 0xC020;
    private static final int TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA = 0xC021;
    private static final int TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA = 0xC022;

    private final CertificateVerifyer verifyer;

    private TlsProtocolHandler handler;

    // (Optional) details for client-side authentication
    private CertificateChain clientCert = new CertificateChain(new Certificate[0]);
    private AsymmetricKeyParameter clientPrivateKey = null;
    private TlsSigner clientSigner = null;

    private int selectedCipherSuite;

    public DefaultTlsClient(CertificateVerifyer verifyer)
    {
        this.verifyer = verifyer;
    }

    public void enableClientAuthentication(CertificateChain clientCertificate,
        AsymmetricKeyParameter clientPrivateKey)
    {
        if (clientCertificate == null)
        {
            throw new IllegalArgumentException("'clientCertificate' cannot be null");
        }
        if (clientCertificate.certs.length == 0)
        {
            throw new IllegalArgumentException("'clientCertificate' cannot be empty");
        }
        if (clientPrivateKey == null)
        {
            throw new IllegalArgumentException("'clientPrivateKey' cannot be null");
        }
        if (!clientPrivateKey.isPrivate())
        {
            throw new IllegalArgumentException("'clientPrivateKey' must be private");
        }

        if (clientPrivateKey instanceof RSAKeyParameters)
        {
            clientSigner = new TlsRSASigner();
        }
        else if (clientPrivateKey instanceof DSAPrivateKeyParameters)
        {
            clientSigner = new TlsDSSSigner();
        }
        else
        {
            throw new IllegalArgumentException("'clientPrivateKey' type not supported: "
                + clientPrivateKey.getClass().getName());
        }

        this.clientCert = clientCertificate;
        this.clientPrivateKey = clientPrivateKey;
    }

    public void init(TlsProtocolHandler handler)
    {
        this.handler = handler;
    }

    public int[] getCipherSuites()
    {
        return new int[] {
            TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
            TLS_DHE_DSS_WITH_AES_256_CBC_SHA,
            TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
            TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
            TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA,
            TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA,
            TLS_RSA_WITH_AES_256_CBC_SHA,
            TLS_RSA_WITH_AES_128_CBC_SHA,
            TLS_RSA_WITH_3DES_EDE_CBC_SHA,

//            TLS_DH_RSA_WITH_AES_256_CBC_SHA,
//            TLS_DH_DSS_WITH_AES_256_CBC_SHA,
//            TLS_DH_RSA_WITH_AES_128_CBC_SHA,
//            TLS_DH_DSS_WITH_AES_128_CBC_SHA,
//            TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA,
//            TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA,

//            TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA,
//            TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA,
//            TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA,
//            TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA,
//            TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA,
//            TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA,
//            TLS_SRP_SHA_WITH_AES_256_CBC_SHA,
//            TLS_SRP_SHA_WITH_AES_128_CBC_SHA,
//            TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA,
        };
    }

    public Hashtable generateClientExtensions()
    {
        // TODO[SRP]
//        Hashtable clientExtensions = new Hashtable();
//        ByteArrayOutputStream srpData = new ByteArrayOutputStream();
//        TlsUtils.writeOpaque8(SRP_identity, srpData);
//
//        // TODO[SRP] RFC5054 2.8.1: ExtensionType.srp = 12
//        clientExtensions.put(Integer.valueOf(12), srpData.toByteArray());
//        return clientExtensions;
        return null;
    }

    public void notifySessionID(byte[] sessionID)
    {
        // Currently ignored
    }

    public void notifySelectedCipherSuite(int selectedCipherSuite)
    {
        this.selectedCipherSuite = selectedCipherSuite;
    }

    public void processServerExtensions(Hashtable serverExtensions)
    {
        // TODO Validate/process serverExtensions (via client?)
        // TODO[SRP]
    }

    public TlsKeyExchange createKeyExchange() throws IOException
    {
        switch (selectedCipherSuite)
        {
            case TLS_RSA_WITH_3DES_EDE_CBC_SHA:
            case TLS_RSA_WITH_AES_128_CBC_SHA:
            case TLS_RSA_WITH_AES_256_CBC_SHA:
                return createRSAKeyExchange();

            case TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA:
            case TLS_DH_DSS_WITH_AES_128_CBC_SHA:
            case TLS_DH_DSS_WITH_AES_256_CBC_SHA:
                return createDHKeyExchange(TlsKeyExchange.Algorithm.KE_DH_DSS);

            case TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA:
            case TLS_DH_RSA_WITH_AES_128_CBC_SHA:
            case TLS_DH_RSA_WITH_AES_256_CBC_SHA:
                return createDHKeyExchange(TlsKeyExchange.Algorithm.KE_DH_RSA);

            case TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA:
            case TLS_DHE_DSS_WITH_AES_128_CBC_SHA:
            case TLS_DHE_DSS_WITH_AES_256_CBC_SHA:
                return createDHKeyExchange(TlsKeyExchange.Algorithm.KE_DHE_DSS);

            case TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
            case TLS_DHE_RSA_WITH_AES_128_CBC_SHA:
            case TLS_DHE_RSA_WITH_AES_256_CBC_SHA:
                return createDHKeyExchange(TlsKeyExchange.Algorithm.KE_DHE_RSA);

            case TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA:
            case TLS_SRP_SHA_WITH_AES_128_CBC_SHA:
            case TLS_SRP_SHA_WITH_AES_256_CBC_SHA:
                return createSRPExchange(TlsKeyExchange.Algorithm.KE_SRP);

            case TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA:
            case TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA:
            case TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA:
                return createSRPExchange(TlsKeyExchange.Algorithm.KE_SRP_RSA);

            case TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA:
            case TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA:
            case TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA:
                return createSRPExchange(TlsKeyExchange.Algorithm.KE_SRP_DSS);

            default:
                /*
                 * Note: internal error here; the TlsProtocolHandler verifies that the
                 * server-selected cipher suite was in the list of client-offered cipher
                 * suites, so if we now can't produce an implementation, we shouldn't have
                 * offered it!
                 */
                handler.failWithError(TlsProtocolHandler.AL_fatal,
                    TlsProtocolHandler.AP_internal_error);
                return null; // Unreachable!
        }
    }

    public void processServerCertificateRequest(byte[] certificateTypes, List certificateAuthorities)
    {
        // TODO There shouldn't be a certificate request for SRP

        // TODO Use provided info to choose a certificate in getCertificate()
    }

    public byte[] generateCertificateSignature(byte[] md5andsha1) throws IOException
    {
        if (clientSigner == null)
        {
            return null;
        }

        try
        {
            return clientSigner.calculateRawSignature(clientPrivateKey, md5andsha1);
        }
        catch (CryptoException e)
        {
            handler.failWithError(TlsProtocolHandler.AL_fatal, TlsProtocolHandler.AP_internal_error);
            return null;
        }
    }

    public CertificateChain getCertificate()
    {
        return clientCert;
    }

    public TlsCipher createCipher(SecurityParameters securityParameters) throws IOException
    {
        switch (selectedCipherSuite)
        {
            case TLS_RSA_WITH_3DES_EDE_CBC_SHA:
            case TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA:
            case TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA:
            case TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA:
            case TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
            case TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA:
            case TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA:
            case TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA:
                return createDESedeCipher(24, securityParameters);

            case TLS_RSA_WITH_AES_128_CBC_SHA:
            case TLS_DH_DSS_WITH_AES_128_CBC_SHA:
            case TLS_DH_RSA_WITH_AES_128_CBC_SHA:
            case TLS_DHE_DSS_WITH_AES_128_CBC_SHA:
            case TLS_DHE_RSA_WITH_AES_128_CBC_SHA:
            case TLS_SRP_SHA_WITH_AES_128_CBC_SHA:
            case TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA:
            case TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA:
                return createAESCipher(16, securityParameters);

            case TLS_RSA_WITH_AES_256_CBC_SHA:
            case TLS_DH_DSS_WITH_AES_256_CBC_SHA:
            case TLS_DH_RSA_WITH_AES_256_CBC_SHA:
            case TLS_DHE_DSS_WITH_AES_256_CBC_SHA:
            case TLS_DHE_RSA_WITH_AES_256_CBC_SHA:
            case TLS_SRP_SHA_WITH_AES_256_CBC_SHA:
            case TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA:
            case TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA:
                return createAESCipher(32, securityParameters);

            default:
                /*
                 * Note: internal error here; the TlsProtocolHandler verifies that the
                 * server-selected cipher suite was in the list of client-offered cipher
                 * suites, so if we now can't produce an implementation, we shouldn't have
                 * offered it!
                 */
                handler.failWithError(TlsProtocolHandler.AL_fatal,
                    TlsProtocolHandler.AP_internal_error);
                return null; // Unreachable!
        }
    }

    private TlsKeyExchange createDHKeyExchange(Algorithm keyExchange)
    {
        return new TlsDHKeyExchange(handler, verifyer, keyExchange);
    }

    private TlsKeyExchange createRSAKeyExchange()
    {
        return new TlsRSAKeyExchange(handler, verifyer);
    }

    private TlsKeyExchange createSRPExchange(Algorithm keyExchange)
    {
        return new TlsSRPKeyExchange(handler, verifyer, keyExchange);
    }

    private TlsCipher createAESCipher(int cipherKeySize, SecurityParameters securityParameters)
    {
        return new TlsBlockCipher(handler, createAESBlockCipher(), createAESBlockCipher(),
            new SHA1Digest(), new SHA1Digest(), cipherKeySize, securityParameters);
    }

    private TlsCipher createDESedeCipher(int cipherKeySize, SecurityParameters securityParameters)
    {
        return new TlsBlockCipher(handler, createDESedeBlockCipher(), createDESedeBlockCipher(),
            new SHA1Digest(), new SHA1Digest(), cipherKeySize, securityParameters);
    }

    private static BlockCipher createAESBlockCipher()
    {
        return new CBCBlockCipher(new AESFastEngine());
    }

    private static BlockCipher createDESedeBlockCipher()
    {
        return new CBCBlockCipher(new DESedeEngine());
    }
}
