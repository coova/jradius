/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2006 David Bird <david@coova.com>
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

package net.jradius.example;

import net.jradius.dictionary.Attr_AuthType;
import net.jradius.dictionary.Attr_EAPMessage;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.vsa_chillispot.Attr_ChilliSpotConfig;
import net.jradius.dictionary.vsa_freeradius.Attr_FreeRADIUSProxiedTo;
import net.jradius.exception.RadiusException;
import net.jradius.handler.PacketHandlerBase;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;

/**
 * An example using CoovaAP (and Coova Chilli) with WPA authentication. It allows
 * for "guest" users, those not presenting valid credentials, WPA access, but subject
 * to a captive-portal/walled-garden. Users with valid credentials get immediate Internet
 * access.
 * 
 * @author David Bird
 */
public class WPACaptivePortal extends PacketHandlerBase
{
    public boolean handle(JRadiusRequest request)
    {
        try
        {
            /*
             * Gather some information about the JRadius request
             */
            AttributeList ci = request.getConfigItems();
            RadiusPacket req = request.getRequestPacket();
            RadiusPacket rep = request.getReplyPacket();

            /*
             * Find the username in the request packet
             */
            String username = (String)req.getAttributeValue(Attr_UserName.TYPE);

            if (rep instanceof AccessAccept)
            {
                RadiusLog.info("Allowing WPA access for username: " + username);
            }
            else
            {   // Is an Access-Reject
                if ("allow-wpa-guests".
                        equals((String)req.getAttributeValue(Attr_ChilliSpotConfig.TYPE))) 
                {   // Allowing WPA "guest" access
                    if (req.findAttribute(Attr_EAPMessage.TYPE) != null)
                    {   // Is EAP (802.1x)
                        if (req.findAttribute(Attr_FreeRADIUSProxiedTo.TYPE) != null)
                        {   // Is the inner request, TLS termianted
                            rep = new AccessAccept();
                            rep.addAttribute(new Attr_ChilliSpotConfig("require-uam-auth"));
                            request.setReplyPacket(rep);

                            ci.add(new Attr_AuthType("Accept"));
                            request.setReturnValue(JRadiusServer.RLM_MODULE_UPDATED);

                            RadiusLog.error("Allowing Guest WPA access for username: " + username);
                            return true;
                        }
                    }
                }
                RadiusLog.info("Authentication failed for username: " + username);
            }
        }
        catch (RadiusException e)
        {
            e.printStackTrace();
        }
        
        request.setReturnValue(JRadiusServer.RLM_MODULE_UPDATED);
        return false;
    }
}
