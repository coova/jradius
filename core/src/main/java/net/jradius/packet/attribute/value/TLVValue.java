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

import java.io.Serializable;
import java.nio.ByteBuffer;

import net.jradius.packet.attribute.AttributeList;

public class TLVValue extends OctetsValue
{
	private static final long serialVersionUID = 1L;
	private TLVFormat format;
	private AttributeList list;
	
	public TLVValue(long vendorId, int vsaType, AttributeList subAttributes) {
		format = new TLVFormat(vendorId, vsaType);
		list = subAttributes;
	}
	
	@Override
	public void getBytes(ByteBuffer buffer) {
		format.packAttributeList(list, buffer, false);
	}

	
    @Override
	public void copy(AttributeValue value) 
    {
    	TLVValue tlvValue = (TLVValue) value;
    	list.clear();
    	list.add(tlvValue.list);
	}

	@Override
	public int getLength() {
		//XXX
		ByteBuffer b = ByteBuffer.allocate(1500);
		format.packAttributeList(list, b, true);
		return b.position();
	}

	@Override
	public Serializable getValueObject() {
		return super.getValueObject();
	}

	@Override
	public void setValue(byte[] b) {
		list.clear();
		if (b != null && b.length > 0)
		{
			ByteBuffer bb = ByteBuffer.wrap(b);
			format.unpackAttributes(list, bb, bb.limit());
		}
	}

	@Override
    public void setValue(byte[] b, int off, int len) {
		list.clear();
		if (b != null && len > 0)
		{
			ByteBuffer bb = ByteBuffer.wrap(b, off, len);
			format.unpackAttributes(list, bb, len);
		}
    }
    
	@Override
	public void setValueObject(Serializable o) 
	{
		super.setValueObject(o);
	}

	@Override
    public String toDebugString()
    {
    	return "["+list.toString().trim().replaceAll("\n", ", ")+"]";
    }

	@Override
    public String toString()
    {
    	return toDebugString();
    }
}
