package net.jradius.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.prng.ThreadedSeedGenerator;

/**
 * An implementation of all high level protocols in TLS 1.0.
 */
public class TlsProtocolHandler
{
//    private static final int EXT_RenegotiationInfo = 0xFF01;

    private static final int TLS_EMPTY_RENEGOTIATION_INFO_SCSV = 0x00FF;

    private static final short RL_CHANGE_CIPHER_SPEC = 20;
    private static final short RL_ALERT = 21;
    private static final short RL_HANDSHAKE = 22;
    private static final short RL_APPLICATION_DATA = 23;

    /*
     * hello_request(0), client_hello(1), server_hello(2), certificate(11),
     * server_key_exchange (12), certificate_request(13), server_hello_done(14),
     * certificate_verify(15), client_key_exchange(16), finished(20), (255)
     */
    private static final short HP_HELLO_REQUEST = 0;
    private static final short HP_CLIENT_HELLO = 1;
    private static final short HP_SERVER_HELLO = 2;
    private static final short HP_CERTIFICATE = 11;
    private static final short HP_SERVER_KEY_EXCHANGE = 12;
    private static final short HP_CERTIFICATE_REQUEST = 13;
    private static final short HP_SERVER_HELLO_DONE = 14;
    private static final short HP_CERTIFICATE_VERIFY = 15;
    private static final short HP_CLIENT_KEY_EXCHANGE = 16;
    private static final short HP_FINISHED = 20;

    /*
     * Our Connection states
     */
    private static final short CS_CLIENT_HELLO_SEND = 1;
    private static final short CS_SERVER_HELLO_RECEIVED = 2;
    private static final short CS_SERVER_CERTIFICATE_RECEIVED = 3;
    private static final short CS_SERVER_KEY_EXCHANGE_RECEIVED = 4;
    private static final short CS_CERTIFICATE_REQUEST_RECEIVED = 5;
    private static final short CS_SERVER_HELLO_DONE_RECEIVED = 6;
    private static final short CS_CLIENT_KEY_EXCHANGE_SEND = 7;
    private static final short CS_CERTIFICATE_VERIFY_SEND = 8;
    private static final short CS_CLIENT_CHANGE_CIPHER_SPEC_SEND = 9;
    private static final short CS_CLIENT_FINISHED_SEND = 10;
    private static final short CS_SERVER_CHANGE_CIPHER_SPEC_RECEIVED = 11;
    public static final short CS_DONE = 12;

    /*
     * AlertLevel enum (255)
     */
    // RFC 2246
    protected static final short AL_warning = 1;
    protected static final short AL_fatal = 2;

    /*
     * AlertDescription enum (255)
     */
    // RFC 2246
    protected static final short AP_close_notify = 0;
    protected static final short AP_unexpected_message = 10;
    protected static final short AP_bad_record_mac = 20;
    protected static final short AP_decryption_failed = 21;
    protected static final short AP_record_overflow = 22;
    protected static final short AP_decompression_failure = 30;
    protected static final short AP_handshake_failure = 40;
    protected static final short AP_bad_certificate = 42;
    protected static final short AP_unsupported_certificate = 43;
    protected static final short AP_certificate_revoked = 44;
    protected static final short AP_certificate_expired = 45;
    protected static final short AP_certificate_unknown = 46;
    protected static final short AP_illegal_parameter = 47;
    protected static final short AP_unknown_ca = 48;
    protected static final short AP_access_denied = 49;
    protected static final short AP_decode_error = 50;
    protected static final short AP_decrypt_error = 51;
    protected static final short AP_export_restriction = 60;
    protected static final short AP_protocol_version = 70;
    protected static final short AP_insufficient_security = 71;
    protected static final short AP_internal_error = 80;
    protected static final short AP_user_canceled = 90;
    protected static final short AP_no_renegotiation = 100;

    // RFC 4279
    protected static final short AP_unknown_psk_identity = 115;

    private static final byte[] emptybuf = new byte[0];

    private static final String TLS_ERROR_MESSAGE = "Internal TLS error, this could be an attack";

    /*
     * Queues for data from some protocols.
     */
    private final ByteQueue applicationDataQueue = new ByteQueue();
    private final ByteQueue changeCipherSpecQueue = new ByteQueue();
    private final ByteQueue alertQueue = new ByteQueue();
    private final ByteQueue handshakeQueue = new ByteQueue();

    /*
     * The Record Stream we use
     */
    private final RecordStream rs;
    private final SecureRandom random;

    private TlsInputStream tlsInputStream = null;
    private TlsOutputStream tlsOutputStream = null;

