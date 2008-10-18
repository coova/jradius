/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
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

import net.jradius.exception.RadiusException;
import net.jradius.log.JRadiusLogEntry;
import net.jradius.packet.RadiusPacket;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;
import net.jradius.session.JRadiusSession;

public class SessionLogHandler extends RadiusSessionHandler
{
    private boolean isReply = false;     // request or reply
    private boolean isOutbound = false;  // inbound or outbound
    
    public boolean handle(JRadiusRequest request) throws RadiusException
    {
        JRadiusSession session = request.getSession();
        if (session == null) return noSessionFound(request);

        RadiusPacket p = null;

        JRadiusLogEntry logEntry = session.getLogEntry(request);

        if (isReply) 
        {
            p = request.getReplyPacket();
        }
        else
        {
            p = request.getRequestPacket();
        }
        
        if (!isReply)
        {
            if (!isOutbound)
            {
                if (logEntry.getInboundRequest() == null)
                    logEntry.setInboundRequest(p.toString(false, true));
            }
            else
            {
                if (logEntry.getOutboundRequest() == null)
                    logEntry.setOutboundRequest(p.toString(false, true));
            }
        }
        else
        {
            if (!isOutbound)
            {
                if (logEntry.getInboundReply() == null)
                    logEntry.setInboundReply(p.toString(false, true));
            }
            else
            {
                if (logEntry.getOutboundReply() == null)
                    logEntry.setOutboundReply(p.toString(false, true));
            }
        }

	    request.setReturnValue(JRadiusServer.RLM_MODULE_UPDATED);
        return false;
    }
    
    public String getDirection()
    {
        return isOutbound ? "outbound" : "inbound";
    }

    public void setDirection(String direction)
    {
        isOutbound = "outbound".equals(direction);
    }

    public String getPacketType()
    {
        return isReply ? "reply" : "request";
    }

    public void setPacketType(String packetType)
    {
        isReply = "reply".equals(packetType) || "response".equals(packetType);
    }
}
