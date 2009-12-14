/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2009 Coova Technologies, LLC.
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

package net.jradius.packet.attribute.value;

import java.nio.ByteBuffer;

import net.jradius.packet.Format;
import net.jradius.packet.attribute.RadiusAttribute;

public class TLVFormat extends Format
{
	int parentType;
	long vendorId;
	
	public TLVFormat(long vendor, int pt)
	{
		this.vendorId = vendor;
		this.parentType = pt;
	}

	/*
	@Override
	public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException 
	{
        AttributeValue attributeValue = a.getValue();
        writeUnsignedByte(out, (int)a.getType());
        writeUnsignedByte(out, attributeValue.getLength() + 2);
        attributeValue.getBytes(out);
	}

	@Override
	public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException 
	{
        ctx.attributeType = (readUnsignedByte(in) << 8) | (parentType & 0xFF);
        ctx.attributeLength = readUnsignedByte(in);
        ctx.vendorNumber = (int) vendorId;
        ctx.headerLength = 2;
		return 0;
	}
*/
	
	public int unpackAttributeHeader(ByteBuffer buffer, AttributeParseContext ctx) 
	{
        ctx.attributeType = (getUnsignedByte(buffer) << 8) | (parentType & 0xFF);
        ctx.attributeLength = getUnsignedByte(buffer);
        ctx.vendorNumber = (int) vendorId;
        ctx.headerLength = 2;
        
        ctx.attributeLength -= ctx.headerLength;
        
		return 2;
	}

	@Override
	public void packAttribute(ByteBuffer buffer, RadiusAttribute a) 
	{
        AttributeValue attributeValue = a.getValue();
        putUnsignedByte(buffer, (int) a.getType());
        putUnsignedByte(buffer, attributeValue.getLength() + 2);
        attributeValue.getBytes(buffer);
	}
}
