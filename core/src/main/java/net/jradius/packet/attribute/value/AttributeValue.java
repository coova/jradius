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

package net.jradius.packet.attribute.value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import net.jradius.log.RadiusLog;
import net.jradius.util.Hex;


/**
 * Base abstract class of all Attribute Value classes.
 *
 * @author David Bird
 */
public abstract class AttributeValue implements Serializable
{
    private static final long serialVersionUID = 0L;

    public abstract void getBytes(OutputStream io) throws IOException;

    public abstract void getBytes(ByteBuffer buffer);

    /**
     * The values of valueOffset and valueLength are only used by some value types, for others
     * they are will always be the same or meaningless. 
     * @param buffer
     * @param valueOffset
     * @param valueLength
     */
    public void getBytes(ByteBuffer buffer, int valueOffset, int valueLength)
    {
    	getBytes(buffer);
    }

    public byte[] getBytes()
    { 
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            this.getBytes(out);
            out.close();
        }
        catch (Exception e)
        {
            RadiusLog.error(e.getMessage(), e);
        }
        return out.toByteArray();
    }

    public int getLength() { return 0; }

    public Serializable getValueObject() { return null; }

    public abstract void setValue(byte[] b);

    public abstract void setValue(byte[] b, int off, int len);

    public abstract void setValueObject(Serializable o);

	public abstract void copy(AttributeValue value);

    public void setValue(String s) 
    { 
    	if (s.startsWith("0x"))
    	{
        	setValue(Hex.hexStringToByteArray(s.substring(2))); 
    	}
    	else
    	{
        	setValue(s.getBytes()); 
    	}
    }

    public String toString() 
    { 
    	return "[Binary Data]"; 
    }
    
    public String toDebugString() 
    { 
    	return toString(); 
    }
    
    public String toXMLString() { return ""; }
}
