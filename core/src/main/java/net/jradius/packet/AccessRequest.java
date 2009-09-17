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

package net.jradius.packet;

import java.util.Arrays;

import net.jradius.client.RadiusClient;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.util.RadiusUtils;

/**
 *  The RADIUS Access-Request Packet.
 *  See http://www.iana.org/assignments/radius-types
 *
 * @author David Bird
 */
public class AccessRequest extends RadiusRequest
{
    public static final byte CODE = (byte)1;
    private static final long serialVersionUID = (long)CODE;

    public AccessRequest() 
    {
        code = CODE;
    }

    public AccessRequest(RadiusClient client) 
    {
        super(client);
        code = CODE;
    }
    
    public AccessRequest(AttributeList attributes) 
    {
        super(attributes);
        code = CODE;
    }
    
    public AccessRequest(RadiusClient client, AttributeList attributes) 
    {
        super(client, attributes);
        code = CODE;
    }
    
    /**
     * Creates an Access-Request Authenticator
     * @see net.jradius.packet.RadiusPacket#createAuthenticator(byte[])
     */
    public byte[] createAuthenticator(byte[] attributes, String sharedSecret) 
    {
    	authenticator = RadiusUtils.makeRFC2865RequestAuthenticator(sharedSecret);
    	return authenticator;
    }
}
