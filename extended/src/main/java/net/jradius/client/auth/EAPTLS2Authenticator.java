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
import java.nio.ByteBuffer;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.tls.TlsProtocolHandler;
import net.jradius.exception.RadiusException;
import net.jradius.packet.RadiusPacket;
import net.jradius.log.RadiusLog;

import org.bouncycastle.crypto.tls.AlwaysValidVerifyer;


/**
 * EAP-TLS Authentication (and TLS Tunnel support).
 * 
 * @author David Bird
 */
public class EAPTLS2Authenticator extends EAPAuthenticator
{
    public static final String NAME = "eap-tls";

    private String keyFileType;
    private String keyFile;
    private String keyPassword;
    
    private String caFileType;
    private String caFile;
    private String caPassword;
    
    private Boolean trustAll = Boolean.FALSE;

    private TlsProtocolHandler handler = new TlsProtocolHandler();
    private AlwaysValidVerifyer verifyer = new AlwaysValidVerifyer();
    
    private ByteBuffer receivedEAP = ByteBuffer.allocate(10000000);
    
    public EAPTLS2Authenticator()
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
    public void setupRequest(RadiusClient c, RadiusPacket p) throws RadiusException
    {
        super.setupRequest(c, p);
        init();
    }

    /**
     * Initializs the SSL layer.
     * @throws RadiusException
     */
    protected void init() throws RadiusException
    {
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

            switch(state)
            {
                case TLS_CLIENT_HELLO:
                {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    handler.clientHello(os, verifyer);
                    data = os.toByteArray();
                    state = TLS_SERVER_HELLO;
                }
                break;
                
                case TLS_SERVER_HELLO:
                {
                    receivedEAP.flip();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    handler.serverHello(new ByteArrayInputStream(receivedEAP.array(), receivedEAP.position(), receivedEAP.remaining()), os);
                    data = os.toByteArray();
                    state = TLS_APP_DATA;
                    receivedEAP.clear();
                }
                break;
                
                case TLS_APP_DATA:
                {
                    receivedEAP.flip();
                    ByteArrayInputStream is = new ByteArrayInputStream(receivedEAP.array(), receivedEAP.position(), receivedEAP.remaining());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();

                    byte[] in = handler.readApplicationData(is, os);

                    try
                    {
                        doTunnelAuthentication(id, in);
                    }
                    catch(Throwable e)
                    {
                        RadiusLog.error(e.getMessage(), e);
                    }

                    handler.writeApplicationData(is, os, getAppBuffer());

                    data = os.toByteArray();
                    receivedEAP.clear();
                }
                break;
                
                default:
                {
                	RadiusLog.error("-----\n\nUnhandled EAP-TLS packet\n\n------\n");
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
                response[1] = (byte) (length >> 24 & 0xFF);
                response[2] = (byte) (length >> 16 & 0xFF);
                response[3] = (byte) (length >> 8 & 0xFF);
                response[4] = (byte) (length & 0xFF);
                System.arraycopy(data, 0, response, 5, data.length);
        	}
        }

        return response;
    }
    
    protected void doTunnelAuthentication(byte id, byte[] in) throws Throwable
    {
        // Not needed for EAP-TLS, but dependent protocols (PEAP, EAP-TTLS) implement this
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

    /*
    private class NoopX509TrustManager implements X509TrustManager
    {
        public void checkClientTrusted(X509Certificate[] chain, String authType) { }
        public void checkServerTrusted(X509Certificate[] chain, String authType) { }
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
    }
    */
}
