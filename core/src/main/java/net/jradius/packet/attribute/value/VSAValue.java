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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * The Vendor Specific Attribute (VSA) value
 *
 * @author David Bird
 */
public abstract class VSAValue extends AttributeValue
{
    private int vendorID;
    private AttributeValue vsaValue = null;
    
    public VSAValue() { }
    
    public VSAValue(AttributeValue v)
    {
        vsaValue = v;
    }
  
    public void getBytes(DataOutputStream out) throws IOException
    {
        if (vsaValue != null)
        {
            out.writeInt(vendorID);
            vsaValue.getBytes(out);
        }
    }

    public int getLength()
    {
        if (vsaValue != null)
        {
            return vsaValue.getLength();
        }
        return 0;
    }

    public String toString()
    {
        return vsaValue.toString();
    }
    
    public String toXMLString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<vsa vendor=\"").append(vendorID).append("\">");
        if (vsaValue != null) 
        {
            sb.append(vsaValue.toXMLString());
        }
        sb.append("</vsa>");
        return sb.toString();
    }

    public int getVendorID()
    {
        return vendorID;
    }
    
    public void setVendorID(int vendorID)
    {
        this.vendorID = vendorID;
    }
    
    public Serializable getValueObject()
    {
        if (vsaValue != null)
        {
            return vsaValue.getValueObject();
        }
        return null;
    }
    
    public void setObjectValue(Serializable o)
    {
        if (vsaValue != null)
        {
            vsaValue.setValueObject(o);
        }
    }
}
