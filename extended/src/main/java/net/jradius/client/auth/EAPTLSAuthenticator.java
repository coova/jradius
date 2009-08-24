/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import net.jradius.client.RadiusClient;
import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.util.KeyStoreUtil;


/**
 * EAP-TLS Authentication (and TLS Tunnel support).
 * 
 * @author David Bird
 */
public class EAPTLSAuthenticator extends EAPAuthenticator
{
    public static final String NAME = "eap-tls";

    private SSLContext sslContext;
    private SSLEngine sslEngine;
    private SSLSession sslSession;
    
    private ByteBuffer appInBuffer;
    private ByteBuffer appOutBuffer;
    private ByteBuffer packetInBuffer;
    private ByteBuffer packetOutBuffer;
    
    private ByteArrayOutputStream packetInput = new ByteArrayOutputStream();
    private ByteArrayOutputStream packetOutput = new ByteArrayOutputStream();
    private ByteArrayOutputStream appOutput = new ByteArrayOutputStream();
    
    private String keyFileType;
    private String keyFile;
    private String keyPassword;
    
    private String caFileType;
    private String caFile;
    private String caPassword;
    
    private Boolean trustAll = Boolean.FALSE;

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
    public void setupRequest(RadiusClient c, RadiusPacket p) throws RadiusException
    {
        client = c;
        username = p.findAttribute(AttributeDictionary.USER_NAME);
        password = p.findAttribute(AttributeDictionary.USER_PASSWORD);
        init();
    }

    /**
     * Initializs the SSL layer.
     * @throws RadiusException
     */
    protected void init() throws RadiusException
    {
        try
        {
            KeyManager keyManagers[] = null;
            TrustManager trustManagers[] = null;
            
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
            
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            
            sslEngine = sslContext.createSSLEngine();

            /*
            String[] cs = sslEngine.getSupportedCipherSuites();
            for (int i=0; i<cs.length; i++)
                RadiusLog.debug(cs[i]);
            */

            sslEngine.setEnableSessionCreation(true);
            sslEngine.setUseClientMode(true);
            sslEngine.setWantClientAuth(true);
            sslEngine.setNeedClientAuth(true);
            sslEngine.setEnabledProtocols(new String[] { "TLSv1" });
            sslEngine.setEnabledCipherSuites(new String[] { 
                    "TLS_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_DHE_DSS_WITH_AES_128_CBC_SHA" });
  
            sslSession = sslEngine.getSession();
            
            appInBuffer = ByteBuffer.allocate(sslSession.getApplicationBufferSize());
            appOutBuffer = ByteBuffer.allocate(sslSession.getApplicationBufferSize());
            packetInBuffer = ByteBuffer.allocate(sslSession.getPacketBufferSize());
            packetOutBuffer = ByteBuffer.allocate(sslSession.getPacketBufferSize());
            
            /*
            appInBuffer 	= ByteBuffer.allocate(200000);
            appOutBuffer 	= ByteBuffer.allocate(2000000);
            packetInBuffer 	= ByteBuffer.allocate(200000);
            packetOutBuffer	= ByteBuffer.allocate(2000000);
            */
        }
        catch (Exception e)
        {
            throw new RadiusException(e);
        }
    }

    /**
     * @see net.jradius.client.auth.RadiusAuthenticator#getAuthName()
     */
    public String getAuthName()
    {
        return NAME;
    }

    protected int None = 0;
    protected int Handshaking = 1;
    protected int Finished = 2;
    private int tlsState = None;

    protected int tlsHandshake() throws SSLException
    {
        SSLEngineResult result = null;
        SSLEngineResult.HandshakeStatus hsStatus = null;
        Runnable task;
        boolean didWrap = false;
        
        if (tlsState == Finished)
        {
            return tlsState;
        }
        
        if (tlsState == None)
        {
            tlsState = Handshaking;
            sslEngine.beginHandshake();
        }
        
        while (true)
        {
            hsStatus = sslEngine.getHandshakeStatus();
            
            RadiusLog.debug(hsStatus.toString());
            
            if (hsStatus == SSLEngineResult.HandshakeStatus.FINISHED ||
                    hsStatus == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
            {
                tlsState = Finished;
                return tlsState;
            }
            else if (hsStatus == SSLEngineResult.HandshakeStatus.NEED_TASK)
            {
                while ((task = sslEngine.getDelegatedTask()) != null) task.run();
            }
            else if (hsStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP)
            {
                result = sslEngine.wrap(appOutBuffer, packetOutBuffer);
                packetOutBuffer.flip();
                if (packetOutBuffer.hasRemaining())
                {
                    packetOutput.write(
                            packetOutBuffer.array(), 
                            packetOutBuffer.arrayOffset(), 
                            packetOutBuffer.remaining());
                }
                packetOutBuffer.clear();
                didWrap = true;
            }
            else if (hsStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP)
            {
                if (didWrap) return tlsState;
                packetInBuffer.flip();
                
                while (packetInBuffer.hasRemaining())
                { 
                    result = sslEngine.unwrap(packetInBuffer, appInBuffer);
                    if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK)
                    {
                        while ((task = sslEngine.getDelegatedTask()) != null) task.run();
                    }
                }

                packetInBuffer.clear();
            }
            else
            {
                return tlsState;
            }
        }
    }

