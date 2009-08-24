/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2009 Coova Technologies, LLC <support@coova.com>
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

package net.jradius.radsec;

import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusNativeRequest;
import net.jradius.server.JRadiusServer;


/**
 * RadSec Request
 *
 * @author David Bird
 */
public class RadSecRequest extends JRadiusNativeRequest
{
    private RadiusPacket packets[];
    private AttributeList configItems;
    private int returnValue = JRadiusServer.RLM_MODULE_UPDATED;

    /**
     * @return the "config_items" of the request (FreeRADIUS "control" attributes)
     */
    public AttributeList getConfigItems()
    {
        return configItems;
    }
    
    /**
     * @return the request packet array
     */
    public RadiusPacket[] getPackets()
    {
        return packets;
    }
    
    /**
     * @return Returns the returnValue.
     */
    public int getReturnValue()
    {
        return returnValue;
    }

    /**
     * Set the "config_items" of the request (FreeRADIUS "control"
     * attributes)
     * @param configItems
     */
    public void setConfigItems(AttributeList configItems)
    {
        this.configItems = configItems;
    }
    
    /**
     * Set the packet array of the request
     * @param packets
     */
    public void setPackets(RadiusPacket[] packets)
    {
        this.packets = packets;
    }

    /**
     * @param returnValue The returnValue to set.
     */
    public void setReturnValue(int returnValue)
    {
        this.returnValue = returnValue;
    }
}
