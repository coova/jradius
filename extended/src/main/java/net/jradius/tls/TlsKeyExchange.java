package net.jradius.tls;

import java.io.IOException;
import java.io.InputStream;

/**
 * A generic interface for key exchange implementations in TLS 1.0.
 */
interface TlsKeyExchange
{
    enum Algorithm {

       KE_RSA("RSA", 1),
       KE_DHE_DSS("DHE_DSS", 3),
       KE_DHE_RSA("DHE_RSA", 5),
       KE_DH_DSS("DH_DSS", 7),
       KE_DH_RSA("DH_RSA", 8),
       KE_SRP("SRP", 10),
       KE_SRP_DSS("SRP_DSS", 11),
       KE_SRP_RSA("SRP_RSA", 12);

       private final String name;
       private final int id;

       Algorithm(String name, int id) {
           this.name = name;
           this.id = id;
       }

       public String getName() {
           return name;
       }

       public int getId() {
           return id;
       }

    }

    void skipServerCertificate() throws IOException;

    void processServerCertificate(CertificateChain serverCertificate) throws IOException;

    void skipServerKeyExchange() throws IOException;

    void processServerKeyExchange(InputStream is, SecurityParameters securityParameters)
        throws IOException;

    byte[] generateClientKeyExchange() throws IOException;

    byte[] generatePremasterSecret() throws IOException;

    Algorithm getAlgorithm();
}
