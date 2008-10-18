/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
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
public class DHCPFormat extends Format
{
    public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException
    {
        if (a instanceof VSAttribute) 
        { 
            packAttribute(out, (VSAttribute)a); 
        }
    }
    
    public void packAttribute(OutputStream out, VSAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        writeUnsignedByte(out, (int)a.getVsaAttributeType());
        writeUnsignedByte(out, attributeValue.getLength());
        attributeValue.getBytes(out);
    }

    public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException
    {
        ctx.attributeType = readUnsignedByte(in);
        ctx.attributeLength = readUnsignedByte(in);
        ctx.headerLength = 2;
        return 0;
    }
}
