/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package net.jradius.client.auth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Vector;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import net.jradius.client.RadiusClient;
import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusPacket;
import net.jradius.tls.AlwaysValidVerifyer;
import net.jradius.tls.Certificate;
import net.jradius.tls.DefaultTlsClient;
import net.jradius.tls.TlsProtocolHandler;
import net.jradius.util.KeyStoreUtil;
import org.bouncycastle.asn1.ASN1Encodable;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.oiw.ElGamalParameter;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x9.X962NamedCurves;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;


/**
 * EAP-TLS Authentication (and TLS Tunnel support).
 * 
 * @author David Bird
 */
public class EAPTLSAuthenticator extends EAPAuthenticator
{
	public static final String NAME = "eap-tls";

    private String keyFileType;
    private String keyFile;
    private String keyPassword;
    
    private String caFileType;
    private String caFile;
    private String caPassword;
    
    private Boolean trustAll = Boolean.FALSE;

    private ByteArrayOutputStream bout;
    private ByteArrayInputStream bin;

    private TlsProtocolHandler handler = new TlsProtocolHandler();
    private AlwaysValidVerifyer verifyer = new AlwaysValidVerifyer();
    
    private DefaultTlsClient tlsClient = null;
    
    private ByteBuffer receivedEAP = ByteBuffer.allocate(10000000);

    private KeyManager keyManagers[] = null;
    private TrustManager trustManagers[] = null;

    public EAPTLSAuthenticator()
    {
        setEAPType(EAP_TLS);

        keyFileType = "pkcs12";
        keyPassword = "";
        caFileType = "pkcs12";
        caPassword = "";
    }
    
    /* (non-Javadoc)
     * @see net.sf.jradius.client.auth.RadiusAuthenticator#setupRequest(net.sf.jradius.client.RadiusClient, net.sf.jradius.packet.RadiusPacket)
     */
    public void setupRequest(RadiusClient c, RadiusPacket p) throws RadiusException, NoSuchAlgorithmException
    {
        super.setupRequest(c, p);
        init();
    }