    private boolean closed = false;
    private boolean failedWithError = false;
    private boolean appDataReady = false;
    private boolean extendedClientHello;

    private SecurityParameters securityParameters = null;

    private TlsClient tlsClient = null;
    private int[] offeredCipherSuites = null;
    private TlsKeyExchange keyExchange = null;

    private short connection_state = 0;

    private KeyManager[] keyManagers = null;
    private TrustManager[] trustManagers = null;

    private boolean isSendCertificate = false;

    private static SecureRandom createSecureRandom()
    {
        /*
         * We use our threaded seed generator to generate a good random seed. If the user
         * has a better random seed, he should use the constructor with a SecureRandom.
         */
        ThreadedSeedGenerator tsg = new ThreadedSeedGenerator();
        SecureRandom random = new SecureRandom();

        /*
         * Hopefully, 20 bytes in fast mode are good enough.
         */
        random.setSeed(tsg.generateSeed(20, true));

        return random;
    }

    public TlsProtocolHandler(InputStream is, OutputStream os)
    {
        this(is, os, createSecureRandom());
    }

    public TlsProtocolHandler(InputStream is, OutputStream os, SecureRandom sr)
    {
        this.rs = new RecordStream(this, is, os);
        this.random = sr;
    }

    public TlsProtocolHandler()
    {
    	this.rs = new RecordStream(this);
    	this.random = createSecureRandom();
	}

	SecureRandom getRandom()
    {
        return random;
    }

	public void setSendCertificate(boolean b)
	{
		this.isSendCertificate = b;
	}

    protected void processData(short protocol, byte[] buf, int offset, int len) throws IOException
    {
        /*
         * Have a look at the protocol type, and add it to the correct queue.
         */
        switch (protocol)
        {
            case RL_CHANGE_CIPHER_SPEC:
                changeCipherSpecQueue.addData(buf, offset, len);
                processChangeCipherSpec();
                break;
            case RL_ALERT:
                alertQueue.addData(buf, offset, len);
                processAlert();
                break;
            case RL_HANDSHAKE:
                handshakeQueue.addData(buf, offset, len);
                processHandshake();
                break;
            case RL_APPLICATION_DATA:
                if (!appDataReady)
                {
                    this.failWithError(AL_fatal, AP_unexpected_message);
                }
                applicationDataQueue.addData(buf, offset, len);
                processApplicationData();
                break;
            default:
                /*
                 * Uh, we don't know this protocol.
                 *
                 * RFC2246 defines on page 13, that we should ignore this.
                 */
        }
    }

    private void processHandshake() throws IOException
    {
        boolean read;
        do
        {
            read = false;
            /*
             * We need the first 4 bytes, they contain type and length of the message.
             */
            if (handshakeQueue.size() >= 4)
            {
                byte[] beginning = new byte[4];
                handshakeQueue.read(beginning, 0, 4, 0);
                ByteArrayInputStream bis = new ByteArrayInputStream(beginning);
                short type = TlsUtils.readUint8(bis);
                int len = TlsUtils.readUint24(bis);

                /*
                 * Check if we have enough bytes in the buffer to read the full message.
                 */
                if (handshakeQueue.size() >= (len + 4))
                {
                    /*
                     * Read the message.
                     */
                    byte[] buf = new byte[len];
                    handshakeQueue.read(buf, 0, len, 4);
                    handshakeQueue.removeData(len + 4);

                    /*
                     * RFC 2246 7.4.9. "The value handshake_messages includes all
                     * handshake messages starting at client hello up to, but not
                     * including, this finished message. [..] Note: [Also,] Hello Request
                     * messages are omitted from handshake hashes."
                     */
                    switch (type)
                    {
                        case HP_HELLO_REQUEST:
                        case HP_FINISHED:
                            break;
                        default:
                            rs.updateHandshakeData(beginning, 0, 4);
                            rs.updateHandshakeData(buf, 0, len);
                            break;
                    }

                    /*
                     * Now, parse the message.
                     */
                    processHandshakeMessage(type, buf);
                    read = true;
                }
            }
        }
        while (read);
    }

