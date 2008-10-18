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

import java.io.Serializable;

/**
 * The String attribute value
 *
 * @author David Bird
 */
public class StringValue extends OctetsValue
{
    private static final long serialVersionUID = 0L;
    
    public StringValue() { }
    
    public StringValue(String s)
    {
        byteValue = s.getBytes();
    }
    
    public String toString()
    {
        if (byteValue == null) return null;
        String stringValue = new String(byteValue);
        return stringValue.trim();
    }
    
    public String toXMLString()
    {
        String s = toString();
        StringBuffer sb = new StringBuffer();
        sb.append("<string>").append(s == null ? "" : s).append("</string>");
        return sb.toString();
    }

    public Serializable getValueObject()
    {
        if (byteValue == null) return byteValue;
        return new String(byteValue);
    }

    public void setString(String s)
    {
        byteValue = s.getBytes();
    }

    public void setValueObject(Serializable o)
    {
		if (o instanceof byte[])
		{
			super.setValueObject(o);
		}
		else
		{
			setString(o.toString());
		}
    }
}
