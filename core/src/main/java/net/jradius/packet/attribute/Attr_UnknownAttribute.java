/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2006 David Bird <david@coova.com>
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

package net.jradius.packet.attribute;

import net.jradius.packet.attribute.value.OctetsValue;

/**
 * @author David Bird
 */
public class Attr_UnknownAttribute extends RadiusAttribute implements UnknownAttribute
{
    private static final long serialVersionUID = 0L;
    public static final String NAME = "Unknown-Attribute";

    public void setup() {}

    public void setup(long type)
    {
        attributeName = NAME + "(" + type + ")";
        attributeType = type;
    }

    public Attr_UnknownAttribute(long type)
    {
        setup(type);
        attributeValue = new OctetsValue();
    }

    public Attr_UnknownAttribute(long type, OctetsValue v)
    {
        setup(type);
        attributeValue = v;
    }

    public Attr_UnknownAttribute(long type, byte[]  v)
    {
        setup(type);
        attributeValue = new OctetsValue(v);
    }
    
    public long getAttributeType() 
    {
        return attributeType;
    }

    public void setAttributeName(String attributeName)
    {
    		this.attributeName = attributeName;
    }
}
