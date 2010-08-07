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

import javax.net.ssl.SSLException;

import net.jradius.client.RadiusClient;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.exception.RadiusException;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;


/**
 * EAP-TTLS Authentication. 
 * 
 * @author David Bird
 */
public class PEAPAuthenticator extends EAPTLS2Authenticator 
{
    public static final String NAME = "peap";
    private EAPAuthenticator tunnelAuth;
    private RadiusPacket tunnelRequest;
    
    public PEAPAuthenticator()
    {
        setEAPType(EAP_PEAP);
    }
    
    /* (non-Javadoc)
     * @see net.jradius.client.auth.EAPTLSAuthenticator#init()
     */
    protected void init() throws RadiusException
    {
        super.init();
        tunnelAuth = new EAPMSCHAPv2Authenticator(true);
    }

    /**
     * @see net.jradius.client.auth.RadiusAuthenticator#getAuthName()
     */
    public String getAuthName()
    {
        return NAME;
    }
    
    protected boolean isCertificateRequired() 
    {
		return false;
	}

    /**
     * @see net.jradius.client.auth.RadiusAuthenticator#setupRequest(net.jradius.client.RadiusClient, net.jradius.packet.RadiusPacket)
     */
    public void setupRequest(RadiusClient c, RadiusPacket p) throws RadiusException
    {
        super.setupRequest(c, p);
        tunnelRequest = new AccessRequest();
        AttributeList attrs = tunnelRequest.getAttributes();
        if (attrs.get(Attr_UserName.TYPE) == null) attrs.add(username);
        if (attrs.get(Attr_UserPassword.TYPE) == null) attrs.add(password);
        tunnelAuth.setupRequest(c, tunnelRequest);
        tunnelAuth.processRequest(tunnelRequest);
    }
    
    protected boolean doTunnelAuthentication(byte id, byte[] in) throws RadiusException, SSLException
    {
        byte []out;

        if (in != null && in.length > 0)
        {
            out = tunnelAuth.doEAP(in);
        }
        else
        {
            out = tunnelAuth.eapResponse(EAP_IDENTITY, (byte)0, getUsername());
        }
        
        putAppBuffer(out);
        return true;
    }
}
