/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006 David Bird <david@coova.com>
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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedHashMap;

import net.jradius.client.auth.CHAPAuthenticator;
import net.jradius.client.auth.EAPMD5Authenticator;
import net.jradius.client.auth.EAPMSCHAPv2Authenticator;
import net.jradius.client.auth.MSCHAPv1Authenticator;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.client.auth.PAPAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.exception.RadiusException;
import net.jradius.exception.RadiusSecurityException;
import net.jradius.exception.TimeoutException;
import net.jradius.exception.UnknownAttributeException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessChallenge;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.AccountingResponse;
import net.jradius.packet.CoARequest;
import net.jradius.packet.CoAResponse;
import net.jradius.packet.DisconnectRequest;
import net.jradius.packet.DisconnectResponse;
import net.jradius.packet.PacketFactory;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.session.JRadiusSession;
import net.jradius.util.MessageAuthenticator;


/**
 * A Radius Client Context
 *
 * @author David Bird
 */
public class RadiusClient
{
    public static final int defaultAuthPort = 1812;
    public static final int defaultAcctPort = 1813;
    public static final int defaultTimeout  = 60;

    protected InetAddress remoteInetAddress;
    protected String sharedSecret;

    protected int authPort = defaultAuthPort;
    protected int acctPort = defaultAcctPort;
    protected int socketTimeout = defaultTimeout * 1000;

    protected DatagramSocket socket;
    private MessageDigest md5Digest;

    protected JRadiusSession session;

    private static final RadiusFormat format = RadiusFormat.getInstance();
        
    protected static final LinkedHashMap authenticators = new LinkedHashMap();
    
    static
    {
        // Supported Authentication Protocols
        registerAuthenticator("pap", PAPAuthenticator.class);
        registerAuthenticator("chap", CHAPAuthenticator.class);
        registerAuthenticator("mschapv1", MSCHAPv1Authenticator.class);
        registerAuthenticator("mschapv2", MSCHAPv2Authenticator.class);
        registerAuthenticator("mschap", MSCHAPv2Authenticator.class);
        registerAuthenticator("eap-md5", EAPMD5Authenticator.class);
        registerAuthenticator("eap-mschapv2", EAPMSCHAPv2Authenticator.class);
        try
        {
            registerAuthenticator("eap-tls",  "net.jradius.client.auth.EAPTLSAuthenticator");
            registerAuthenticator("eap-ttls", "net.jradius.client.auth.EAPTTLSAuthenticator");
            registerAuthenticator("peap", "net.jradius.client.auth.PEAPAuthenticator");
            //registerAuthenticator("eap-aka",  "net.jradius.client.auth.EAPAKAAuthenticator");
        }
        catch (ClassNotFoundException e)
        {
            RadiusLog.warn("EAP-TLS and EAP-TTLS are only available with Java 1.5");
        }
        // Lets use the Gnu-Crypto Provider
        //if (java.security.Security.getProvider("GNU-CRYPTO") == null)
        //java.security.Security.addProvider(new gnu.crypto.jce.GnuCrypto());
    }
    
