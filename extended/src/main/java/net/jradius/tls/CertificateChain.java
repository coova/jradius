package net.jradius.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.Certificate;

/**
 * A representation for a certificate chain as used by a tls server.
 */
public class CertificateChain
{
    /**
     * The certificates.
     */
    protected Certificate[] certs;

    /**
     * Parse the ServerCertificate message.
     *
     * @param is The stream where to parse from.
     * @return A Certificate object with the certs, the server has sended.
     * @throws IOException If something goes wrong during parsing.
     */
    public static CertificateChain parse(InputStream is) throws IOException
    {
        Certificate[] certs;
        int left = TlsUtils.readUint24(is);
        Vector tmp = new Vector();
        while (left > 0)
        {
            int size = TlsUtils.readUint24(is);
            left -= 3 + size;
            byte[] buf = new byte[size];
            TlsUtils.readFully(buf, is);
            ByteArrayInputStream bis = new ByteArrayInputStream(buf);
            ASN1InputStream ais = new ASN1InputStream(bis);
            ASN1Primitive o = ais.readObject();
            tmp.addElement(Certificate.getInstance(o));
            if (bis.available() > 0)
            {
                throw new IllegalArgumentException(
                    "Sorry, there is garbage data left after the certificate");
            }
        }
        certs = new Certificate[tmp.size()];
        for (int i = 0; i < tmp.size(); i++)
        {
            certs[i] = (Certificate)tmp.elementAt(i);
        }
        return new CertificateChain(certs);
    }

    /**
     * Encodes version of the ClientCertificate message
     *
     * @param os stream to write the message to
     * @throws IOException If something goes wrong
     */
    protected void encode(OutputStream os) throws IOException
    {
        Vector encCerts = new Vector();
        int totalSize = 0;
        for (int i = 0; i < this.certs.length; ++i)
        {
            byte[] encCert = certs[i].getEncoded();
            encCerts.addElement(encCert);
            totalSize += encCert.length + 3;
        }

        TlsUtils.writeUint24(totalSize + 3, os);
        TlsUtils.writeUint24(totalSize, os);

        for (int i = 0; i < encCerts.size(); ++i)
        {
            byte[] encCert = (byte[])encCerts.elementAt(i);
            TlsUtils.writeOpaque24(encCert, os);
        }
    }

    /**
     * Private constructor from a cert array.
     *
     * @param certs The certs the chain should contain.
     */
    public CertificateChain(Certificate[] certs)
    {
        this.certs = certs;
    }

    /**
     * @return An array which contains the certs, this chain contains.
     */
    public Certificate[] getCerts()
    {
        Certificate[] result = new Certificate[certs.length];
        System.arraycopy(certs, 0, result, 0, certs.length);
        return result;
    }

    public X509Certificate[] toX509() throws IOException, CertificateException {
        X509Certificate[] x509 = new X509Certificate[certs.length];
        for (int i = 0; i < certs.length; i++) {
            Certificate cert = certs[i];
            InputStream in = new ByteArrayInputStream(cert.getEncoded());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            x509[i] = (X509Certificate)certFactory.generateCertificate(in);
            in.close();
        }

        return x509;
    }
}
