/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2009 Coova Technologies, LLC <support@coova.com>
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

package net.jradius.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import net.jradius.exception.RadiusException;
import net.jradius.exception.RadiusSecurityException;
import net.jradius.exception.TimeoutException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.util.MessageAuthenticator;

public abstract class RadiusClientTransport 
{
    protected InetAddress localInetAddress;
    protected InetAddress remoteInetAddress;
    protected String sharedSecret;

    protected int authPort;
    protected int acctPort;

    public static final int defaultTimeout  = 60;

    protected int socketTimeout = defaultTimeout * 1000;

    protected RadiusClient radiusClient;
    
    protected abstract void send(RadiusRequest req, int attempt) throws Exception;

    protected abstract RadiusResponse receive(RadiusRequest req) throws Exception;
	
	public abstract void close();

	protected TransportStatusListener statusListener;

    /**
     * Send and receive RadiusPackets
     * @param p The RadiusPacket being sent
     * @param a The Internet Address sending to
     * @param port The port sending to
     * @param retries Number of times to retry (without response)
     * @return Returns the returned RadiusPacket
     */
    public RadiusResponse sendReceive(RadiusRequest p, int retries) throws RadiusException
    {
    	InetAddress a = getRemoteInetAddress();
    	RadiusResponse r = null;
        int tries = 0;
        
        if (p instanceof AccessRequest)
        {
            try
            {
	        	generateMessageAuthenticator(p);
            }
            catch(IOException e)
            {
                throw new RadiusException(e);
            }
        }

        if (retries < 0) retries = 0; retries++; // do at least one
        
        while (tries < retries)
        {
            try
            {
                send(p, tries);
                r = receive(p);
                break;
            }
            catch (SocketTimeoutException e)
            {
            }
            catch (IOException e) 
            { 
                RadiusLog.warn("Unable to send or receive radius packet", e);
            }
            catch (Exception e) 
            {
            	e.printStackTrace();
            }
            tries++;
        }
        
        if (tries == retries)
        {                        
            throw new TimeoutException("Timeout: No Response from RADIUS Server");
        }
            
        if (!verifyAuthenticator(p, r))
        {
            throw new RadiusSecurityException("Invalid RADIUS Authenticator");
        }

        if (!verifyMessageAuthenticator(p, r, (r.findAttribute(AttributeDictionary.EAP_MESSAGE) != null)))
        {
            throw new RadiusSecurityException("Invalid RADIUS Message-Authenticator");
        }
        
        return r;
    }

    /**
     * Add the Message-Authentivator attribute to the given RadiusPacket
     * @param request The RadiusPacket
     */
    protected void generateMessageAuthenticator(RadiusPacket request) throws IOException
    {
    	MessageAuthenticator.generateRequestMessageAuthenticator(request, sharedSecret);
    }

    /**
     * Verify the Message-Authenticator based on RFC 2869
     * @param request The RADIUS request send
     * @param reply The RADIUS reply received
     * @param required Whether or not the Message-Authenticator is required (as for EAP)
     * @return Returns true if there is no Message-Authenticator or if it present and correct
     */
    protected boolean verifyMessageAuthenticator(RadiusRequest request, RadiusResponse reply, boolean required)
    {
		try
		{
			Boolean verified = MessageAuthenticator.verifyReply(request, reply, sharedSecret);
			if (verified == null && required) return false;
			if (verified == null) return true;
			return verified.booleanValue();
		}
		catch(IOException e)
		{
			return false;
		}
	}
    
    /**
     * Verify the RADIUS Authenticator
     * @param request The RADIUS request send
     * @param reply The RADIUS reply received
     * @return Returns true if there is no Authenticator is correct
     */
    protected boolean verifyAuthenticator(RadiusRequest request, RadiusResponse reply)
    {
    	return reply.verifyAuthenticator(request.getAuthenticator(), getSharedSecret());
    }

	public InetAddress getRemoteInetAddress() {
		return remoteInetAddress;
	}
	
	public void setRemoteInetAddress(InetAddress remoteInetAddress) {
		this.remoteInetAddress = remoteInetAddress;
	}
	
	public InetAddress getLocalInetAddress() {
		return localInetAddress;
	}

	public void setLocalInetAddress(InetAddress localInetAddress) {
		this.localInetAddress = localInetAddress;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}
	
	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}
	
	public int getAuthPort() {
		return authPort;
	}
	
	public void setAuthPort(int authPort) {
		this.authPort = authPort;
	}
	
	public int getAcctPort() {
		return acctPort;
	}
	
	public void setAcctPort(int acctPort) {
		this.acctPort = acctPort;
	}
    
    /**
     * @return Returns the socket timeout (in seconds)
     */
    public int getSocketTimeout()
    {
        return socketTimeout / 1000;
    }
    
    /**
     * @param socketTimeout The socket timeout (in seconds)
     */
    public void setSocketTimeout(int socketTimeout)
    {
        this.socketTimeout = socketTimeout * 1000;
    }

	public RadiusClient getRadiusClient() {
		return radiusClient;
	}

	public void setRadiusClient(RadiusClient radiusClient) {
		this.radiusClient = radiusClient;
	}

	public void setStatusListener(TransportStatusListener statusListener) {
		this.statusListener = statusListener;
	}
}
