/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (C) 2006-2007 David Bird <david@coova.com>
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
 * The "Named Value" attribute value (Integer enumerated attributes)
 *
 * @author David Bird
 */
public class NamedValue extends IntegerValue
{
    private static final long serialVersionUID = 0L;

    public interface NamedValueMap 
    {
        public String getNamedValue(Long l);
        public Long getNamedValue(String s);
        public Long[] getKnownValues();
    }
    
    private NamedValueMap valueMap = null;

    public NamedValue(NamedValueMap map)
    {
        valueMap = map;
    }
    
    public NamedValue(NamedValueMap map, String s)
    {
        valueMap = map;
        setValue(s);
    }

    public NamedValue(NamedValueMap map, Long l)
    {
        valueMap = map;
        setValue(l);
    }

    public NamedValue(NamedValueMap map, Integer i)
    {
        valueMap = map;
        setValue(i);
    }

    public NamedValue(Integer i)
    {
        setValue(i);
    }

    public void setValue(String s)
    {
        Long i = valueMap.getNamedValue(s);
        if (i != null)
        {
            this.integerValue = i;
        }
        else
        {
            System.err.println("Error: invalid NamedValue string value: " + s);
        }
    }
    
    public void setValue(Number l)
    {
        this.integerValue = new Long(l.longValue());
    }
    
	public void setValueObject(Serializable o) 
	{
		if (o instanceof Number)	
		{
			setValue((Number)o);
		}
		else
		{
			setValue(o.toString());
		}
	}

	public String getValueString()
    {
        return valueMap.getNamedValue(integerValue);
    }
    
    public NamedValueMap getMap() 
    { 
        return valueMap; 
    }
    
    public String toString() 
    { 
        String s = valueMap.getNamedValue(integerValue);
        if (s != null) return s;
        return "Unknown-" + integerValue;
    }
}
