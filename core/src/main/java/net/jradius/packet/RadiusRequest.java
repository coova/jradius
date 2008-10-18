/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006 David Bird <david@coova.com>
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

package net.jradius.packet;

import net.jradius.client.RadiusClient;
import net.jradius.packet.attribute.AttributeList;



/**
 * A Radius Request (either Access Request or Accounting Request)
 * 
 * @author David Bird
 */
public abstract class RadiusRequest extends RadiusPacket 
{
    protected RadiusClient client = null;
    
    /**
     * Default constructor
     */
    public RadiusRequest()
    {
    }

    /**
     * Constructor
     * @param client The client context to be used (when creating UDP packets)
     */
    public RadiusRequest(RadiusClient client)
    {
        this.client = client;
    }

    /**
     * Constructor
     * @param attributes The attributes to be used
     */
    public RadiusRequest(AttributeList attributes)
    {
        super(attributes);
    }

    /**
     * Constructor
     * @param client The client context to be used (when creating UDP packets)
     * @param attributes The attributes to be used
     */
    public RadiusRequest(RadiusClient client, AttributeList attributes)
    {
        super(attributes);
        this.client = client;
    }
}
