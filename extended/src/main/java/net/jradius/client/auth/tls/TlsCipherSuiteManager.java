package net.jradius.client.auth.tls;

import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;

/**
 * A manager for ciphersuite. This class does manage all ciphersuites
 * which are used by MicroTLS.
 */
public class TlsCipherSuiteManager
{
    private static final int TLS_RSA_WITH_3DES_EDE_CBC_SHA = 0x000a;
    private static final int TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = 0x0016;
    private static final int TLS_RSA_WITH_AES_128_CBC_SHA = 0x002f;
    private static final int TLS_DHE_RSA_WITH_AES_128_CBC_SHA = 0x0033;
    private static final int TLS_RSA_WITH_AES_256_CBC_SHA = 0x0035;
    private static final int TLS_DHE_RSA_WITH_AES_256_CBC_SHA = 0x0039;


    protected static void writeCipherSuites(OutputStream os) throws IOException
    {
        TlsUtils.writeUint16(2 * 6, os);

        TlsUtils.writeUint16(TLS_DHE_RSA_WITH_AES_256_CBC_SHA, os);
        TlsUtils.writeUint16(TLS_DHE_RSA_WITH_AES_128_CBC_SHA, os);
        TlsUtils.writeUint16(TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA, os);

        TlsUtils.writeUint16(TLS_RSA_WITH_AES_256_CBC_SHA, os);
        TlsUtils.writeUint16(TLS_RSA_WITH_AES_128_CBC_SHA, os);
        TlsUtils.writeUint16(TLS_RSA_WITH_3DES_EDE_CBC_SHA, os);

    }

    protected static TlsCipherSuite getCipherSuite(int number, TlsProtocolHandler handler) throws IOException
    {
        switch (number)
        {
            case TLS_RSA_WITH_3DES_EDE_CBC_SHA:
                return new TlsBlockCipherCipherSuite(new CBCBlockCipher(new DESedeEngine()), new CBCBlockCipher(new DESedeEngine()), new SHA1Digest(), new SHA1Digest(), 24, TlsCipherSuite.KE_RSA);

            case TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
                return new TlsBlockCipherCipherSuite(new CBCBlockCipher(new DESedeEngine()), new CBCBlockCipher(new DESedeEngine()), new SHA1Digest(), new SHA1Digest(), 24, TlsCipherSuite.KE_DHE_RSA);

            case TLS_RSA_WITH_AES_128_CBC_SHA:
                return new TlsBlockCipherCipherSuite(new CBCBlockCipher(new AESFastEngine()), new CBCBlockCipher(new AESFastEngine()), new SHA1Digest(), new SHA1Digest(), 16, TlsCipherSuite.KE_RSA);

            case TLS_DHE_RSA_WITH_AES_128_CBC_SHA:
                return new TlsBlockCipherCipherSuite(new CBCBlockCipher(new AESFastEngine()), new CBCBlockCipher(new AESFastEngine()), new SHA1Digest(), new SHA1Digest(), 16, TlsCipherSuite.KE_DHE_RSA);

            case TLS_RSA_WITH_AES_256_CBC_SHA:
                return new TlsBlockCipherCipherSuite(new CBCBlockCipher(new AESFastEngine()), new CBCBlockCipher(new AESFastEngine()), new SHA1Digest(), new SHA1Digest(), 32, TlsCipherSuite.KE_RSA);

            case TLS_DHE_RSA_WITH_AES_256_CBC_SHA:
                return new TlsBlockCipherCipherSuite(new CBCBlockCipher(new AESFastEngine()), new CBCBlockCipher(new AESFastEngine()), new SHA1Digest(), new SHA1Digest(), 32, TlsCipherSuite.KE_DHE_RSA);

            default:
                handler.failWithError(TlsProtocolHandler.AL_fatal, TlsProtocolHandler.AP_handshake_failure);

                /*
                * Unreachable Code, failWithError will always throw an exception!
                */
                return null;


        }
    }

}
