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

/**
 * Raw Octets attribute value
 *
 * @author David Bird
 */
public class OctetsValue extends AttributeValue
{
    private static final long serialVersionUID = 0L;
    protected byte[] byteValue = null;
    
    public OctetsValue() { }
    
    public OctetsValue(byte[] b)
    {
        byteValue = b;
    }
    
    public void getBytes(OutputStream out) throws IOException
    {
        if (byteValue != null)
        {
            out.write(byteValue);
        }
    }

    public int getLength()
    {
        return byteValue == null ? 0 : byteValue.length;
    }

    public void setValue(byte[] b)
    {
        byteValue = b;
    }
    
    public String toString()
    {
        return "[Binary Data (length="+(byteValue == null ? 0 : byteValue.length)+")]";
    }

    public Serializable getValueObject()
    {
        return byteValue;
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
