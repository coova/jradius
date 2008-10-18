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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.jradius.exception.UnknownAttributeException;

/**
 * Represents the Attribute List of a packet. Supports singleton
 * and lists of attribute values (building packets with serverl
 * of the same attribute).
 *
 * @author David Bird
 */
public class AttributeList implements Serializable
{
    private static final long serialVersionUID = 0L;
    private LinkedList attributeOrderList;
    private Map attributeMap;

    /**
     * Default constructor
     */
    public AttributeList()
    {
        attributeMap = new LinkedHashMap();
        attributeOrderList = new LinkedList();
    }
    
    /**
     * Add an attribute list to this attribute list
     * 
     * @param list The attribute list to add
     */
    public void add(AttributeList list) 
    { 
        if (list != null) 
        {
            attributeMap.putAll(list.getMap()); 
            attributeOrderList.addAll(list.getAttributeList());
        }
    }
    
    /**
     * Add an attribute, defaulting to overwriting
     * 
     * @param a The attribute to add
     */
    public void add(RadiusAttribute a) 
    { 
        add(a, true); 
    }
    
    /**
     * Add an attribute with option to overwrite. If overwrite is false,
     * multiple of the same attribute can be added (building a list)
     * 
     * @param a
     * @param overwrite
     */
    public void add(RadiusAttribute a, boolean overwrite)
    {
        Long key = new Long(a.getFormattedType());
        Object o = attributeMap.get(key);
        attributeOrderList.add(a);
        if (o == null || overwrite)
        {
        		if (o != null) removeFromList(o);
        		attributeMap.put(key, a);
        }
        else
        {
            // If we already have this attribute and are not
            // overwriting, then we create a list of attribtues.
            if (o instanceof LinkedList)
            {
                ((LinkedList)o).add(a);
            }
            else
            {
                LinkedList l = new LinkedList();
                l.add(o); l.add(a);
                attributeMap.put(key, l);
            }
        }
    }

    /**
     * Removes attribute(s) by type
     * @param a RadiusAttribute to remove
     */
    public void remove(RadiusAttribute a)
    {
        remove(a.getFormattedType());
    }

    /**
     * Removes attribute(s) by type
     * @param attributeType The attribute type to remove
     */
    public void remove(long attributeType)
    {
        Long key = new Long(attributeType);
        Object o = attributeMap.remove(key);
        if (o instanceof LinkedList)
        {
        		for (Iterator i = ((LinkedList)o).iterator(); i.hasNext(); )
        			removeFromList(i.next());
        }
        else removeFromList(o);
    }
    
    public void clear()
    {
    		attributeMap.clear();
    		attributeOrderList.clear();
    }

    private void removeFromList(Object o)
    {
    		Object ol[] = attributeOrderList.toArray();
    		for (int i = 0; i < ol.length; i++)
    		{
    			if (ol[i] == o)
    			{
    				attributeOrderList.remove(i);
    				return;
    			}
    		}
    }
    
    /**
     * @return Returns the number of attributes in the list
     */
    public int getSize()
    {
        return attributeOrderList.size();
    }
    
    /**
     * Removes all unknown (not in the configured JRadius Dictionary) attribtues.
     */
    public void removeUnknown()
    {
        List list = getAttributeList();
        for (Iterator i = list.iterator(); i.hasNext();)
        {
            RadiusAttribute a = (RadiusAttribute)i.next();
            if (a instanceof UnknownAttribute) remove(a);
        }
    }
    
    /**
     * @param type The type of attribute to retrieve
     * @param single True if a only a single attribute can be returned;
     * false if a List of attributes is also ok
     * @return Returns either s single attribute, a list of attributes, or null
     */
    public Object get(long type, boolean single) 
    {
        Long key = new Long(type);
        Object o = attributeMap.get(key);
        if (o == null || !(o instanceof LinkedList))
        {
            return o;
        }
        LinkedList l = (LinkedList)o;
        return (single ? l.get(0) : o);
    }
    
    public RadiusAttribute get(long type) { return (RadiusAttribute)get(type, true); }

    public Object get(String name, boolean single) throws UnknownAttributeException
    {
        return get(AttributeFactory.getTypeByName(name), single);
    }

    public RadiusAttribute get(String name) throws UnknownAttributeException
    {
        return (RadiusAttribute)get(AttributeFactory.getTypeByName(name), true);
    }

    /**
     * Get all attributes of a certain type returned at an array
     * @param type The type of attribute to find
     * @return Returns an array of all attributes found of a certain type
     */
    public Object[] getArray(long type)
    {
        Long key = new Long(type);
        return toArray(attributeMap.get(key));
    }
    
    public String toString(boolean nonStandardAttrs, boolean unknownAttrs)
    {
        StringBuffer sb = new StringBuffer();
        Iterator i = attributeOrderList.iterator();
        while (i.hasNext())
        {
        		RadiusAttribute attr = (RadiusAttribute)i.next();
        		if (!nonStandardAttrs && attr.attributeType > 256) continue;
        		if (!unknownAttrs && attr instanceof UnknownAttribute) continue;
        		sb.append(attr.toString()).append("\n");
        }
        return sb.toString();
    }
    
    public String toString()
    {
        return toString(true, true);
    }
    
    /**
     * Returns the attribute hash as a list
     * @return Returns a List of all attributes
     */
    public List getAttributeList()
    {
        return attributeOrderList;
    }
    
    /**
     * @return Returns the attribute map
     */
    public Map getMap()
    {
        return attributeMap;
    }
    
    /**
     * Returns an attribute or list of attributes as an array
     * @param o The single attribute or LinkedList of attributes
     * @return Returns an array of RadiusAttributes
     */
    private Object[] toArray(Object o)
    {
        if (o == null) return null;
        
        Object ol[];

        if (o instanceof LinkedList)
        {
            ol = ((LinkedList)o).toArray();
        }
        else
        {
            ol = new Object[1];
            ol[0] = o;
        }
        return ol;
    }
}
