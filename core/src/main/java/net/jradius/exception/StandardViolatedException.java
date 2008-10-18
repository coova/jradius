/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2007 David Bird <david@coova.com>
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

package net.jradius.exception;

import java.util.Iterator;
import java.util.List;

import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.RadiusAttribute;


/**
 * The Exception thrown by a RadiusStandard which found missing attributes.
 *
 * @author David Bird
 */
public class StandardViolatedException extends RadiusException
{
    private final Class standardClass;
    private final List missingAttributes;
    
    public StandardViolatedException(Class standardClass, List missing)
    {
        super("Standards Violation: " + standardClass.getName());
        this.standardClass = standardClass;
        this.missingAttributes = missing;
    }
    
    /**
     * @return Returns same as listAttribtues(", ")
     */
    public String listAttributes()
    {
    		return listAttributes(", ");
    }
    
    /**
     * Provides a listing of the names of the missing attributes
     * @param sep delimiter to use between attribute names
     * @return Returns the list of attribute names as a String
     */
    public String listAttributes(String sep)
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator i = missingAttributes.iterator(); i.hasNext(); )
        {
            Long type = (Long)i.next();
            RadiusAttribute a = AttributeFactory.newAttribute(type.longValue(), null);
            if (a != null) sb.append(sep).append(a.getAttributeName());
        }
        return sb.substring(sep.length());
    }
    
    /**
     * @return Returns the Class the generated the exception
     */
    public Class getStandardClass()
    {
        return standardClass;
    }
    
    /**
     * @return Returns the list of missing attributes (a list of Integers)
     */
    public List getMissingAttributes()
    {
        return missingAttributes;
    }
}
