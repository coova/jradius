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

import net.jradius.exception.RadiusException;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.util.MSCHAP;
import net.jradius.util.RadiusRandom;


/**
 * MSCHAPv2 Authentication.
 * 
 * @author David Bird
 */
public class MSCHAPv2Authenticator extends RadiusAuthenticator 
{
    public static final String NAME = "mschapv2";
    
    public String getAuthName()
    {
        return NAME;
    }
    
    public void processRequest(RadiusPacket p) throws RadiusException
    {
        if (password == null) throw new RadiusException("Password required");
        p.removeAttribute(password);
        
        RadiusAttribute attr;
        byte authChallenge[] = RadiusRandom.getBytes(16);
        byte chapResponse[] = MSCHAP.doMSCHAPv2(username.getValue().getBytes(), password.getValue().getBytes(), authChallenge);

        p.addAttribute(attr = AttributeFactory.newAttribute("MS-CHAP-Challenge"));
        attr.setValue(authChallenge);
        
        p.addAttribute(attr = AttributeFactory.newAttribute("MS-CHAP2-Response"));
        attr.setValue(chapResponse);
    }
}
