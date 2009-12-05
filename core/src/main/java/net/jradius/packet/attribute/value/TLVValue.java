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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

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
	public void getBytes(OutputStream out) throws IOException {
		byteValue = format.packAttributeList(list);
		super.getBytes(out);
	}

	@Override
	public int getLength() {
		byteValue = format.packAttributeList(list);
		return super.getLength();
	}

	@Override
	public Serializable getValueObject() {
		return super.getValueObject();
	}

	@Override
	public void setValue(byte[] b) {
		format.unpackAttributes(list, b, 0, b.length);
	}

	@Override
	public void setValueObject(Serializable o) {
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
