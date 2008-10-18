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
public class Attr_UnknownVSAttribute extends VSAttribute implements UnknownAttribute
{
    public static final long serialVersionUID = 0L;
    public static final String NAME = "Unknown-VSAttribute";

    public void setup() {}
    
    public void setup(long vendorId, long vsaAttributeType)
    {
        attributeName = NAME + "(" + vendorId + ":" + vsaAttributeType + ")";
        attributeType = 26;
        this.vendorId = vendorId;
        this.vsaAttributeType = vsaAttributeType;
    }

    public Attr_UnknownVSAttribute(long vendorId, long vsaAttributeType)
    {
        setup(vendorId, vsaAttributeType);
        attributeValue = new OctetsValue();
    }

    public Attr_UnknownVSAttribute(long vendorId, long vsaAttributeType, OctetsValue v)
    {
        setup(vendorId, vsaAttributeType);
        attributeValue = v;
    }

    public Attr_UnknownVSAttribute(long vendorId, long vsaAttributeType, byte[]  v)
    {
        setup(vendorId, vsaAttributeType);
        attributeValue = new OctetsValue(v);
    }
    
    public long getAttributeType() 
    {
        return ((vendorId & 0xFFFF) << 16) | vsaAttributeType;
    }
    
    public void setAttributeName(String attributeName)
    {
    		this.attributeName = attributeName;
    }
}
