/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (C) 2007-2008 David Bird <dbird@acm.org>
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

package net.jradius.handler;

import net.jradius.dictionary.Attr_AcctInputGigawords;
import net.jradius.dictionary.Attr_AcctInputOctets;
import net.jradius.dictionary.Attr_AcctInputPackets;
import net.jradius.dictionary.Attr_AcctOutputGigawords;
import net.jradius.dictionary.Attr_AcctOutputOctets;
import net.jradius.dictionary.Attr_AcctOutputPackets;
import net.jradius.dictionary.Attr_CleartextPassword;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusRequest;


/**
 * Extended from PacketHandler, this type of handler is required to have a JRadiusSession
 * @author David Bird
 */
public abstract class RadiusSessionHandler extends PacketHandlerChain
{
    public static final String ClassPrefix = "JRADIUS-CLASS:";

    protected boolean noSessionFound(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        RadiusLog.error("No session found for packet:\n" + req.toString());
        return true;
    }

    protected void setPlainTextPassword(JRadiusRequest request, String password)
    {
        setPlainTextPassword(request, password, true);
    }

    protected void setPlainTextPassword(JRadiusRequest request, String password, boolean overwrite)
    {
        AttributeList ci = request.getConfigItems();
        ci.add(AttributeFactory.newAttribute(Attr_CleartextPassword.TYPE, password), overwrite);
        ci.add(AttributeFactory.newAttribute(Attr_UserPassword.TYPE, password), overwrite);
    }

    protected void reverseAccounting(RadiusPacket req) throws RadiusException
    {
        Long octetsIn = (Long)req.getAttributeValue(Attr_AcctInputOctets.TYPE);
        Long octetsOut = (Long)req.getAttributeValue(Attr_AcctOutputOctets.TYPE);
        
        Long gigaIn = (Long)req.getAttributeValue(Attr_AcctInputGigawords.TYPE);
        Long gigaOut = (Long)req.getAttributeValue(Attr_AcctOutputGigawords.TYPE);

        Long packetsIn = (Long)req.getAttributeValue(Attr_AcctInputPackets.TYPE);
        Long packetsOut = (Long)req.getAttributeValue(Attr_AcctOutputPackets.TYPE);
        
        if (octetsIn != null && octetsOut != null)
        {
            req.overwriteAttribute(AttributeFactory.newAttribute(Attr_AcctInputOctets.TYPE, octetsOut));
            req.overwriteAttribute(AttributeFactory.newAttribute(Attr_AcctOutputOctets.TYPE, octetsIn));
        }
        
        if (gigaIn != null && gigaOut != null)
        {
            req.overwriteAttribute(AttributeFactory.newAttribute(Attr_AcctInputGigawords.TYPE, gigaOut));
            req.overwriteAttribute(AttributeFactory.newAttribute(Attr_AcctOutputGigawords.TYPE, gigaIn));
        }
        
        if (packetsIn != null && packetsOut != null)
        {
            req.overwriteAttribute(AttributeFactory.newAttribute(Attr_AcctInputPackets.TYPE, packetsOut));
            req.overwriteAttribute(AttributeFactory.newAttribute(Attr_AcctOutputPackets.TYPE, packetsIn));
        }
    }
}
