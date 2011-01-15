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

package net.jradius.handler.proxy;

import java.util.Arrays;

import net.jradius.dictionary.Attr_Class;
import net.jradius.dictionary.Attr_State;
import net.jradius.handler.RadiusSessionHandler;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.server.JRadiusRequest;
import net.jradius.session.JRadiusSession;

/**
 * Rewrites the Class Attribute with the Session Class (for use in pre-proxy). This handler
 * works in cooperation with the PostAuthorizeClassHandler and will rewrite the out-going
 * Class attribute to be the appropriate value for the session (the value returned to us and
 * captured in the PostAuthorizeClassHandler)
 * 
 * @author David Bird
 * @see net.jradius.handler.authorize.PostAuthorizeClassHandler
 */
public class ProxyClassHandler extends RadiusSessionHandler
{
    public boolean handle(JRadiusRequest request) throws Exception
    {
        JRadiusSession session = request.getSession();
        if (session == null) return noSessionFound(request);
        
        System.err.println(this.getClass().getName());

        RadiusPacket req = request.getRequestPacket();
        
        byte[] packetClass = (byte[])req.getAttributeValue(Attr_Class.TYPE);
        byte[][] sessionClass = session.getRadiusClass();

        if (packetClass != null || sessionClass != null)
        {
            if (sessionClass == null)
            {
                session.addLogMessage(request, "Request has Class attribute when it should not");
            }
            else
            {
                session.addLogMessage(request, "Missing Class Attribute (added)");
            	req.removeAttribute(Attr_Class.TYPE);
            	for (byte[] a : sessionClass) {
            		req.addAttribute(AttributeFactory.newAttribute(Attr_Class.TYPE, a));
            	}
            }
        }
        
        return false;
    }
}
