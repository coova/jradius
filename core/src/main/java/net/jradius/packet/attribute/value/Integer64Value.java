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
import java.math.BigInteger;
import java.nio.ByteBuffer;

import net.jradius.log.RadiusLog;
import net.jradius.packet.Format;

/**
 * The Integer64 attribute value. Uses a BigInteger as the underlying object since
 * this is an unsigned long in RADIUS. 
 */
public class Integer64Value extends AttributeValue
{
    private static final long serialVersionUID = 0L;
    
    private int length = 8;
    private BigInteger value;
    
    public Integer64Value() { }
    
    public Integer64Value(BigInteger i)
    {
        setValue(i);
    }
    
    public Integer64Value(byte[] bytes) {
        setValue(new BigInteger(1, bytes));
    }
    
    public void copy(AttributeValue value)
    {
    	Integer64Value iValue = (Integer64Value) value;
    	this.value = iValue.value;
    	this.length = iValue.length;
    }
    
    public int getLength()
    {
        return length;
    }
    
    public void getBytes(OutputStream out) throws IOException
    {
        if (value != null)
        {
            byte[] bytes = value.toByteArray();
            
            for (byte b : bytes) {
                out.write(b & 0xFF);
            }
        }
    }

    public void getBytes(ByteBuffer buffer)
    {
        if (value != null)
        {
            byte[] bytes = value.toByteArray();
            
            for (byte b : bytes) {
                Format.putUnsignedByte(buffer, b & 0xFF);
            }
        }
    }

    public void setValue(byte[] bytes)
    {
        if (bytes == null) return;
    	setValue(bytes, 0, bytes.length);
    }
    
    public void setValue(byte[] bytes, int off, int len)
    {
        if (bytes == null) return;
        if (len != 8) throw new RuntimeException("Wrong size for integer64 attribute");
        
        try
        {
            byte[] magnitude = new byte[8];                 
            System.arraycopy(bytes, off, magnitude, 0, len);
            value = new BigInteger(1, magnitude);
        }
        catch (Exception e)
        {
            RadiusLog.warn("Error during bean initialization [InitializingBean]", e);
        }
    }    
    
    public void setValue(String value)
    {
        setValue(new BigInteger(value));
    }

    public BigInteger getValue()
    {
        return value;
    }
    
    public String toString()
    {
        if (value != null)
        {
            return value.toString();
        }
        return "[Bad Integer64 Value]";
    }
    
    public String toXMLString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<int64>");
        if (value != null) 
        {
            sb.append(value.toString());
        }
        sb.append("</int64>");
        return sb.toString();
    }

    public Serializable getValueObject()
    {
        return value;
    }
    
    public void setValueObject(Serializable o)
    {
        if (o instanceof BigInteger)
        {
            setValue((BigInteger) o);
        } 
        else if (o instanceof String) 
        {
            setValue(new BigInteger((String) o));
        }
        else if (o instanceof byte[])
        {
            setValue(new BigInteger((byte[]) o));
        }
    }
    
    public void setValue(BigInteger value) throws NumberFormatException
    {
        if (!isValid(value)) throw new NumberFormatException("[bad integer64 value: " + String.valueOf(value) + "]");
        this.value = value;
    }

    public static boolean isValid(BigInteger value)
    {
        return value.signum() >= 0 && value.bitLength() <= 64;
    }
    
}
