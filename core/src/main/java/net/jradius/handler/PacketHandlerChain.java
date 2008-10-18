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

package net.jradius.handler;


import net.jradius.handler.chain.JRCommand;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;

import org.apache.commons.chain.Catalog;

/**
 * The Catalog Handler is a simple PacketHandler that delegates
 * the request to a type specific command within a chain catalog, 
 * if one is found. In the case of accounting, if the 'accounting'
 * chain is not found, accounting status specific chains are tried
 * (e.g. acct-start, acct-interim, and acct-stop).
 * @author David Bird
 */
public class PacketHandlerChain extends EventHandlerChain implements PacketHandler
{
    public boolean handle(JRadiusEvent event) throws Exception
    {
        return handle((JRadiusRequest)event);
    }

    public boolean handle(JRadiusRequest request) throws Exception
    {
        Catalog catalog = getCatalog();
        if (catalog != null)
        {
            JRCommand command;
            
            if (request.getType() == JRadiusServer.JRADIUS_accounting)
            {
                RadiusPacket req = request.getRequestPacket();
                Long i = (Long) req.getAttributeValue(AttributeDictionary.ACCT_STATUS_TYPE);
                if (i != null)
                {
                    String com = "other_accounting";
                    switch(i.intValue())
                    {
                    	case AccountingRequest.ACCT_STATUS_START: com = "start_accounting"; break;
                    	case AccountingRequest.ACCT_STATUS_STOP: com = "stop_accounting"; break;
                    	case AccountingRequest.ACCT_STATUS_INTERIM: com = "interim_accounting"; break;
                        case AccountingRequest.ACCT_STATUS_ACCOUNTING_ON: com = "accounting_on"; break;
                        case AccountingRequest.ACCT_STATUS_ACCOUNTING_OFF: com = "accounting_off"; break;
                    }
                    command = (JRCommand)catalog.getCommand(com);
                    if (command != null) return execute(command, request);
                }
            }

            command = (JRCommand)catalog.getCommand(request.getTypeString());
            return execute(command, request);
        }
        return true;
    }
}