    private void processHandshakeMessage(short type, byte[] buf) throws IOException
    {
        ByteArrayInputStream is = new ByteArrayInputStream(buf);

        switch (type)
        {
            case HP_CERTIFICATE:
            {
                switch (connection_state)
                {
                    case CS_SERVER_HELLO_RECEIVED:
                    {
                        // Parse the Certificate message and send to cipher suite

                        CertificateChain serverCertificate = CertificateChain.parse(is);

                        assertEmpty(is);

                        this.keyExchange.processServerCertificate(serverCertificate);

                        X509TrustManager trustManager = chooseTrustManager(trustManagers);

                        if (trustManager != null) {
                            try {
                                trustManager.checkServerTrusted(serverCertificate.toX509(),
                                                                keyExchange.getAlgorithm().getName());
                            } catch (CertificateException e) {
                                // If we encounter a certificate exception it
                                // is likely the server certificate chain is not trusted..
                                this.failWithError(AL_fatal, AP_handshake_failure);
                            }
                        }

                        break;
                    }
                    default:
                        this.failWithError(AL_fatal, AP_unexpected_message);
                }

                connection_state = CS_SERVER_CERTIFICATE_RECEIVED;
                break;
            }
            case HP_FINISHED:
                switch (connection_state)
                {
                    case CS_SERVER_CHANGE_CIPHER_SPEC_RECEIVED:
                        /*
                         * Read the checksum from the finished message, it has always 12
                         * bytes.
                         */
                        byte[] serverVerifyData = new byte[12];
                        TlsUtils.readFully(serverVerifyData, is);

                        assertEmpty(is);

                        /*
                         * Calculate our own checksum.
                         */
                        byte[] expectedServerVerifyData = TlsUtils.PRF(
                            securityParameters.masterSecret, "server finished",
                            rs.getCurrentHash(), 12);

                        /*
                         * Compare both checksums.
                         */
                        if (!Arrays.constantTimeAreEqual(expectedServerVerifyData, serverVerifyData))
                        {
                            /*
                             * Wrong checksum in the finished message.
                             */
                            this.failWithError(AL_fatal, AP_handshake_failure);
                        }

                        connection_state = CS_DONE;

                        /*
                         * We are now ready to receive application data.
                         */
                        this.appDataReady = true;
                        break;
                    default:
                        this.failWithError(AL_fatal, AP_unexpected_message);
                }
                break;
            case HP_SERVER_HELLO:
                switch (connection_state)
                {
                    case CS_CLIENT_HELLO_SEND:
                        /*
                         * Read the server hello message
                         */
                        TlsUtils.checkVersion(is, this);

                        /*
                         * Read the server random
                         */
                        securityParameters.serverRandom = new byte[32];
                        TlsUtils.readFully(securityParameters.serverRandom, is);

                        byte[] sessionID = TlsUtils.readOpaque8(is);
                        if (sessionID.length > 32)
                        {
                            this.failWithError(TlsProtocolHandler.AL_fatal,
                                TlsProtocolHandler.AP_illegal_parameter);
                        }

                        this.tlsClient.notifySessionID(sessionID);

                        /*
                         * Find out which ciphersuite the server has chosen and check that
                         * it was one of the offered ones.
                         */
                        int selectedCipherSuite = TlsUtils.readUint16(is);
                        if (!wasCipherSuiteOffered(selectedCipherSuite))
                        {
                            this.failWithError(TlsProtocolHandler.AL_fatal,
                                TlsProtocolHandler.AP_illegal_parameter);
                        }

                        this.tlsClient.notifySelectedCipherSuite(selectedCipherSuite);

                        /*
                         * We support only the null compression which means no
                         * compression.
                         */
                        short compressionMethod = TlsUtils.readUint8(is);
                        if (compressionMethod != 0)
                        {
                            this.failWithError(TlsProtocolHandler.AL_fatal,
                                TlsProtocolHandler.AP_illegal_parameter);
                        }

                        /*
                         * RFC4366 2.2 The extended server hello message format MAY be
                         * sent in place of the server hello message when the client has
                         * requested extended functionality via the extended client hello
                         * message specified in Section 2.1.
                         */
                        if (extendedClientHello)
                        {
                            // Integer -> byte[]
                            Hashtable serverExtensions = new Hashtable();

                            if (is.available() > 0)
                            {
                                // Process extensions from extended server hello
                                byte[] extBytes = TlsUtils.readOpaque16(is);

                                ByteArrayInputStream ext = new ByteArrayInputStream(extBytes);
                                while (ext.available() > 0)
                                {
                                    int extType = TlsUtils.readUint16(ext);
                                    byte[] extValue = TlsUtils.readOpaque16(ext);

                                    serverExtensions.put(new Integer(extType), extValue);
                                }
                            }

                            // TODO[RFC 5746] If renegotiation_info was sent in client hello, check here

                            tlsClient.processServerExtensions(serverExtensions);
                        }

                        assertEmpty(is);

                        this.keyExchange = tlsClient.createKeyExchange();

                        connection_state = CS_SERVER_HELLO_RECEIVED;
                        break;
                    default:
                        this.failWithError(AL_fatal, AP_unexpected_message);
                }
                break;
            case HP_SERVER_HELLO_DONE:
                switch (connection_state)
                {
                    case CS_SERVER_CERTIFICATE_RECEIVED:

                        // There was no server key exchange message; check it's OK
                        this.keyExchange.skipServerKeyExchange();

                        // NB: Fall through to next case label

                    case CS_SERVER_KEY_EXCHANGE_RECEIVED:
                    case CS_CERTIFICATE_REQUEST_RECEIVED:

                        assertEmpty(is);

                        boolean isClientCertificateRequested = (connection_state == CS_CERTIFICATE_REQUEST_RECEIVED || isSendCertificate);

                        connection_state = CS_SERVER_HELLO_DONE_RECEIVED;

                        if (isClientCertificateRequested)
                        {
                        	sendClientCertificate(tlsClient.getCertificate());
                        }

                        /*
                         * Send the client key exchange message, depending on the key
                         * exchange we are using in our ciphersuite.
                         */
                        sendClientKeyExchange(this.keyExchange.generateClientKeyExchange());

                        connection_state = CS_CLIENT_KEY_EXCHANGE_SEND;

                        if (isClientCertificateRequested)
                        {
                            byte[] clientCertificateSignature = tlsClient.generateCertificateSignature(rs.getCurrentHash());
                            if (clientCertificateSignature != null)
                            {
                                sendCertificateVerify(clientCertificateSignature);

                                connection_state = CS_CERTIFICATE_VERIFY_SEND;
                            }
                        }

                        /*
                         * Now, we send change cipher state
                         */
                        byte[] cmessage = new byte[1];
                        cmessage[0] = 1;
                        rs.writeMessage(RL_CHANGE_CIPHER_SPEC, cmessage, 0, cmessage.length);

                        connection_state = CS_CLIENT_CHANGE_CIPHER_SPEC_SEND;

                        /*
                         * Calculate the master_secret
                         */
                        byte[] pms = this.keyExchange.generatePremasterSecret();

                        securityParameters.masterSecret = TlsUtils.PRF(pms, "master secret",
                            TlsUtils.concat(securityParameters.clientRandom,
                                securityParameters.serverRandom), 48);

                        // TODO Is there a way to ensure the data is really overwritten?
                        /*
                         * RFC 2246 8.1. "The pre_master_secret should be deleted from
                         * memory once the master_secret has been computed."
                         */
                        Arrays.fill(pms, (byte)0);

                        /*
                         * Initialize our cipher suite
                         */
                        rs.clientCipherSpecDecided(tlsClient.createCipher(securityParameters));

                        /*
                         * Send our finished message.
                         */
                        byte[] clientVerifyData = TlsUtils.PRF(securityParameters.masterSecret,
                            "client finished", rs.getCurrentHash(), 12);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        TlsUtils.writeUint8(HP_FINISHED, bos);
                        TlsUtils.writeOpaque24(clientVerifyData, bos);
                        byte[] message = bos.toByteArray();

                        rs.writeMessage(RL_HANDSHAKE, message, 0, message.length);

                        this.connection_state = CS_CLIENT_FINISHED_SEND;
                        break;
                    default:
                        this.failWithError(AL_fatal, AP_handshake_failure);
                }
                break;
            case HP_SERVER_KEY_EXCHANGE:
            {
                switch (connection_state)
                {
                    case CS_SERVER_HELLO_RECEIVED:

                        // There was no server certificate message; check it's OK
                        this.keyExchange.skipServerCertificate();

                        // NB: Fall through to next case label

                    case CS_SERVER_CERTIFICATE_RECEIVED:

                        this.keyExchange.processServerKeyExchange(is, securityParameters);

                        assertEmpty(is);
                        break;

                    default:
                        this.failWithError(AL_fatal, AP_unexpected_message);
                }

                this.connection_state = CS_SERVER_KEY_EXCHANGE_RECEIVED;
                break;
            }
            case HP_CERTIFICATE_REQUEST:
            {
                switch (connection_state)
                {
                    case CS_SERVER_CERTIFICATE_RECEIVED:

                        // There was no server key exchange message; check it's OK
                        this.keyExchange.skipServerKeyExchange();

                        // NB: Fall through to next case label

                    case CS_SERVER_KEY_EXCHANGE_RECEIVED:
                    {
                        byte[] types = TlsUtils.readOpaque8(is);
                        byte[] authorities = TlsUtils.readOpaque16(is);

                        assertEmpty(is);

                        ArrayList authorityDNs = new ArrayList();

                        ByteArrayInputStream bis = new ByteArrayInputStream(authorities);
                        while (bis.available() > 0)
                        {
                            byte[] dnBytes = TlsUtils.readOpaque16(bis);
                            authorityDNs.add(X509Name.getInstance(ASN1Primitive.fromByteArray(dnBytes)));
                        }

                        this.tlsClient.processServerCertificateRequest(types, authorityDNs);

                        break;
                    }
                    default:
                        this.failWithError(AL_fatal, AP_unexpected_message);
                }

                this.connection_state = CS_CERTIFICATE_REQUEST_RECEIVED;
                break;
            }
            case HP_HELLO_REQUEST:
                /*
                 * RFC 2246 7.4.1.1 Hello request
                 * "This message will be ignored by the client if the client is currently
                 * negotiating a session. This message may be ignored by the client if it
                 * does not wish to renegotiate a session, or the client may, if it wishes,
                 * respond with a no_renegotiation alert."
                 */
                if (connection_state == CS_DONE)
                {
                    // Renegotiation not supported yet
                    sendAlert(AL_warning, AP_no_renegotiation);
                }
                break;
            case HP_CLIENT_KEY_EXCHANGE:
            case HP_CERTIFICATE_VERIFY:
            case HP_CLIENT_HELLO:
            default:
                // We do not support this!
                this.failWithError(AL_fatal, AP_unexpected_message);
                break;
        }
    }

