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

package net.jradius.freeradius;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.value.AttributeValue;


/**
 * Packs and Unpacks Radius Packets and Attributes for the FreeRADIUS Server.
 *
 * @author David Bird
 */
public class FreeRadiusFormat extends RadiusFormat
{
    private static final FreeRadiusFormat staticFormat = new FreeRadiusFormat();
    
    /**
     * @see net.jradius.packet.RadiusFormat#setAttributeBytes(net.jradius.packet.RadiusPacket, byte[])
     */
    public static void setAttributeBytes(RadiusPacket p, byte[] bAttributes)
    {
        int attributesLength = bAttributes.length;
        
        if (attributesLength > 0)
        {
            staticFormat.unpackAttributes(p.getAttributes(), bAttributes, 0, attributesLength);
        }
    }

    /**
     * @see net.jradius.packet.RadiusFormat#packHeader(java.io.OutputStream, net.jradius.packet.RadiusPacket, byte[])
     */
    public void packHeader(OutputStream out, RadiusPacket p, byte[] attributeBytes, String sharedSecret) throws IOException
    {
        writeUnsignedInt(out, p.getCode());
        writeUnsignedInt(out, p.getIdentifier());
        writeUnsignedInt(out, attributeBytes == null ? 0 : attributeBytes.length);
    }
    
    /**
     * @see net.jradius.packet.RadiusFormat#packHeader(java.io.OutputStream, net.jradius.packet.attribute.RadiusAttribute)
     */
    public void packHeader(OutputStream out, RadiusAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        writeUnsignedInt(out, a.getFormattedType());
        writeUnsignedInt(out, attributeValue.getLength());
        writeUnsignedInt(out, a.getAttributeOp());
    }
    
    /**
     * @see net.jradius.packet.RadiusFormat#unpackAttributeHeader(java.io.InputStream, net.jradius.packet.RadiusFormat.AttributeParseContext)
     */
    public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException
    {
	    ctx.attributeType = (int)readUnsignedInt(in);
	    ctx.attributeLength = (int)readUnsignedInt(in);
	    ctx.attributeOp = (int)readUnsignedInt(in);
		
        if (ctx.attributeType > (1 << 16))
        {
            // FreeRADIUS encodes the vendor number in the type
            // with: if (vendor) attr->attr |= (vendor << 16);
            ctx.vendorNumber = (ctx.attributeType >> 16) & 0xffff;
            ctx.attributeType &= 0xffff;
        }
        
        return 12;
    }
}
