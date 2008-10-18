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

package net.jradius.handler.tlstunnel;

import net.jradius.dictionary.Attr_ProxyToRealm;
import net.jradius.dictionary.Attr_Realm;
import net.jradius.dictionary.Attr_StrippedUserName;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.exception.RadiusException;
import net.jradius.handler.PacketHandlerBase;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;
import net.jradius.session.JRadiusSession;

/**
 * TLS Tunnel Termination Pre-Accounting Handler
 * @author David Bird
 */
public class PreAcctHandler extends PacketHandlerBase
{
    public boolean handle(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        AttributeList ci = request.getConfigItems();
        
        JRadiusSession session = request.getSession();
        if (session == null) return false;
        
        String proxyToRealm = session.getProxyToRealm();
        	    
        if (proxyToRealm != null)
        {
	        /*
	         *  If this session was the result of a terminated EAP Tunnel,
	         *  then proxy accounting to the home realm after adjusting
	         *  the User-Name to that in the EAP Tunnel.
	         */
	        RadiusAttribute a;
	        if ((a = req.findAttribute(Attr_StrippedUserName.TYPE)) != null) req.removeAttribute(a);
	        if ((a = req.findAttribute(Attr_Realm.TYPE)) != null) req.removeAttribute(a);
	        req.overwriteAttribute(new Attr_UserName(session.getUsername() + "@" + session.getRealm()));
	        ci.add(new Attr_ProxyToRealm(proxyToRealm));
	        request.setReturnValue(JRadiusServer.RLM_MODULE_UPDATED);
	        return true;
	    }

        return false;
    }
}