    private void processApplicationData()
    {
        /*
         * There is nothing we need to do here.
         *
         * This function could be used for callbacks when application data arrives in the
         * future.
         */
    }

    private void processAlert() throws IOException
    {
        while (alertQueue.size() >= 2)
        {
            /*
             * An alert is always 2 bytes. Read the alert.
             */
            byte[] tmp = new byte[2];
            alertQueue.read(tmp, 0, 2, 0);
            alertQueue.removeData(2);
            short level = tmp[0];
            short description = tmp[1];
            if (level == AL_fatal)
            {
                /*
                 * This is a fatal error.
                 */
                this.failedWithError = true;
                this.closed = true;
                /*
                 * Now try to close the stream, ignore errors.
                 */
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {

                }
                throw new IOException(TLS_ERROR_MESSAGE);
            }
            else
            {
                /*
                 * This is just a warning.
                 */
                if (description == AP_close_notify)
                {
                    /*
                     * Close notify
                     */
                    this.failWithError(AL_warning, AP_close_notify);
                }
                /*
                 * If it is just a warning, we continue.
                 */
            }
        }
    }

    /**
     * This method is called, when a change cipher spec message is received.
     *
     * @throws IOException If the message has an invalid content or the handshake is not
     *             in the correct state.
     */
    private void processChangeCipherSpec() throws IOException
    {
        while (changeCipherSpecQueue.size() > 0)
        {
            /*
             * A change cipher spec message is only one byte with the value 1.
             */
            byte[] b = new byte[1];
            changeCipherSpecQueue.read(b, 0, 1, 0);
            changeCipherSpecQueue.removeData(1);
            if (b[0] != 1)
            {
                /*
                 * This should never happen.
                 */
                this.failWithError(AL_fatal, AP_unexpected_message);
            }

            /*
             * Check if we are in the correct connection state.
             */
            if (this.connection_state != CS_CLIENT_FINISHED_SEND)
            {
                this.failWithError(AL_fatal, AP_handshake_failure);
            }

            rs.serverClientSpecReceived();

            this.connection_state = CS_SERVER_CHANGE_CIPHER_SPEC_RECEIVED;
        }
    }

