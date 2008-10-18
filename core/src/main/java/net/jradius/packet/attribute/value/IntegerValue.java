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
 * The Integer attribute value. Uses a Long as the underlying object since
 * this is an unsigned long in RADIUS. 
 *
 * @author David Bird
 */
public class IntegerValue extends AttributeValue
{
    private static final long serialVersionUID = 0L;
    private int length = 4;
    protected Long integerValue;
    
    public IntegerValue() { }
    
    public IntegerValue(Long l)
    {
        integerValue = l;
    }
    
    public IntegerValue(Integer i)
    {
        setValue(i.longValue());
    }
    
    public IntegerValue(int i)
    {
        setValue(i);
    }
    
    public IntegerValue(long l)
    {
        setValue(l);
    }
    
    public int getLength()
    {
        return integerValue == null ? 0 : length;
    }
    
    public void setLength(int length)
    {
        this.length = length;
    }
    
    public void getBytes(OutputStream out) throws IOException
    {
        if (integerValue != null)
        {
            long longValue = integerValue.longValue();
            
            if (length == 4)
            {
                out.write((int)((longValue >> 24) & 0xFF));
                out.write((int)((longValue >> 16) & 0xFF));
            }
            
            if (length >= 2)
            {
                out.write((int)((longValue >>  8) & 0xFF));
            }

            out.write((int)(longValue & 0xFF));
        }
    }

    public void setValue(byte[] b)
    {
        if (b == null) return;
        try
        {
            switch(b.length)
            {
                case 1: // it's really a byte
                {
                    length = 1;
                    integerValue = new Long((int)b[0]&0xFF);
                }
                break;
                
                case 2:
                {
                    length = 2;
                    long longValue = 
                        (long)((int)b[0] & 0xFF) <<  8 | 
                        (long)((int)b[1] & 0xFF);
        
                    integerValue = new Long(longValue);
                }
                break;

                case 4:
                {
                    long longValue = 
                        (long)((int)b[0] & 0xFF) << 24 | 
                        (long)((int)b[1] & 0xFF) << 16 | 
                        (long)((int)b[2] & 0xFF) <<  8 | 
                        (long)((int)b[3] & 0xFF);
        
                    integerValue = new Long(longValue);
                }
                break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void setValue(String v)
    {
        setValue(Long.parseLong(v));
    }
    
    public Long getValue()
    {
        return integerValue;
    }
    
    public String toString()
    {
        if (integerValue != null)
        {
            return integerValue.toString();
        }
        return "[Bad Integer Value]";
    }
    
    public String toXMLString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<int>");
        if (integerValue != null) 
        {
            sb.append(integerValue);
        }
        sb.append("</int>");
        return sb.toString();
    }

    public void setLong(Long l)
    {
        this.integerValue = l;
    }
    
    public Serializable getValueObject()
    {
        return integerValue;
    }
    
    public void setValueObject(Serializable o)
    {
		if (o instanceof Long)
		{
			setLong((Long)o);
		}
		else if (o instanceof Number)
		{
			setLong(new Long(((Number)o).longValue()));
		}
		else
		{
			setLong(new Long(Long.parseLong(o.toString())));
		}
    }
    
    public void setValue(long l) throws NumberFormatException
    {
        if (isValid(l) == false) throw new NumberFormatException("[bad unsigned integer value: " + String.valueOf(l) + "]");
        integerValue = new Long(l);
    }

    public static boolean isValid(long l)
    {
        if ((l < 0L) || (l > 4294967295L)) return false;
        return true;
    }
}
