package net.jradius.client.auth.tls;

import java.io.IOException;
import java.io.InputStream;

/**
 * A generic interface for key exchange implementations in TLS 1.0.
 */
interface TlsKeyExchange
{
    static final short KE_RSA = 1;
//    static final short KE_RSA_EXPORT = 2;
    static final short KE_DHE_DSS = 3;
//    static final short KE_DHE_DSS_EXPORT = 4;
    static final short KE_DHE_RSA = 5;
//    static final short KE_DHE_RSA_EXPORT = 6;
    static final short KE_DH_DSS = 7;
    static final short KE_DH_RSA = 8;
//    static final short KE_DH_anon = 9;
    static final short KE_SRP = 10;
    static final short KE_SRP_DSS = 11;
    static final short KE_SRP_RSA = 12;

    void skipServerCertificate() throws IOException;

    void processServerCertificate(Certificate serverCertificate) throws IOException;

    void skipServerKeyExchange() throws IOException;

    void processServerKeyExchange(InputStream is, SecurityParameters securityParameters)
        throws IOException;

    byte[] generateClientKeyExchange() throws IOException;

    byte[] generatePremasterSecret() throws IOException;
}
