/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import net.jradius.exception.RadiusException;
import net.jradius.exception.UnknownAttributeException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.attribute.RadiusAttribute.Operator;

/**
 * The Attribute Factor. This factor builds the RADIUS attributes
 * based on configured dictionaries. 
 *
 * @author David Bird
 */
public final class AttributeFactory
{
    private static LinkedHashMap<Long, Class<?>> attributeMap = new LinkedHashMap<Long, Class<?>>();
    private static LinkedHashMap<Long, Class<?>> vendorMap = new LinkedHashMap<Long, Class<?>>();
    private static LinkedHashMap<Long, VendorValue> vendorValueMap = new LinkedHashMap<Long, VendorValue>();
    private static LinkedHashMap<String, Class<?>> attributeNameMap = new LinkedHashMap<String, Class<?>>();
    
    public static final class VendorValue
    {
        private Class<?> c;
        private Map<Long, Class<?>> typeMap;
        private Map<String, Class<?>> nameMap;

        public VendorValue(Class<?> c, LinkedHashMap<Long, Class<?>> t, Map<String, Class<?>> n) { this.c = c; typeMap = t; nameMap = n; }

        public Map<String, Class<?>> getAttributeNameMap() {
            return nameMap;
        }
        public Map<Long, Class<?>> getAttributeMap() {
            return typeMap;
        }
        public Class<?> getDictClass() {
            return c;
        }
    }
    
