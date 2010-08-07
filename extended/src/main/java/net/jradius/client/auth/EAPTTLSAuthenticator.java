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

import java.nio.ByteBuffer;

import javax.net.ssl.SSLException;

import net.jradius.client.RadiusClient;
import net.jradius.dictionary.Attr_EAPMessage;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessChallenge;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.DiameterFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;


/**
 * EAP-TTLS Authentication. 
 * 
 * @author David Bird
 */
public class EAPTTLSAuthenticator extends EAPTLSAuthenticator implements TunnelAuthenticator
{
    public static final String NAME = "eap-ttls";
    private String innerProtocol = "pap";
    private RadiusAuthenticator tunnelAuth;
    private RadiusPacket tunnelRequest;
    private RadiusPacket tunnelChallenge;
    private AttributeList tunneledAttributes;
    
    private static final DiameterFormat diameterFormat = new DiameterFormat();

    public EAPTTLSAuthenticator()
    {
        setEAPType(EAP_TTLS);
    }
    
    /* (non-Javadoc)
     * @see net.jradius.client.auth.EAPTLSAuthenticator#init()
     */
    protected void init() throws RadiusException
    {
        super.init();
        tunnelAuth = RadiusClient.getAuthProtocol(getInnerProtocol());
        
        if (tunnelAuth == null ||
            tunnelAuth instanceof MSCHAPv2Authenticator ||
            tunnelAuth instanceof MSCHAPv1Authenticator ||
            tunnelAuth instanceof CHAPAuthenticator)
        {
            throw new RadiusException("You can not currently use " + tunnelAuth.getAuthName() +" within a TLS Tunnel because of limitations in Java 1.5.");
        }
    }

    protected boolean isCertificateRequired() 
    {
		return false;
	}

    /**
     * @see net.jradius.client.auth.RadiusAuthenticator#getAuthName()
     */
    public String getAuthName()
    {
        return NAME;
    }
    
    /**
     * @param tunneledAttributes The tunneledAttributes to set.
     */
    public void setTunneledAttributes(AttributeList tunneledAttributes)
    {
        this.tunneledAttributes = tunneledAttributes;
    }
    
    /**
     * @see net.jradius.client.auth.RadiusAuthenticator#setupRequest(net.jradius.client.RadiusClient, net.jradius.packet.RadiusPacket)
     */
    public void setupRequest(RadiusClient c, RadiusPacket p) throws RadiusException
    {
        super.setupRequest(c, p);
        tunnelRequest = new AccessRequest(tunneledAttributes);
        AttributeList attrs = tunnelRequest.getAttributes();
        if (attrs.get(Attr_UserName.TYPE) == null) attrs.add(username);
        if (attrs.get(Attr_UserPassword.TYPE) == null) attrs.add(password);
        tunnelAuth.setupRequest(c, tunnelRequest);
        if (!(tunnelAuth instanceof PAPAuthenticator)) // do not encode pap password
        {
            tunnelAuth.processRequest(tunnelRequest);
        }
    }
    
    protected boolean doTunnelAuthentication(byte id, byte[] in) throws RadiusException, SSLException
    {
        if (tunnelChallenge != null && in != null)
        {
            AttributeList list = tunnelChallenge.getAttributes();
            list.clear();
            
            ByteBuffer buffer = ByteBuffer.wrap(in);
            
            diameterFormat.unpackAttributes(list, buffer, buffer.limit());
            if (tunnelAuth instanceof EAPAuthenticator && tunnelChallenge.findAttribute(Attr_EAPMessage.TYPE) == null)
                tunnelAuth.setupRequest(client, tunnelRequest);
            else
                tunnelAuth.processChallenge(tunnelRequest, tunnelChallenge);
        }
        else tunnelChallenge = new AccessChallenge();

        ByteBuffer buffer = ByteBuffer.allocate(1500);
        diameterFormat.packAttributeList(tunnelRequest.getAttributes(), buffer, true);

        putAppBuffer(buffer.array(), 0, buffer.position());
        RadiusLog.debug("Tunnel Request:\n" + tunnelRequest.toString());
        return true;
    }

    public String getInnerProtocol()
    {
        return innerProtocol;
    }

    public void setInnerProtocol(String innerProtocol)
    {
        this.innerProtocol = innerProtocol;
    }
}