    /**
     * Initializs the SSL layer.
     * @throws Exception 
     * @throws FileNotFoundException 
     */
    public void init() throws RadiusException
    {
    	try
    	{
	        if (getKeyFile() != null)
	        {
	        	keyManagers = KeyStoreUtil.loadKeyManager(getKeyFileType(), new FileInputStream(getKeyFile()), getKeyPassword());
	        }
	
	        if (getTrustAll().booleanValue()) 
	        {
	        	trustManagers = KeyStoreUtil.trustAllManager();
	        }
	        else if (getCaFile() != null)
	        {
	        	trustManagers = KeyStoreUtil.loadTrustManager(getCaFileType(), new FileInputStream(getCaFile()), getCaPassword());
	        }
	        
			tlsClient = new DefaultTlsClient(verifyer);

			try
			{
				if (keyManagers != null && keyManagers.length > 0)
				{
					X509CertificateStructure[] certs = null;
					X509Certificate[] certChain = ((X509KeyManager)keyManagers[0]).getCertificateChain("");
					PrivateKey key = ((X509KeyManager)keyManagers[0]).getPrivateKey("");
					Vector tmp = new Vector();

					for (X509Certificate cert : certChain)
					{
			            ByteArrayInputStream bis = new ByteArrayInputStream(cert.getEncoded());
			            ASN1InputStream ais = new ASN1InputStream(bis);
			            ASN1Primitive o = ais.readObject();
			            tmp.addElement(X509CertificateStructure.getInstance(o));
			            if (bis.available() > 0)
			            {
			                throw new IllegalArgumentException(
			                    "Sorry, there is garbage data left after the certificate");
			            }
			        }
			        certs = new X509CertificateStructure[tmp.size()];
			        for (int i = 0; i < tmp.size(); i++)
			        {
			            certs[i] = (X509CertificateStructure)tmp.elementAt(i);
			        }

					tlsClient.enableClientAuthentication(new Certificate(certs), createKey(key.getEncoded()));
		        }
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			handler.setSendCertificate(isCertificateRequired());
	        handler.setKeyManagers(keyManagers);
	        handler.setTrustManagers(trustManagers);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}

    	/*
        try
        {
            KeyManager keyManagers[] = null;
            TrustManager trustManagers[] = null;
            
            if (getKeyFile() != null)
            {
                KeyStore ksKeys = KeyStore.getInstance(getKeyFileType());
                ksKeys.load(new FileInputStream(getKeyFile()), getKeyPassword().toCharArray());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ksKeys, getKeyPassword().toCharArray());
                
                keyManagers = kmf.getKeyManagers();
            }

            if (getCaFile() != null)
            {
                KeyStore caKeys = KeyStore.getInstance(getCaFileType());
                caKeys.load(new FileInputStream(getCaFile()), getCaPassword().toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(caKeys);
                
                trustManagers = tmf.getTrustManagers();
            }
            else 
            {
                if (getTrustAll().booleanValue()) 
                {
                    trustManagers = new TrustManager[]{ new NoopX509TrustManager() };
                }
            }
        }
        catch (Exception e)
        {
            throw new RadiusException(e);
        }
        */
    }

	/**
     * @see net.jradius.client.auth.RadiusAuthenticator#getAuthName()
     */
    public String getAuthName()
    {
        return NAME;
    }

    int state = 0;
    protected static final short TLS_START 			 = 0x20;
    protected static final short TLS_MORE_FRAGMENTS  = 0x40;
    protected static final short TLS_HAS_LENGTH  	 = 0x80;

    protected static final int TLS_CLIENT_HELLO = 0;
    protected static final int TLS_SERVER_HELLO = 1;
    protected static final int TLS_APP_DATA = 2;
    
    protected byte[] eapFragmentedReply = null;
    protected int eapFragmentedOffset = 0;
    
    ByteArrayOutputStream appOutput = new ByteArrayOutputStream();
    
    public void setServerMode()
    {
    	state = TLS_SERVER_HELLO;
    }
    
    public void putAppBuffer(byte []b)
    {
        try
        {
            appOutput.write(b);
        }
        catch (Exception e)
        {
            RadiusLog.error(e.getMessage(), e);
        }
    }

    public void putAppBuffer(byte []b, int off, int len)
    {
        try
        {
            appOutput.write(b, off, len);
        }
        catch (Exception e)
        {
            RadiusLog.error(e.getMessage(), e);
        }
    }

    protected byte[] getAppBuffer() 
    {
        byte b[] = appOutput.toByteArray();
        appOutput = new ByteArrayOutputStream();
        return b;
    }

    public byte[] doEAPType(byte id, byte[] data) throws RadiusException
    {
        ByteBuffer bb = ByteBuffer.wrap(data);
        
        byte dflags = bb.get();
        byte flags = 0;
        int dlen = 0;
        
        try
        {
            if ((dflags & TLS_HAS_LENGTH) != 0)
            {
                dlen = bb.getInt();
            }

            if (bb.hasRemaining())
            {
                receivedEAP.put(bb.array(), bb.position(), bb.remaining());
            }
            else
            {
                // We were sent a NAK, lets see if we are fragmenting
                if (eapFragmentedReply != null)
                {
                    return nextFragment();
                }
            }

            if ((dflags & TLS_MORE_FRAGMENTS) != 0)
            {
                return tlsResponse(flags, null);
            }

            switch (state)
            {
                case 0:
                {
                    ByteArrayInputStream is = new ByteArrayInputStream(receivedEAP.array(), receivedEAP.position(), receivedEAP.remaining());
                	ByteArrayOutputStream os = new ByteArrayOutputStream();
                	handler.connect(is, os, tlsClient);
                    data = os.toByteArray();
                    state = 1;
                }
                break;

                case 1:
                {
                    receivedEAP.flip();
                    ByteArrayInputStream is = new ByteArrayInputStream(receivedEAP.array(), 
                    		receivedEAP.position(), 
                    		receivedEAP.remaining());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    short s = handler.updateConnectState(is, os);
                    data = os.toByteArray();
                    receivedEAP.clear();
                    if (s == TlsProtocolHandler.CS_DONE) 
                    {
                    	state = 2;
                    }
                    else
                    {
                    	break;
                    }
                }
                // drop through....

                case 2:
                {
                    receivedEAP.flip();
                    ByteArrayInputStream is = new ByteArrayInputStream(receivedEAP.array(), 
                    		receivedEAP.position(), 
                    		receivedEAP.remaining());

                    ByteArrayOutputStream os = new ByteArrayOutputStream();

                    byte[] in = handler.readApplicationData(is, os);

                	// System.err.println("doTunnelAuth()");
                    try
                    {
                    	if (doTunnelAuthentication(id, in))
                    	{
    	                    handler.writeApplicationData(is, os, getAppBuffer());
                    	}
                    }
                    catch(Throwable e)
                    {
                        RadiusLog.error(e.getMessage(), e);
                    }
                    
                    data = os.toByteArray();
                    receivedEAP.clear();
                }
                break;
            }
            
            if (data != null && data.length > 1024)
            {
                eapFragmentedReply = data;
                return nextFragment();
            }
            
            return tlsResponse(flags, data);
        }
        catch (Exception e)
        {
            throw new RadiusException(e);
        }
    }
    
    protected byte[] nextFragment()
    {
        int left = eapFragmentedReply.length - eapFragmentedOffset;
        byte flags = (byte)0;
        
        if (left > 1024) 
        {
            left = 1024;
            flags |= TLS_MORE_FRAGMENTS;
        }

        byte[] data = new byte[left];
        System.arraycopy(eapFragmentedReply, eapFragmentedOffset, data, 0, data.length);
        eapFragmentedOffset += data.length;
        
        if (eapFragmentedReply.length == eapFragmentedOffset)
        {
            eapFragmentedReply = null;
            eapFragmentedOffset = 0;
        }
        
        return tlsResponse(flags, data);
    }

    protected byte[] tlsResponse(byte flags, byte[] data)
    {
        int length = 1;

        if (data != null && data.length > 0) 
        {
        	length += data.length;
        	if (flags != 0) 
        	{
        		length += 4;
        		flags |= TLS_HAS_LENGTH;
        	}
        }

        byte[] response = new byte[length];
        response[0] = flags;
        
        if (data != null && data.length > 0) 
        {
        	if (flags == 0) 
        	{
        		System.arraycopy(data, 0, response, 1, data.length);
        	}
        	else 
        	{ 
        		length -= 1;
                response[1] = (byte) (eapFragmentedReply.length >> 24 & 0xFF);
                response[2] = (byte) (eapFragmentedReply.length >> 16 & 0xFF);
                response[3] = (byte) (eapFragmentedReply.length >> 8 & 0xFF);
                response[4] = (byte) (eapFragmentedReply.length & 0xFF);
                System.arraycopy(data, 0, response, 5, data.length);
        	}
        }

        return response;
    }
    
    protected boolean doTunnelAuthentication(byte id, byte[] in) throws Throwable
    {
        // Not needed for EAP-TLS, but dependent protocols (PEAP, EAP-TTLS) implement this
    	return false;
    }

    public String getCaFile()
    {
        return caFile;
    }

    public void setCaFile(String caFile)
    {
        this.caFile = caFile;
    }

    public String getCaFileType()
    {
        return caFileType;
    }

    public void setCaFileType(String caFileType)
    {
        this.caFileType = caFileType;
    }

    public String getKeyFile()
    {
        return keyFile;
    }

    public void setKeyFile(String keyFile)
    {
        this.keyFile = keyFile;
    }

    public String getKeyFileType()
    {
        return keyFileType;
    }

    public void setKeyFileType(String keyFileType)
    {
        this.keyFileType = keyFileType;
    }

    public String getKeyPassword()
    {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword)
    {
        this.keyPassword = keyPassword;
    }

    public String getCaPassword()
    {
        return caPassword;
    }

    public void setCaPassword(String caPassword)
    {
        this.caPassword = caPassword;
    }
    
    public Boolean getTrustAll()
    {
        return trustAll;
    }

    public void setTrustAll(Boolean trustAll)
    {
        this.trustAll = trustAll;
    }

    
    /**
     * Create a private key parameter from a PKCS8 PrivateKeyInfo encoding.
     * 
     * @param privateKeyInfoData the PrivateKeyInfo encoding
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(
        byte[] privateKeyInfoData)
        throws IOException
    {
        return createKey(
            PrivateKeyInfo.getInstance(ASN1TaggedObject.fromByteArray(privateKeyInfoData))
        );
    }

    /**
     * Create a private key parameter from a PKCS8 PrivateKeyInfo encoding read from a stream.
     * 
     * @param inStr the stream to read the PrivateKeyInfo encoding from
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(
        InputStream inStr)
        throws IOException
    {
        return createKey(
            PrivateKeyInfo.getInstance(
                new ASN1InputStream(inStr).readObject()));
    }

    /**
     * Create a private key parameter from the passed in PKCS8 PrivateKeyInfo object.
     * 
     * @param keyInfo the PrivateKeyInfo object containing the key material
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(
        PrivateKeyInfo    keyInfo)
        throws IOException
    {
        AlgorithmIdentifier     algId = keyInfo.getPrivateKeyAlgorithm();
        if (algId.getAlgorithm().equals(PKCSObjectIdentifiers.rsaEncryption))
        {
            RSAPrivateKey keyStructure=RSAPrivateKey.getInstance(keyInfo.parsePrivateKey());
            return new RSAPrivateCrtKeyParameters(
                                        keyStructure.getModulus(),
                                        keyStructure.getPublicExponent(),
                                        keyStructure.getPrivateExponent(),
                                        keyStructure.getPrime1(),
                                        keyStructure.getPrime2(),
                                        keyStructure.getExponent1(),
                                        keyStructure.getExponent2(),
                                        keyStructure.getCoefficient());
        }
        else if (algId.getAlgorithm().equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
            DHParameter     params = DHParameter.getInstance(keyInfo.getPrivateKeyAlgorithm().getParameters());
            ASN1Integer      derX = (ASN1Integer)keyInfo.parsePrivateKey();

            BigInteger lVal = params.getL();
            int l = lVal == null ? 0 : lVal.intValue();
            DHParameters dhParams = new DHParameters(params.getP(), params.getG(), null, l);

            return new DHPrivateKeyParameters(derX.getValue(), dhParams);
        }
        else if (algId.getAlgorithm().equals(OIWObjectIdentifiers.elGamalAlgorithm))
        {
            ElGamalParameter    params = ElGamalParameter.getInstance(keyInfo.getPrivateKeyAlgorithm().getParameters());
            ASN1Integer          derX = (ASN1Integer)keyInfo.parsePrivateKey();

            return new ElGamalPrivateKeyParameters(derX.getValue(), new ElGamalParameters(params.getP(), params.getG()));
        }
        else if (algId.getAlgorithm().equals(X9ObjectIdentifiers.id_dsa))
        {
            ASN1Integer          derX = (ASN1Integer)keyInfo.parsePrivateKey();
            ASN1Encodable de = keyInfo.getPrivateKeyAlgorithm().getParameters();

            DSAParameters parameters = null;
            if (de != null)
            {
                DSAParameter params = DSAParameter.getInstance(de);
                parameters = new DSAParameters(params.getP(), params.getQ(), params.getG());
            }

            return new DSAPrivateKeyParameters(derX.getValue(), parameters);
        }
        else if (algId.getAlgorithm().equals(X9ObjectIdentifiers.id_ecPublicKey))
        {
            X962Parameters      params = X962Parameters.getInstance(keyInfo.getPrivateKeyAlgorithm().getParameters());
            ECDomainParameters  dParams = null;
            if (params.isNamedCurve())
            {
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) params.getParameters();
                X9ECParameters      ecP = X962NamedCurves.getByOID(oid);

                if (ecP == null)
                {
                    ecP = SECNamedCurves.getByOID(oid);

                    if (ecP == null)
                    {
                        ecP = NISTNamedCurves.getByOID(oid);

                        if (ecP == null)
                        {
                            ecP = TeleTrusTNamedCurves.getByOID(oid);
                        }
                    }
                }

                dParams = new ECDomainParameters(
                                            ecP.getCurve(),
                                            ecP.getG(),
                                            ecP.getN(),
                                            ecP.getH(),
                                            ecP.getSeed());
            }
            else
            {
                X9ECParameters ecP = X9ECParameters.getInstance(params.getParameters());
                dParams = new ECDomainParameters(
                                            ecP.getCurve(),
                                            ecP.getG(),
                                            ecP.getN(),
                                            ecP.getH(),
                                            ecP.getSeed());
            }
            ECPrivateKey ec = ECPrivateKey.getInstance(keyInfo.getPrivateKeyAlgorithm());

            return new ECPrivateKeyParameters(ec.getKey(), dParams);
        }
        else
        {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }
    /*
    private class NoopX509TrustManager implements X509TrustManager
    {
        public void checkClientTrusted(X509Certificate[] chain, String authType) { }
        public void checkServerTrusted(X509Certificate[] chain, String authType) { }
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
    }
    */

	protected boolean isCertificateRequired() 
	{
		return true;
	}

	public KeyManager[] getKeyManagers() {
		return keyManagers;
	}

	public void setKeyManagers(KeyManager[] keyManagers) {
		this.keyManagers = keyManagers;
	}

	public TrustManager[] getTrustManagers() {
		return trustManagers;
	}

	public void setTrustManagers(TrustManager[] trustManagers) {
		this.trustManagers = trustManagers;
	}
}
