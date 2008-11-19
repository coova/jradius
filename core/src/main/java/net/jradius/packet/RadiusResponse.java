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

import net.jradius.packet.attribute.AttributeList;
import net.jradius.util.RadiusUtils;



/**
 * The RADIUS Response Packet
 *
 * @author David Bird
 */
public abstract class RadiusResponse extends RadiusPacket
{
    public RadiusResponse() 
    {
		super();
	}

	public RadiusResponse(int id, AttributeList list) 
	{
		super(list);
		setIdentifier(id);
	}

	/**
     * Calculates and compares the RADIUS Response Authenticator (per RFC 2865)
     * @param requestAuthenticator The Authenticator of the request
     * @sharedSecret
     * @return Returns true of the authenticators match
     */
    public boolean verifyAuthenticator(byte[] requestAuthenticator, String sharedSecret)
    {
        byte[] attribtues = RadiusFormat.getInstance().packAttributeList(getAttributes());
        byte[] hash = RadiusUtils.makeRFC2865ResponseAuthenticator(sharedSecret,
		        (byte)(getCode() & 0xff), (byte)(getIdentifier() & 0xff), 
		        (short)(attribtues.length + RADIUS_HEADER_LENGTH), 
		        requestAuthenticator, attribtues);
        return Arrays.equals(hash, getAuthenticator());
    }

    public void generateAuthenticator(byte[] requestAuthenticator, String sharedSecret)
    {
        byte[] attribtues = RadiusFormat.getInstance().packAttributeList(getAttributes());
		setAuthenticator(RadiusUtils.makeRFC2865ResponseAuthenticator( sharedSecret,
		        (byte)(getCode() & 0xff), (byte)(getIdentifier() & 0xff), 
		        (short)(attribtues.length + RADIUS_HEADER_LENGTH), 
		        requestAuthenticator, attribtues));
    }
}