    /**
     * Load an attribute dictionary
     * @param className Name of the Java Class derived from AttributeDictionary
     * @return Returns true if loading of dictionary was successful
     */
    public static boolean loadAttributeDictionary(String className) 
    {
        try
        {
            Class<?> clazz = Class.forName(className);
            Object o = clazz.newInstance();
            return loadAttributeDictionary((AttributeDictionary)o);
        } 
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean loadAttributeDictionary(AttributeDictionary dict)
    {
        dict.loadAttributes(attributeMap);
        dict.loadAttributesNames(attributeNameMap);
        dict.loadVendorCodes(vendorMap);

        Iterator<Long> i = vendorMap.keySet().iterator();
        while (i.hasNext())
        {
            Long id = i.next();
            Class<?> c = vendorMap.get(id);
            try
            {
                LinkedHashMap<Long, Class<?>> typeMap = new LinkedHashMap<Long, Class<?>>();
                LinkedHashMap<String, Class<?>> nameMap = new LinkedHashMap<String, Class<?>>();
                VSADictionary vsadict = (VSADictionary)c.newInstance();
                vsadict.loadAttributes(typeMap);
                vsadict.loadAttributesNames(nameMap);
                vsadict.loadAttributesNames(attributeNameMap);
                vendorValueMap.put(id, new AttributeFactory.VendorValue(c, typeMap, nameMap));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return true;
    }
    
    /**
     * Parses a string to create a RadiusAttribute. Will either return the 
     * attribute, or throw an Exception.
     * @param src The source String
     * @return Returns the RadiusAttribute parsed from String
     * @throws RadiusException
     * @throws UnknownAttributeException
     */
    public static RadiusAttribute attributeFromString(String src) 
        throws RadiusException, UnknownAttributeException
    {
        String parts[] = src.split("=", 2);
        if (parts.length == 2)
        {
            String attribute = parts[0].trim();
            String value = parts[1].trim();
            
            char q = value.charAt(0);
            if (q == value.charAt(value.length() - 1) && (q == '\'' || q == '"'))
            {
                value = value.substring(1, value.length() - 1);
            }
            
            return newAttribute(attribute, value, "=");
        }
        throw new RadiusException("Syntax error for attributes: " + src);
    }

    public static void loadAttributesFromString(AttributeList list, String src, String delim, boolean beStrinct) throws RadiusException
    {
        StringTokenizer st = new StringTokenizer(src, delim);
        while (st.hasMoreTokens())
        {
            try
            {
                list.add(attributeFromString(st.nextToken()));
            }
            catch (RadiusException e)
            {
                if (beStrinct) throw(e);
            }
        }
    }

    /**
     * Creates a new RadiusAttribute
     * @param vendor The VendorID of the attribute (if one)
     * @param type The Attribute Type
     * @param value The Attribute Value
     * @param op The Attribute Operator
     * @return Returns the newly created RadiusAttribute
     */
    public static RadiusAttribute newAttribute(long vendor, long type, byte[] value, int op)
    {
        Class<?> c = null;
        RadiusAttribute attr = null;
        
        try
        {
            if (vendor > 1 || type == 26)
            {
                if (vendor < 1)
                {
                    ByteArrayInputStream bais = new ByteArrayInputStream(value);
                    DataInputStream input = new DataInputStream(bais);
                    vendor = RadiusFormat.readUnsignedInt(input);
                    type = RadiusFormat.readUnsignedByte(input);
                    int vsaLength = RadiusFormat.readUnsignedByte(input);
                    byte[] newValue = new byte[vsaLength - 2];
                    input.readFully(newValue);
                    input.close();
                    value = newValue;
                }

                VendorValue v = vendorValueMap.get(new Long(vendor));
         
                if (v != null)
                {
                		c = v.typeMap.get(new Long(type));
                }
          
                if (c != null)
                {
                		attr = (RadiusAttribute)c.newInstance();
                }
                else
                {
                		RadiusLog.warn("Unknown Vendor Specific Attribute: " + vendor+":"+type);
                		attr = new Attr_UnknownVSAttribute(vendor, type);
                }
            }
            else 
            {
                c = attributeMap.get(new Long(type));
                if (c != null)
                {
                		attr = (RadiusAttribute)c.newInstance();
                }
                else
                {
                		RadiusLog.warn("Unknown Attribute: " + type);
                		attr = new Attr_UnknownAttribute(type);
                }
            }
        
            if (value != null) attr.setValue(value);
            if (op > -1) attr.setAttributeOp(op);
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return attr;
    }

    /**
     * Creates a new RadiusAttribute
     * @param type The type of the attribute
     * @param value The value of the attribute
     * @return Returns the newly created RadiusAttribute
     */
    public static RadiusAttribute newAttribute(long type, byte[] value)
    {
        return newAttribute((type >> 16), type & 0xFF, value, -1);
    }

    /**
     * @param type The type of the attribute
     * @param value The value of the attribute
     * @return Returns the newly created AttributeList
     */
    public static AttributeList newAttributeList(long type, byte[] value)
    {
        AttributeList list = new AttributeList();
        addToAttributeList(list, type, value);
        return list;
    }

    /**
     * @param list The AttributeList to add to
     * @param type The type of the attribute
     * @param value The value of the attribute
     * @return Returns how many attributes created
     */
    public static int addToAttributeList(AttributeList list, long type, byte[] value)
    {
        int left = (value == null) ? 0 : value.length;
        int offset = 0;
        int cnt = 0;
        
        long vendor = (type >> 16);
        int maxlen = vendor > 0 ? 247 : 253;
        type = type & 0xFF;

        while (left > 0)
        {
            int len = maxlen;
            if (left < maxlen) len = left;
            byte b[] = new byte[len];
            System.arraycopy(value, offset, b, 0, len);
            list.add(AttributeFactory.newAttribute(vendor, type, b, Operator.ADD), false);
            offset += len;
            left -= len;
            cnt++;
        }
        return cnt;
    }

    /**
     * Create a RadiusAttribute by name
     * @param aName The name of the attribute to create
     * @return Returns the newly created RadiusAttribute
     * @throws UnknownAttributeException
     */
    public static RadiusAttribute newAttribute(String aName)
    	throws UnknownAttributeException
    {
        Class<?> c = attributeNameMap.get(aName);
        RadiusAttribute attr = null;

        if (c == null) 
            throw new UnknownAttributeException("Unknown attribute " + aName);

        try 
        {
            attr = (RadiusAttribute)c.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return attr;
    }

    /**
     * Create a new RadiusAttribute based on a AttributeDescription
     * @param desc The RadiusDescription
     * @return Returns the newly created RadiusAttribute
     * @throws UnknownAttributeException
     */
    public static RadiusAttribute newAttribute(AttributeDescription desc)
    	throws UnknownAttributeException
    {
        return newAttribute(desc.getName(), desc.getValue(), desc.getOp());
    }
    
    /**
     * Creates a new RadiusAttribute
     * @param aName The name of the attribute to create
     * @param aValue The value of the attribute
     * @param aOp The "operator" of the attribute
     * @return Returns the newly created RadiusAttribute
     * @throws UnknownAttributeException
     */
    public static RadiusAttribute newAttribute(String aName, String aValue, String aOp)
    	throws UnknownAttributeException
    {
        RadiusAttribute attr = newAttribute(aName);
        attr.setAttributeOp(aOp);
        attr.setValue(aValue);
        return attr;
    }

    /**
     * The the integer type of a RadiusAttribute by name
     * @param aName The name of the attribute
     * @return Returns the integer type of the attribute
     * @throws UnknownAttributeException
     */
    public static long getTypeByName(String aName)
    	throws UnknownAttributeException
    {
        Class<?> c = attributeNameMap.get(aName);
        RadiusAttribute attr = null;

        if (c == null) 
            throw new UnknownAttributeException("Unknown attribute " + aName);

        try
        {
            attr = (RadiusAttribute)c.newInstance();
            return attr.getType();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * @return Returns the attributeMap.
     */
    public static LinkedHashMap<Long, Class<?>> getAttributeMap() 
    {
        return attributeMap;
    }

    /**
     * @return Returns the attributeNameMap.
     */
    public static LinkedHashMap<String, Class<?>> getAttributeNameMap() 
    {
        return attributeNameMap;
    }

    /**
     * @return Returns the vendorMap.
     */
    public static LinkedHashMap<Long, Class<?>> getVendorMap() 
    {
    	return vendorMap;
    }

    /**
     * @return Returns the vendorValueMap.
     */
    public static LinkedHashMap<Long, VendorValue> getVendorValueMap() 
    {
    	return vendorValueMap;
    }
}
