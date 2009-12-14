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

import java.io.IOException;
import java.nio.ByteBuffer;

import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.VSAttribute;
import net.jradius.packet.attribute.value.AttributeValue;

/**
 * The Diameter Attribute Formatter
 * @author David Bird
 */
public class DiameterFormat extends Format
{
    // Diameter AVP Format Support

    /*
     *    0                   1                   2                   3
     *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |                           AVP Code                            |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |V M P r r r r r|                  AVP Length                   |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |                        Vendor-ID (opt)                        |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |    Data ...
     *   +-+-+-+-+-+-+-+-+
     */

    private static final byte AVP_VENDOR = (byte) 0x80;

    //private static final byte AVP_MANDITORY	= (byte)0x40;
    //private static final byte AVP_ENCRYPTION	= (byte)0x20;

    public void packAttribute(ByteBuffer  buffer, RadiusAttribute a) 
    {
        AttributeValue attributeValue = a.getValue();
        int length = attributeValue.getLength();
        int padding = ((length + 0x03) & ~(0x03)) - length;
        packHeader(buffer, a);
        attributeValue.getBytes(buffer);
        while (padding-- > 0) putUnsignedByte(buffer, 0);
    }

    public void packHeader(ByteBuffer buffer, RadiusAttribute a) 
    {
        if (a instanceof VSAttribute) 
        { 
            packHeader(buffer, (VSAttribute) a); 
            return;
        }

        AttributeValue attributeValue = a.getValue();
        putUnsignedInt(buffer, a.getType());
        putUnsignedByte(buffer, 0);
        putUnsignedByte(buffer, 0); // part of the AVP Length!
        putUnsignedShort(buffer, attributeValue.getLength() + 8);
    }

    public void packHeader(ByteBuffer buffer, VSAttribute a) 
    {
        AttributeValue attributeValue = a.getValue();
        putUnsignedInt(buffer, a.getVsaAttributeType());
        putUnsignedByte(buffer, AVP_VENDOR);
        putUnsignedByte(buffer, 0); // part of the AVP Length!
        putUnsignedShort(buffer, attributeValue.getLength() + 12);
        putUnsignedInt(buffer, a.getVendorId());
    }

    public int unpackAttributeHeader(ByteBuffer buffer, AttributeParseContext ctx) throws IOException
    {
        ctx.attributeType = (int) getUnsignedInt(buffer);

        long flen = getUnsignedInt(buffer);
        byte flags = (byte) ((flen >> 24) & 0xff);

        ctx.attributeLength = (int)(flen & 0x00ffffff);
        ctx.headerLength = 8;

        if ((flags & AVP_VENDOR) > 0)
        {
            ctx.vendorNumber = (int)getUnsignedInt(buffer);
            ctx.headerLength += 4;
        }

        ctx.padding = (int) (((ctx.attributeLength + 0x03) & ~(0x03)) - ctx.attributeLength);

        return 0;
    }
}
