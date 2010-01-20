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
import java.util.Date;

/**
 * The Date attribute value
 *
 * @author David Bird
 */
public class DateValue extends IntegerValue
{
    private static final long serialVersionUID = 0L;
    private Date dateValue;
    
    public DateValue() { }
    
    public DateValue(Date d)
    {
        dateValue = d;
    }
    
    public void copy(AttributeValue value)
    {
    	DateValue dValue = (DateValue) value;
    	this.integerValue = dValue.integerValue;
    	this.length = dValue.length;
    	this.dateValue = dValue.dateValue;
    }
    
    public void getBytes(OutputStream out) throws IOException
    {
        integerValue = new Long(dateValue.getTime() / 1000);
        super.getBytes(out);
    }
    
    public void setValue(byte[] b)
    {
        super.setValue(b);
        dateValue = new Date(integerValue.longValue() * 1000);
    }

    public void setValue(int i)
    {
        super.setValue(i);
        dateValue = new Date(integerValue.longValue() * 1000);
    }
    
    public void setValue(long l)
    {
        super.setValue(l);
        dateValue = new Date(integerValue.longValue() * 1000);
    }
 
    public String toString()
    {
        if (dateValue != null)
        {
            return dateValue.toString();
        }
        return "[Bad Date Value]";
    }
    
    public String toXMLString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<time>");
        if (dateValue != null) 
        {
            sb.append(dateValue.getTime());
        }
        sb.append("</time>");
        return sb.toString();
    }

    public void setDate(Date newDate)
    {
        this.dateValue = newDate;
        this.integerValue = new Long(this.dateValue.getTime() / 1000);
    }
    
    public Serializable getValueObject()
    {
        return dateValue;
    }

    public void setValueObject(Serializable o)
    {
        if (o instanceof Date)
        {
            setDate((Date)o);
        }
        else if (o instanceof Number)
        {
            setDate(new Date(((Number)o).longValue() * 1000));
        }
        else
        {
            setDate(new Date((Long.parseLong(o.toString())) * 1000));
        }
    }
}
