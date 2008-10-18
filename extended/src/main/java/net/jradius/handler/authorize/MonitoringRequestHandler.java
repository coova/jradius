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

package net.jradius.handler.authorize;

import net.jradius.dictionary.Attr_AuthType;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.handler.PacketHandlerBase;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;

/**
 * A handler to reply to monitoring requests. 
 * 
 * Requests are sent back an AccessReject with an configurable Reply-Message to indicate
 * a positive reply.
 * 
 * @author David Bird
 */
public class MonitoringRequestHandler extends PacketHandlerBase
{
    private String replyMessage;
    private String username;
    private String nasid;
    
    /* (non-Javadoc)
     * @see net.jradius.handler.PacketHandler#handle(net.jradius.server.JRadiusRequest)
     */
    public boolean handle(JRadiusRequest request) throws Exception
    {
        RadiusPacket req = request.getRequestPacket();
        RadiusPacket rep = request.getReplyPacket();
        AttributeList ci = request.getConfigItems();
        
        String u = (String) req.getAttributeValue(Attr_UserName.TYPE);
        String n = (String) req.getAttributeValue(Attr_NASIdentifier.TYPE);
        
        if ((username != null && username.equals(u)) || 
            (nasid != null && nasid.equals(n)))
        {
            if (request.getType() == JRadiusServer.JRADIUS_authorize)
            {
                // Reject the user (which should be fine for monitoring)
                // and stop processing the current handler chain
                RadiusLog.info("Answering monitoring request: {User-Name = " + username + ", NAS-Identifier = " + nasid + "}");
                if (replyMessage != null)
                {
                    rep.addAttribute(new Attr_ReplyMessage(replyMessage));
                }
                ci.add(new Attr_AuthType(Attr_AuthType.Reject));
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * @return Returns the nasid.
     */
    public String getNasid()
    {
        return nasid;
    }
    /**
     * @param nasid The nasid to set.
     */
    public void setNasid(String nasid)
    {
        this.nasid = nasid;
    }
    /**
     * @return Returns the replyMessage.
     */
    public String getReplyMessage()
    {
        return replyMessage;
    }
    /**
     * @param replyMessage The replyMessage to set.
     */
    public void setReplyMessage(String replyMessage)
    {
        this.replyMessage = replyMessage;
    }
    /**
     * @return Returns the username.
     */
    public String getUsername()
    {
        return username;
    }
    /**
     * @param username The username to set.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }
}
