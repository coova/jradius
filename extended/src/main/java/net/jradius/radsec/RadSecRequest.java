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
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;


/**
 * RadSec Request
 *
 * @author David Bird
 */
public class RadSecRequest extends JRadiusRequest
{
    private int type;
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
     * @return the request type
     */
    public int getType()
    {
        return type;
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
     * Set the type of the request
     * @param type
     */
    public void setType(int type)
    {
        this.type = type;
    }
        
    /**
     * @param returnValue The returnValue to set.
     */
    public void setReturnValue(int returnValue)
    {
        this.returnValue = returnValue;
    }
    
    public String getTypeString()
    {
        switch(getType())
        {
            case JRadiusServer.JRADIUS_authenticate: return "authenticate";
            case JRadiusServer.JRADIUS_authorize:    return "authorize";
            case JRadiusServer.JRADIUS_preacct:      return "preacct";
            case JRadiusServer.JRADIUS_accounting:   return "accounting";
            case JRadiusServer.JRADIUS_checksimul:   return "checksimul";
            case JRadiusServer.JRADIUS_pre_proxy:    return "pre_proxy";
            case JRadiusServer.JRADIUS_post_proxy:   return "post_proxy";
            case JRadiusServer.JRADIUS_post_auth:    return "post_auth";
            default:                                 return "UNKNOWN";
        }
    }
}
