/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
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
import java.nio.ByteBuffer;

import net.jradius.util.Hex;

/**
 * Raw Octets attribute value
 *
 * @author David Bird
 */
public class OctetsValue extends AttributeValue
{
	private static final long serialVersionUID = 0L;
    
    protected byte[] byteValue;
    protected int byteValueOffset;
    protected int byteValueLength;
    
    public OctetsValue() { }
    
    public OctetsValue(byte[] b)
    {
        byteValue = b;
    }
    
	public void copy(AttributeValue value) 
    {
    	OctetsValue cValue = (OctetsValue) value;
    	this.byteValue = new byte[cValue.byteValueLength];
    	this.byteValueLength = cValue.byteValueLength;
    	this.byteValueOffset = 0;
    	if (this.byteValueLength > 0)
    		System.arraycopy(cValue.byteValue, cValue.byteValueOffset, this.byteValue, 0, this.byteValueLength);
	}

    public void getBytes(OutputStream out) throws IOException
    {
        if (byteValue != null)
        {
            out.write(byteValue, byteValueOffset, byteValueLength);
        }
    }

    public void getBytes(ByteBuffer buffer)
    {
        if (byteValue != null)
        {
        	buffer.put(byteValue, byteValueOffset, byteValueLength);
        }
    }

    public void getBytes(ByteBuffer buffer, int off, int len)
    {
        if (byteValue != null)
        {
        	buffer.put(byteValue, byteValueOffset + off, len);
        }
    }

    public int getLength()
    {
        return byteValueLength;
    }

    public void setValue(byte[] b)
    {
    	byteValue = b;
    	byteValueOffset = 0;
    	byteValueLength = b == null ? 0 : b.length;
    }
    
    public void setValue(byte[] b, int off, int len)
    {
        byteValue = b;
    	byteValueOffset = off;
    	byteValueLength = len;
    }
    
    public String toDebugString()
    {
    	return "[Binary Data: "+(byteValue == null ? "null" : "0x"+Hex.byteArrayToHexString(byteValue))+"]";
    }

    public String toString()
    {
    	return "[Binary Data (length="+(byteValue == null ? 0 : byteValueLength)+")]";
    }

    public Serializable getValueObject()
    {
    	if (byteValueLength == 0) return 0;
    	byte[] ret = new byte[byteValueLength];
    	System.arraycopy(byteValue, byteValueOffset, ret, 0, byteValueLength);
        return ret;
    }
    
    public void setValueObject(Serializable o)
    {
		if (o instanceof byte[])
		{
			setValue((byte[])o);
		}
		else
		{
			setValue(o.toString().getBytes());
		}
    }
}
