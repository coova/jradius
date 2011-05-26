/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
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

import java.util.Arrays;

import net.jradius.exception.RadiusException;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.util.RadiusUtils;


/**
 * PAP (default) Authentication.
 * 
 * @author David Bird
 */
public class PAPAuthenticator extends RadiusAuthenticator 
{
    public static final String NAME = "pap";

    public String getAuthName()
    {
        return NAME;
    }
    
    public void processRequest(RadiusPacket p) throws RadiusException
    {
    	if (password == null) throw new RadiusException("no password given");

    	p.removeAttribute(password);
        
        RadiusAttribute attr;
        p.addAttribute(attr = AttributeFactory.newAttribute("User-Password"));
        attr.setValue(RadiusUtils.encodePapPassword(
    			password.getValue().getBytes(), 
	            // Create an authenticator (AccessRequest just needs shared secret)
    			p.createAuthenticator(null, 0, 0, client.getSharedSecret()), 
	            client.getSharedSecret()));
    }

	public static boolean verifyPassword(byte[] userPassword, byte[] requestAuthenticator, byte[] clearText, String sharedSecret) 
	{
		byte[] pw = RadiusUtils.encodePapPassword(clearText, requestAuthenticator, sharedSecret);
		return Arrays.equals(pw, userPassword);
	}
}