    private void sendClientCertificate(CertificateChain clientCert) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TlsUtils.writeUint8(HP_CERTIFICATE, bos);
        clientCert.encode(bos);
        byte[] message = bos.toByteArray();

        rs.writeMessage(RL_HANDSHAKE, message, 0, message.length);
    }

    private void sendClientKeyExchange(byte[] keData) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TlsUtils.writeUint8(HP_CLIENT_KEY_EXCHANGE, bos);
        if (keData == null)
        {
            TlsUtils.writeUint24(0, bos);
        }
        else
        {
            TlsUtils.writeUint24(keData.length + 2, bos);
            TlsUtils.writeOpaque16(keData, bos);
        }
        byte[] message = bos.toByteArray();

        rs.writeMessage(RL_HANDSHAKE, message, 0, message.length);
    }

    private void sendCertificateVerify(byte[] data) throws IOException
    {
        /*
         * Send signature of handshake messages so far to prove we are the owner of the
         * cert See RFC 2246 sections 4.7, 7.4.3 and 7.4.8
         */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TlsUtils.writeUint8(HP_CERTIFICATE_VERIFY, bos);
        TlsUtils.writeUint24(data.length + 2, bos);
        TlsUtils.writeOpaque16(data, bos);
        byte[] message = bos.toByteArray();

        rs.writeMessage(RL_HANDSHAKE, message, 0, message.length);
    }

    /**
     * Connects to the remote system.
     * @param is
     * @param out
     *
     * @param verifyer Will be used when a certificate is received to verify that this
     *            certificate is accepted by the client.
     * @throws IOException If handshake was not successful.
     */
    // TODO Deprecate
    public void connect(ByteArrayInputStream is, ByteArrayOutputStream out, CertificateVerifyer verifyer) throws IOException
    {
        this.connect(is, out, new DefaultTlsClient(verifyer));
    }