    protected void updatePacketBuffer(byte[] b)
    {
        try
        {
            if (tlsState == Finished)
            {
                packetInput.write(b);
            }
            else
            {
                putPacketBuffer(b);
            }
        }
        catch (IOException e) { }
    }
    
    protected void putPacketBuffer(byte[] d) throws SSLException
    {
        Runnable task;
        
        int chunk = packetInBuffer.capacity();
        int left = d.length;
        
        for (int offset = 0; left > 0; offset += chunk)
        {
            if (left < chunk) chunk = left;
            left -= chunk;
            
            packetInBuffer.put(d, offset, chunk);
            
            if (tlsState == Finished)
            {
            packetInBuffer.flip();
            
            SSLEngineResult result = null;

            while ((result == null || result.getStatus() == SSLEngineResult.Status.OK) && 
                    packetInBuffer.hasRemaining())
            { 
                result = sslEngine.unwrap(packetInBuffer, appInBuffer);

                if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK)
                {
                    while ((task = sslEngine.getDelegatedTask()) != null) task.run();
                }

                appInBuffer.flip();
                
                if (appInBuffer.hasRemaining())
                {
                    appOutput.write(
                            appInBuffer.array(), 
                            appInBuffer.arrayOffset(), 
                            appInBuffer.remaining());
                }
                
                appInBuffer.clear();
            }
            packetInBuffer.clear();
        }    
        }
    }

    protected byte[] getPacketInputBuffer()
    {
        byte b[] = packetInput.toByteArray();
        packetInput = new ByteArrayOutputStream();
        return b;
    }

    protected byte[] getPacketOutputBuffer()
    {
        packetOutBuffer.flip();
        if (packetOutBuffer.hasRemaining())
        {
            packetOutput.write(
                    packetOutBuffer.array(), 
                    packetOutBuffer.arrayOffset(), 
                    packetOutBuffer.remaining());
        }
        packetOutBuffer.clear();
        byte b[] = packetOutput.toByteArray();
        packetOutput = new ByteArrayOutputStream();
        return b;
    }

    protected void putAppBuffer(byte[] d) throws SSLException
    {
        SSLEngineResult result = null;
        Runnable task;
        
        int chunk = appOutBuffer.capacity();
        int left = d.length;
        
        for (int offset = 0; left > 0; offset += chunk)
        {
            if (left < chunk) chunk = left;
            left -= chunk;
            
            appOutBuffer.clear();
            appOutBuffer.put(d, offset, chunk);
            appOutBuffer.flip();
        
            while (appOutBuffer.hasRemaining())
            { 
                result = sslEngine.wrap(appOutBuffer, packetOutBuffer);

                if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK)
                {
                    while ((task = sslEngine.getDelegatedTask()) != null) task.run();
                }

                if (tlsState == Finished)
                {
                    packetOutBuffer.flip();
                    if (packetOutBuffer.hasRemaining())
                    {
                        packetOutput.write(
                                packetOutBuffer.array(), 
                                packetOutBuffer.arrayOffset(), 
                                packetOutBuffer.remaining());
                    }
                    packetOutBuffer.clear();
                }
                else 
                {
                }
            }

            packetInBuffer.clear();
        }
    }

    protected byte[] getAppBuffer() throws SSLException
    {
        byte b[] = appOutput.toByteArray();
        appOutput = new ByteArrayOutputStream();
        return b;
    }
    
    protected static final short TLS_START 			 = 0x20;
    protected static final short TLS_MORE_FRAGMENTS  = 0x40;
    protected static final short TLS_HAS_LENGTH  	 = 0x80;
    
    protected byte[] eapFragmentedReply = null;
    protected int eapFragmentedOffset = 0;
    
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
                byte b[] = new byte[bb.remaining()];
                bb.get(b, 0, b.length);
                updatePacketBuffer(b);
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

            if (tlsHandshake() == Finished)
            {
                try
                {
                    byte[] in = getAppBuffer();

                    doTunnelAuthentication(id, in);
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }

            data = getPacketInputBuffer();

            if (data != null && data.length > 0)
            {
                putPacketBuffer(data);
            }

            data = getPacketOutputBuffer();
            
            if (data != null && data.length > 1024)
            {
                eapFragmentedReply = data;
                return nextFragment();
            }
            
            return tlsResponse(flags, data);
        }
        catch (SSLException e)
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
            length += data.length + 4;
            flags |= TLS_HAS_LENGTH;
        }

        byte[] response = new byte[length];
        response[0] = flags;
        
        if (data != null && data.length > 0) 
        {
            length -= 1;
            response[1] = (byte) (length >> 24 & 0xFF);
            response[2] = (byte) (length >> 16 & 0xFF);
            response[3] = (byte) (length >> 8 & 0xFF);
            response[4] = (byte) (length & 0xFF);
            System.arraycopy(data, 0, response, 5, data.length);
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

}