    /**
     * Default constructor
     */
    public RadiusClient() 
    { 
        try
        {
            this.socket = new DatagramSocket();
            this.md5Digest = MessageDigest.getInstance("MD5");
        }
        catch (Exception e)
        {
            RadiusLog.error(e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * RadiusClient constructor
     * @param address The Internet address to send to
     * @param secret Our shared secret
     * @throws RadiusException
     */
    public RadiusClient(InetAddress address, String secret) 
    {
        this();
        setRemoteInetAddress(address);
        setSharedSecret(secret);
    }

    /**
     * RadiusClient constructor
     * @param address The Internet address to send to
     * @param secret Our shared secret
     * @param authPort The authentication port
     * @param acctPort The accounting port
     * @param timeout Timeout (time to wait for a reply)
     * @throws RadiusException
     */
    public RadiusClient(InetAddress address, String secret, int authPort, int acctPort, int timeout) 
    {
        this();
        setRemoteInetAddress(address);
        setSharedSecret(secret);
        setAuthPort(authPort);
        setAcctPort(acctPort);
        setSocketTimeout(timeout);
    }

    public void close()
    {
        this.socket.close();
    }
    
    /**
     * Registration of supported RadiusAuthenticator protocols
     * @param name The authentication protocol name
     * @param c The RadiusAuthenticator class that implements the protocol
     */
    public static void registerAuthenticator(String name, Class c)
    {
        authenticators.put(name, c);
    }
    
    public static void registerAuthenticator(String name, String className) throws ClassNotFoundException
    {
        Class c = Class.forName(className);
        authenticators.put(name, c);
    }
    
    /**
     * Get a supported RadiusAuthenticator based on the protocol name. If no
     * protocol with that name is supported, null is returned. If the authenticator
     * class for the named protocol has writable bean properties, these can be set by 
     * appending a colon separated list of property=value pairs to the protocolName. 
     * For instance, the EAP-TLS (and EAP-TTLS since it derives from EAP-TLS) authenticator 
     * class has numerous configurable properties (including keyFile, keyFileType, keyPassword, etc).
     * <p>
     * Examples:
     * <ul>
     * <li>getAuthProtocol("pap") returns PAPAuthenticator</li>
     * <li>getAuthProtocol("chap") returns CHAPAuthenticator</li>
     * <li>getAuthProtocol("eap-md5") returns EAPMD5Authenticator</li>
     * <li>getAuthProtocol("eap-ttls") returns default EALTTLSAuthenticator</li>
     * <li>getAuthProtocol("eap-tls:keyFile=keystore:keyPassword=mypass") returns EALTLSAuthenticator with setKeyFile("keystore") and setKeyPassword("mypass")</li>
     * <li>getAuthProtocol("eap-ttls:trustAll=true") returns EALTTLSAuthenticator with setTrustAll(true)</li>
     * </ul>
     * Keep in mind that Java 1.5 is required for EAP-TLS/TTLS and only PAP is usable as the inner protocol
     * because of limitations of Java 1.5.
     * <p>
     * @param protocolName The requested authentication protocol
     * @return Returns an instance of RadiusAuthenticator or null
     */
    public static RadiusAuthenticator getAuthProtocol(String protocolName)
    {
        RadiusAuthenticator auth = null;
        String[] args = null;
        int i;

        if ((i = protocolName.indexOf(':')) > 0)
        {
            if (i < protocolName.length())
            {
                args = protocolName.substring(i + 1).split(":");
            }            
            protocolName = protocolName.substring(0, i);
        }

        protocolName = protocolName.toLowerCase();
        
        Class c = (Class)authenticators.get(protocolName);
        
        if (c == null) return null;
        try 
        {
            auth = (RadiusAuthenticator)c.newInstance();
        }
        catch(Exception e) 
        { 
            e.printStackTrace();
            return null;
        }
        if (args != null)
        {
            HashMap elements = new HashMap();
            Class clazz = auth.getClass();
            PropertyDescriptor[] props = null;
            try
            {
                props = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            }
            catch (Exception e)
            {
                RadiusLog.error("Could not instanciate authenticator " + protocolName);
                return auth;
            }
            for (int p = 0; p < props.length; p++)
            {
                PropertyDescriptor pd = props[p];
                Method m = pd.getWriteMethod();
                if (m != null)
                {
                    elements.put(pd.getName(), pd);
                }
            }
            for (int a = 0; a < args.length; a++)
            {
                int eq = args[a].indexOf("=");
                if (eq > 0)
                {
                    String name = args[a].substring(0, eq);
                    String value = args[a].substring(eq + 1);
                    
                    PropertyDescriptor pd = (PropertyDescriptor)elements.get(name);
                    Method m = pd.getWriteMethod();

                    if (m == null)
                    {
                        RadiusLog.error("Authenticator " + protocolName + " does not have a writable attribute " + name);
                    }
                    else 
                    {
                        Object valueObject = value;
                        Class cType = pd.getPropertyType();
                        if (cType == Boolean.class)
                        {
                            valueObject = new Boolean(value);
                        }
                        else if (cType == Integer.class)
                        {
                            valueObject = new Integer(value);
                        }
                        try
                        {
                            m.invoke(auth, new Object[]{ valueObject });
                        }
                        catch (Exception e)
                        {
                            RadiusLog.error("Error setting attribute " + name + " for authenticator " + protocolName + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        return auth;
    }
    
    /**
     * Authenicates using the specified method. For all methods, it is assumed
     * that the user's password can be found in the User-Password attribute. All
     * authentiation requests automatically contain the Message-Authenticator attribute.
     * @param p RadiusPacket to be send (should be AccessRequest)
     * @param auth The RadiusAuthenticator instance (if null, PAPAuthenticator is used)
     * @param retries Number of times to retry (without response)
     * @return Returns the reply RadiusPacket
     * @throws RadiusException
     * @throws UnknownAttributeException
     */
    public RadiusResponse authenticate(AccessRequest p, RadiusAuthenticator auth, int retries)
    	throws RadiusException, UnknownAttributeException
    {
        if (auth == null) auth = new PAPAuthenticator();
        
        auth.setupRequest(this, p);
        auth.processRequest(p);
        
        while (true)
        {
            RadiusResponse reply = sendReceive(p, remoteInetAddress, authPort, retries);

            if (reply instanceof AccessChallenge)
            {
                auth.processChallenge(p, reply);
            }
            else
            {
                return reply;
            }
        }
    }

    /**
     * Add the Message-Authentivator attribute to the given RadiusPacket
     * @param request The RadiusPacket
     */
    private void generateMessageAuthenticator(RadiusPacket request)
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
    private boolean verifyMessageAuthenticator(RadiusRequest request, RadiusResponse reply, boolean required)
    {
    	Boolean verified = MessageAuthenticator.verifyReply(request, reply, sharedSecret);
        if (verified == null && required) return false;
        if (verified == null) return true;
        return verified.booleanValue();
    }
    
    /**
     * Verify the RADIUS Authenticator
     * @param request The RADIUS request send
     * @param reply The RADIUS reply received
     * @return Returns true if there is no Authenticator is correct
     */
    private boolean verifyAuthenticator(RadiusRequest request, RadiusResponse reply)
    {
    	return reply.verifyAuthenticator(request.getAuthenticator(), getSharedSecret());
    }
    
    /**
     * Send an accounting request
     * @param p The RadiusPacket to be sent (should be AccountingRequest)
     * @param retries Number of times to retry (without a response)
     * @return Returns the reply RadiusPacket
     * @throws RadiusException
     * @throws UnknownAttributeException
     */
    public AccountingResponse accounting(AccountingRequest p, int retries)
	throws RadiusException
	{
        RadiusResponse response = sendReceive(p, remoteInetAddress, acctPort, retries);
        if (!(response instanceof AccountingResponse))
            throw new RadiusException("Received something other than AccountingResponse to a AccountingRequest");
        return (AccountingResponse)response;
	}

    public DisconnectResponse disconnect(DisconnectRequest p, int retries)
    throws RadiusException
    {
        RadiusResponse response = sendReceive(p, remoteInetAddress, authPort, retries);
        if (!(response instanceof DisconnectResponse))
            throw new RadiusException("Received something other than DisconnectResponse to a DisconnectRequest");
        return (DisconnectResponse)response;
    }

    public CoAResponse changeOfAuth(CoARequest p, int retries)
    throws RadiusException
    {
        RadiusResponse response = sendReceive(p, remoteInetAddress, authPort, retries);
        if (!(response instanceof CoAResponse))
            throw new RadiusException("Received something other than CoAResponse to a CoARequest");
        return (CoAResponse)response;
    }

    /**
     * Send and receive RadiusPackets
     * @param p The RadiusPacket being sent
     * @param a The Internet Address sending to
     * @param port The port sending to
     * @param retries Number of times to retry (without response)
     * @return Returns the returned RadiusPacket
     */
    public RadiusResponse sendReceive(RadiusRequest p, InetAddress a, int port, int retries)
    throws RadiusException
    {
        RadiusResponse r = null;
        int tries = 0;
        
        if (p instanceof AccessRequest)
        {
        	generateMessageAuthenticator(p);
        }

        if (retries < 0) retries = 0; retries++; // do at least one
        
        while (tries < retries)
        {
            try
            {
                if (socketTimeout > 0) 
                {
                    socket.setSoTimeout(socketTimeout);
                }
                send(p, a, port, tries);
                r = receive();
                break;
            }
            catch (SocketTimeoutException e)
            {
            }
            catch (IOException e) 
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
    
    protected void send(RadiusPacket p, InetAddress a, int port, int attempt) throws IOException
    {
        if (attempt > 1)
        {
            RadiusLog.warn("RadiusClient retrying request (attempt " + attempt + ")...");
        }
        byte[] b = format.packPacket(p, sharedSecret);
        DatagramPacket request = new DatagramPacket(b, b.length, a, port);
        socket.send(request);
    }
    
    protected RadiusResponse receive() throws IOException, RadiusException
    {
        byte replyBytes[] = new byte[RadiusPacket.MAX_PACKET_LENGTH];
        DatagramPacket reply = new DatagramPacket(replyBytes, replyBytes.length);
        
        socket.receive(reply);
        
        RadiusPacket replyPacket = PacketFactory.parse(reply);
        
        if (!(replyPacket instanceof RadiusResponse))
        {
            throw new RadiusException("Received something other than a RADIUS Response to a Request");
        }

        return (RadiusResponse)replyPacket;
    }

    /**
     * @return Returns the MD5 MessageDigest being used
     */
    public MessageDigest getMD()
    {
        return this.md5Digest;
    }

    /**
     * @return Returns the RADIUS accounting port
     */
    public int getAcctPort()
    {
        return acctPort;
    }

    /**
     * @param acctPort The RADIUS accounting port
     */
    public void setAcctPort(int acctPort)
    {
        this.acctPort = acctPort;
    }
    
    /**
     * @return Returns the RADIUS authentication port
     */
    public int getAuthPort()
    {
        return authPort;
    }
    
    /**
     * @param authPort The RADIUS authentication port
     */
    public void setAuthPort(int authPort)
    {
        this.authPort = authPort;
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
    
    /**
     * @return Returns the remote server IP Address
     */
    public InetAddress getRemoteInetAddress()
    {
        return remoteInetAddress;
    }
    
    /**
     * @param remoteInetAddress The remote server IP Address
     */
    public void setRemoteInetAddress(InetAddress remoteInetAddress)
    {
        this.remoteInetAddress = remoteInetAddress;
    }
    
    /**
     * @return Returns the local IP Address (bind address)
     */
    public InetAddress getLocalInetAddress()
    {
        return socket.getLocalAddress();
    }
    
    /**
     * @param localInetAddress The local IP Address to bind to
     */
    public void setLocalInetAddress(InetAddress localInetAddress)
    {
        // TODO: create a socket bound to the localInetAddress
    }
    
    /**
     * @return Returns the shared secret
     */
    public String getSharedSecret()
    {
        return this.sharedSecret;
    }
    
    /**
     * @param sharedSecret The shared secret to set
     */
    public void setSharedSecret(String sharedSecret)
    {
        this.sharedSecret = sharedSecret;
    }
}