//    public void connect(CertificateVerifyer verifyer, Certificate clientCertificate,
//        AsymmetricKeyParameter clientPrivateKey) throws IOException
//    {
//        DefaultTlsClient client = new DefaultTlsClient(verifyer);
//        client.enableClientAuthentication(clientCertificate, clientPrivateKey);
//
//        this.connect(client);
//    }

    /**
     * Connects to the remote system using client authentication
     *
     * @param verifyer Will be used when a certificate is received to verify that this
     *            certificate is accepted by the client.
     * @param clientCertificate The client's certificate to be provided to the remote
     *            system
     * @param clientPrivateKey The client's private key for the certificate to
     *            authenticate to the remote system (RSA or DSA)
     * @throws IOException If handshake was not successful.
     */
    public // TODO Make public
    void connect(ByteArrayInputStream is, ByteArrayOutputStream out, TlsClient tlsClient) throws IOException
    {
        if (tlsClient == null)
        {
            throw new IllegalArgumentException("'tlsClient' cannot be null");
        }
        if (this.tlsClient != null)
        {
            throw new IllegalStateException("connect can only be called once");
        }

        rs.setInputStream(is);
        rs.setOutputStream(out);

        this.tlsClient = tlsClient;
        this.tlsClient.init(this);

        /*
         * Send Client hello
         *
         * First, generate some random data.
         */
        securityParameters = new SecurityParameters();
        securityParameters.clientRandom = new byte[32];
        random.nextBytes(securityParameters.clientRandom);
        TlsUtils.writeGMTUnixTime(securityParameters.clientRandom, 0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TlsUtils.writeVersion(os);
        os.write(securityParameters.clientRandom);

        /*
         * Length of Session id
         */
        TlsUtils.writeUint8((short)0, os);
        //TlsUtils.writeUint8((short)1, os);

        /*
         * Cipher suites
         */
        this.offeredCipherSuites = this.tlsClient.getCipherSuites();

        // Note: 1 extra slot for TLS_EMPTY_RENEGOTIATION_INFO_SCSV
        TlsUtils.writeUint16(2 * (offeredCipherSuites.length/* + 1*/), os);
        for (int i = 0; i < offeredCipherSuites.length; ++i)
        {
            TlsUtils.writeUint16(offeredCipherSuites[i], os);
        }

        // RFC 5746 3.3
        // Note: If renegotiation added, remove this (and extra slot above)
        //TlsUtils.writeUint16(TLS_EMPTY_RENEGOTIATION_INFO_SCSV, os);

        /*
         * Compression methods, just the null method.
         */
        byte[] compressionMethods = new byte[] { 0x00 };
        TlsUtils.writeOpaque8(compressionMethods, os);

        /*
         * Extensions
         */
        // Integer -> byte[]
        Hashtable clientExtensions = this.tlsClient.generateClientExtensions();

        // RFC 5746 3.4
        // Note: If renegotiation is implemented, need to use this instead of TLS_EMPTY_RENEGOTIATION_INFO_SCSV
//      {
//          if (clientExtensions == null)
//          {
//              clientExtensions = new Hashtable();
//          }
//
//          clientExtensions.put(EXT_RenegotiationInfo, createRenegotiationInfo(emptybuf));
//      }

        this.extendedClientHello = clientExtensions != null && !clientExtensions.isEmpty();

        if (extendedClientHello)
        {
            ByteArrayOutputStream ext = new ByteArrayOutputStream();

            Enumeration keys = clientExtensions.keys();
            while (keys.hasMoreElements())
            {
                Integer extType = (Integer)keys.nextElement();
                byte[] extValue = (byte[])clientExtensions.get(extType);

                TlsUtils.writeUint16(extType.intValue(), ext);
                TlsUtils.writeOpaque16(extValue, ext);
            }

            TlsUtils.writeOpaque16(ext.toByteArray(), os);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TlsUtils.writeUint8(HP_CLIENT_HELLO, bos);
        TlsUtils.writeUint24(os.size(), bos);
        bos.write(os.toByteArray());
        byte[] message = bos.toByteArray();
        rs.writeMessage(RL_HANDSHAKE, message, 0, message.length);
        connection_state = CS_CLIENT_HELLO_SEND;

        /*
         * We will now read data, until we have completed the handshake.
        while (connection_state != CS_DONE)
        {
            // TODO Should we send fatal alerts in the event of an exception
            // (see readApplicationData)
            rs.readData();
        }

        this.tlsInputStream = new TlsInputStream(this);
        this.tlsOutputStream = new TlsOutputStream(this);
         */
    }

    public void writeApplicationData(ByteArrayInputStream is, ByteArrayOutputStream os, byte[] b) throws IOException
    {
    	/*
    	 * We will now read data, until we have completed the handshake.
    	 */
    	rs.setInputStream(is);
    	rs.setOutputStream(os);
    	writeData(b, 0, b.length);
    	this.tlsInputStream = new TlsInputStream(this);
        this.tlsOutputStream = new TlsOutputStream(this);
    }

    public byte[] readApplicationData(ByteArrayInputStream is, ByteArrayOutputStream os) throws IOException
    {
    	/*
    	 * We will now read data, until we have completed the handshake.
    	 */
    	rs.setInputStream(is);
    	rs.setOutputStream(os);
    	return readApplicationData();
    }

    protected byte[] readApplicationData() throws IOException
    {
        while (rs.hasMore())
        {
            /*
             * We need to read some data.
             */
            if (this.failedWithError)
            {
                /*
                 * Something went terribly wrong, we should throw an IOException
                 */
                throw new IOException(TLS_ERROR_MESSAGE);
            }
            if (this.closed)
            {
                /*
                 * Connection has been closed, there is no more data to read.
                 */
                return null;
            }

            try
            {
                rs.readData();
            }
            catch (IOException e)
            {
                if (!this.closed)
                {
                    this.failWithError(AL_fatal, AP_internal_error);
                }
                throw e;
            }
            catch (RuntimeException e)
            {
            	e.printStackTrace();
                if (!this.closed)
                {
                    this.failWithError(AL_fatal, AP_internal_error);
                }
                throw e;
            }
        }
        int len = applicationDataQueue.size();
        byte[] b = new byte[len];
        applicationDataQueue.read(b, 0, len, 0);
        applicationDataQueue.removeData(len);
        return b;
    }


    public short updateConnectState(ByteArrayInputStream is, ByteArrayOutputStream os) throws IOException
    {
    	rs.setInputStream(is);
    	rs.setOutputStream(os);
    	while (connection_state != CS_DONE && rs.hasMore())
    	{
    		rs.readData();
    	}
    	return connection_state;
    }

    /**
     * Read data from the network. The method will return immediately, if there is still
     * some data left in the buffer, or block until some application data has been read
     * from the network.
     *
     * @param buf The buffer where the data will be copied to.
     * @param offset The position where the data will be placed in the buffer.
     * @param len The maximum number of bytes to read.
     * @return The number of bytes read.
     * @throws IOException If something goes wrong during reading data.
     */
    protected int readApplicationData(byte[] buf, int offset, int len) throws IOException
    {
        while (applicationDataQueue.size() == 0)
        {
            /*
             * We need to read some data.
             */
            if (this.closed)
            {
                if (this.failedWithError)
                {
                    /*
                     * Something went terribly wrong, we should throw an IOException
                     */
                    throw new IOException(TLS_ERROR_MESSAGE);
                }

                /*
                 * Connection has been closed, there is no more data to read.
                 */
                return -1;
            }

            try
            {
                rs.readData();
            }
            catch (IOException e)
            {
                if (!this.closed)
                {
                    this.failWithError(AL_fatal, AP_internal_error);
                }
                throw e;
            }
            catch (RuntimeException e)
            {
                if (!this.closed)
                {
                    this.failWithError(AL_fatal, AP_internal_error);
                }
                throw e;
            }
        }
        len = Math.min(len, applicationDataQueue.size());
        applicationDataQueue.read(buf, offset, len, 0);
        applicationDataQueue.removeData(len);
        return len;
    }

    /**
     * Send some application data to the remote system.
     * <p/>
     * The method will handle fragmentation internally.
     *
     * @param buf The buffer with the data.
     * @param offset The position in the buffer where the data is placed.
     * @param len The length of the data.
     * @throws IOException If something goes wrong during sending.
     */
    protected void writeData(byte[] buf, int offset, int len) throws IOException
    {
        if (this.closed)
        {
            if (this.failedWithError)
            {
                throw new IOException(TLS_ERROR_MESSAGE);
            }

            throw new IOException("Sorry, connection has been closed, you cannot write more data");
        }

        /*
         * Protect against known IV attack!
         *
         * DO NOT REMOVE THIS LINE, EXCEPT YOU KNOW EXACTLY WHAT YOU ARE DOING HERE.
         */
        rs.writeMessage(RL_APPLICATION_DATA, emptybuf, 0, 0);

        do
        {
            /*
             * We are only allowed to write fragments up to 2^14 bytes.
             */
            int toWrite = Math.min(len, 1 << 14);

            try
            {
                rs.writeMessage(RL_APPLICATION_DATA, buf, offset, toWrite);
            }
            catch (IOException e)
            {
                if (!closed)
                {
                    this.failWithError(AL_fatal, AP_internal_error);
                }
                throw e;
            }
            catch (RuntimeException e)
            {
                if (!closed)
                {
                    this.failWithError(AL_fatal, AP_internal_error);
                }
                throw e;
            }


            offset += toWrite;
            len -= toWrite;
        }
        while (len > 0);

    }

    /**
     * @return An OutputStream which can be used to send data.
     */
    public OutputStream getOutputStream()
    {
        return this.tlsOutputStream;
    }

    /**
     * @return An InputStream which can be used to read data.
     */
    public InputStream getInputStream()
    {
        return this.tlsInputStream;
    }

    /**
     * Terminate this connection with an alert.
     * <p/>
     * Can be used for normal closure too.
     *
     * @param alertLevel The level of the alert, an be AL_fatal or AL_warning.
     * @param alertDescription The exact alert message.
     * @throws IOException If alert was fatal.
     */
    protected void failWithError(short alertLevel, short alertDescription) throws IOException
    {
        /*
         * Check if the connection is still open.
         */
        if (!closed)
        {
            /*
             * Prepare the message
             */
            this.closed = true;

            if (alertLevel == AL_fatal)
            {
                /*
                 * This is a fatal message.
                 */
                this.failedWithError = true;
            }
            sendAlert(alertLevel, alertDescription);
            rs.close();
            if (alertLevel == AL_fatal)
            {
                throw new IOException(TLS_ERROR_MESSAGE);
            }
        }
        else
        {
            throw new IOException(TLS_ERROR_MESSAGE);
        }
    }

    private void sendAlert(short alertLevel, short alertDescription) throws IOException
    {
        byte[] error = new byte[2];
        error[0] = (byte)alertLevel;
        error[1] = (byte)alertDescription;

        rs.writeMessage(RL_ALERT, error, 0, 2);
    }

    /**
     * Closes this connection.
     *
     * @throws IOException If something goes wrong during closing.
     */
    public void close() throws IOException
    {
        if (!closed)
        {
            this.failWithError((short)1, (short)0);
        }
    }

    /**
     * Make sure the InputStream is now empty. Fail otherwise.
     *
     * @param is The InputStream to check.
     * @throws IOException If is is not empty.
     */
    protected void assertEmpty(ByteArrayInputStream is) throws IOException
    {
        if (is.available() > 0)
        {
            this.failWithError(AL_fatal, AP_decode_error);
        }
    }

    protected void flush() throws IOException
    {
        rs.flush();
    }

    private boolean wasCipherSuiteOffered(int cipherSuite)
    {
        for (int i = 0; i < offeredCipherSuites.length; ++i)
        {
            if (offeredCipherSuites[i] == cipherSuite)
            {
                return true;
            }
        }
        return false;
    }

	public void setKeyManagers(KeyManager[] keyManagers) {
		this.keyManagers = keyManagers;
	}

	public void setTrustManagers(TrustManager[] trustManagers) {
		this.trustManagers = trustManagers;
	}

	private X509TrustManager chooseTrustManager(TrustManager[] tm) {
        // We only use the first instance of X509TrustManager passed to us.
        for (int i = 0; tm != null && i < tm.length; i++) {
            if (tm[i] instanceof X509TrustManager) {
                return (X509TrustManager)tm[i];
            }
        }

        return null;
	}

//    private byte[] CreateRenegotiationInfo(byte[] renegotiated_connection) throws IOException
//    {
//        ByteArrayOutputStream buf = new ByteArrayOutputStream();
//        TlsUtils.writeOpaque8(renegotiated_connection, buf);
//        return buf.toByteArray();
//    }
}
