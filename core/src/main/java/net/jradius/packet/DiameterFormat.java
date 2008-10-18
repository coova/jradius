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
import java.io.InputStream;
import java.io.OutputStream;

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

    public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        int length = attributeValue.getLength();
        int padding = ((length + 0x03) & ~(0x03)) - length;
        if (a instanceof VSAttribute) 
            packHeader(out, (VSAttribute) a);
        else
            formatHeader(out, a);
        attributeValue.getBytes(out);
        while (padding-- > 0) out.write(0);
    }

    private void formatHeader(OutputStream out, RadiusAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        writeUnsignedInt(out, a.getType());
        writeUnsignedByte(out, 0);
        writeUnsignedByte(out, 0); // part of the AVP Length!
        writeUnsignedShort(out, attributeValue.getLength() + 8);
    }

    public void packHeader(OutputStream out, RadiusAttribute a) throws IOException
    {
        if (a instanceof VSAttribute) 
        { 
            packHeader(out, (VSAttribute)a); 
        }
    }

    public void packHeader(OutputStream out, VSAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        writeUnsignedInt(out, a.getVsaAttributeType());
        writeUnsignedByte(out, AVP_VENDOR);
        writeUnsignedByte(out, 0); // part of the AVP Length!
        writeUnsignedShort(out, attributeValue.getLength() + 12);
        writeUnsignedInt(out, a.getVendorId());
    }

    public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException
    {
        ctx.attributeType = (int)readUnsignedInt(in);

        long flen = readUnsignedInt(in);
        byte flags = (byte) ((flen >> 24) & 0xff);

        ctx.attributeLength = (int)(flen & 0x00ffffff);
        ctx.headerLength = 8;

        if ((flags & AVP_VENDOR) > 0)
        {
            ctx.vendorNumber = (int)readUnsignedInt(in);
            ctx.headerLength += 4;
        }

        ctx.padding = ((ctx.attributeLength + 0x03) & ~(0x03)) - ctx.attributeLength;

        return 0;
    }
}
