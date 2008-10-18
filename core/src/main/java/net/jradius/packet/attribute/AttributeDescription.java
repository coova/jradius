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

package net.jradius.packet.attribute;


/**
 * A simple attribute description bean (stored as triplet of Strings: 
 * name, operator, and value). 
 *
 * @author David Bird
 */
public final class AttributeDescription
{
    private String name;
    private String op;
    private String value;
    
    /**
     * Default constructor
     */
    public AttributeDescription()
    {
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return Returns the op.
     */
    public String getOp()
    {
        return op;
    }
    
    /**
     * @param op The op to set.
     */
    public void setOp(String op)
    {
        this.op = op;
    }
    
    /**
     * @return Returns the value.
     */
    public String getValue()
    {
        return value;
    }
    
    /**
     * @param value The value to set.
     */
    public void setValue(String value)
    {
        this.value = value;
    }
}
