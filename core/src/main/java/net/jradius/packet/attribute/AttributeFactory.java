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
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.jradius.exception.RadiusException;
import net.jradius.exception.UnknownAttributeException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.attribute.RadiusAttribute.Operator;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

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

    private static RadiusAttribute vsa(long vendor, long type) throws InstantiationException, IllegalAccessException
    {
    	RadiusAttribute attr = null;
        VendorValue v = vendorValueMap.get(new Long(vendor));
        Class<?> c = null;
	         
        if (v != null)
        {
        	c = v.typeMap.get(new Long(type));
        }
  
        if (c != null)
        {
        	attr = (RadiusAttribute) c.newInstance();
        }
        else
        {
        	RadiusLog.warn("Unknown Vendor Specific Attribute: " + vendor+":"+type);
        	attr = new Attr_UnknownVSAttribute(vendor, type);
        }
     
        return attr;
    }

    private static RadiusAttribute attr(long type) throws InstantiationException, IllegalAccessException
    {
    	RadiusAttribute attr = null;
        Class<?> c = attributeMap.get(new Long(type));
    	
        if (c != null)
        {
        	attr = (RadiusAttribute) c.newInstance();
        }
        else
        {
        	RadiusLog.warn("Unknown Attribute: " + type);
        	attr = new Attr_UnknownAttribute(type);
        }

        return attr;
    }
    
    private static KeyedObjectPool attributeObjectPool = new GenericKeyedObjectPool(new KeyedPoolableObjectFactory() 
    {
		public boolean validateObject(Object arg0, Object arg1) 
		{
			return true;
		}
		
		public void passivateObject(Object arg0, Object arg1) throws Exception 
		{
			RadiusAttribute a = (RadiusAttribute) arg1;
			a.recycled = true;
		}
		
		public Object makeObject(Object arg0) throws Exception 
		{
			RadiusAttribute a = newAttribute((Long) arg0);
			a.recyclable = true;
			a.recycled = false;
			return a;
		}
		
		public void destroyObject(Object arg0, Object arg1) throws Exception 
		{
		}
		
		public void activateObject(Object arg0, Object arg1) throws Exception 
		{
			RadiusAttribute a = (RadiusAttribute) arg1;
			a.recycled = false;
		}
		
	}, -1);

    public static RadiusAttribute newAttribute(Long key) throws Exception
    {
		RadiusAttribute a = null;

		long val = key.longValue();
		long vendor = val >> 16;
		long type = val & 0xFFFF;

		if (vendor != 0)
		{
			a = vsa(vendor, type);
		}
		else
		{
			a = attr(type);
		}

		// System.err.println("Created "+a.toString() + " " + key + " " + a.getFormattedType());
		
		return a;
    }
    
    public static RadiusAttribute newAttribute(Long key, Serializable value) 
    {
    	RadiusAttribute attr = null;
    	
    	try
    	{
    		attr = borrow(key);

    		if (attr == null)
	        {
    			attr = newAttribute(key);
	        }
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	attr.getValue().setValueObject(value);
        
        return attr;
    }

    public static RadiusAttribute copyAttribute(RadiusAttribute a)
    {
    	return copyAttribute(a, true);
    }
    
    public static RadiusAttribute copyAttribute(RadiusAttribute a, boolean pool)
    {
    	Long key = new Long(a.getFormattedType());
    	RadiusAttribute attr = null;
    	
    	try
    	{
    		if (pool)
    		{
    			attr = borrow(key);
    		}
    		
    		if (attr == null)
	        {
    			attr = newAttribute(key);
	        }
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	attr.getValue().copy(a.getValue());
        
        return attr;
    }
    
    public static RadiusAttribute borrow(Long key) throws NoSuchElementException, IllegalStateException, Exception
    {
    	RadiusAttribute attr = null;
    	
        if (attributeObjectPool != null)
        {
        	attr = (RadiusAttribute) attributeObjectPool.borrowObject(key);
        	// System.err.println("Borrowed "+attr.toString() + " " + key + " " + attr.getFormattedType());
        }

        return attr;
    }
    
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
    public static RadiusAttribute attributeFromString(String src) throws RadiusException, UnknownAttributeException
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
        RadiusAttribute attr = null;
        
        try
        {
            if (vendor > 1 || type == 26)
            {
            	boolean onWire = (vendor < 1);
                DataInputStream input = null;

                if (onWire)
                {
                	/*
                	 *  We are parsing an off-the-wire packet
                	 */
                    ByteArrayInputStream bais = new ByteArrayInputStream(value);
                    input = new DataInputStream(bais);
                    
                    vendor = RadiusFormat.readUnsignedInt(input);
                    type = RadiusFormat.readUnsignedByte(input);
                }

                Long key = new Long(vendor << 16 | type);
                
                attr = borrow(key);

                if (attr == null)
                {
                	attr = vsa(vendor, type);
                }
                
                if (onWire)
                {
                	VSAttribute vsa = (VSAttribute) attr;
                    int vsaLength = 0;
                    int vsaHeaderLen = 2;
                    switch (vsa.getLengthLength())
                    {
	                    case 1:
	                        vsaLength = RadiusFormat.readUnsignedByte(input);
	                        break;
	                    case 2:
	                        vsaLength = RadiusFormat.readUnsignedShort(input);
	                    	vsaHeaderLen ++;
	                        break;
	                    case 4:
	                        vsaLength = (int) RadiusFormat.readUnsignedInt(input);
	                    	vsaHeaderLen += 3;
	                        break;
                    }
                    if (vsa.hasContinuationByte)
                    {
                    	vsa.continuation = (short) RadiusFormat.readUnsignedByte(input);
                    	vsaHeaderLen ++;
                    }
                    byte[] newValue = new byte[vsaLength - vsaHeaderLen];
                    input.readFully(newValue);
                    input.close();
                    value = newValue;
                }
            }
            else 
            {
            	attr = borrow(type);

            	if (attr == null)
            	{
                	attr = attr(type);
                }
            }
        
            if (value != null) attr.setValue(value);
            else attr.setValue(new byte[] { });
            if (op > -1) attr.setAttributeOp(op);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return attr;
    }

    public static RadiusAttribute newAttribute(long vendor, long type, long len, int op, ByteBuffer buffer)
    {
    	RadiusAttribute attr = null;
        
        int valueLength = (int) len;
        
        try
        {
            if (vendor > 1 || type == 26)
            {
            	boolean needVendorAndType = (vendor < 1);
            	boolean needVendorType = (type < 1);

                if (needVendorAndType)
                {
                	vendor = RadiusFormat.getUnsignedInt(buffer);
                }
                if (needVendorAndType || needVendorType)
                {
                    type = RadiusFormat.getUnsignedByte(buffer);
                }

                Long key = new Long(vendor << 16 | type);
                
                attr = borrow(key);
                
                if (attr == null) 
                {
                	attr = vsa(vendor, type);
                }
                
                if (needVendorAndType || needVendorType)
                {
                	VSAttribute vsa = (VSAttribute) attr;
                    int vsaLength = 0;
                    int vsaHeaderLen = 2;

                    switch (vsa.getLengthLength())
                    {
	                    case 1:
	                        vsaLength = RadiusFormat.getUnsignedByte(buffer);
	                        break;
	                    case 2:
	                        vsaLength = RadiusFormat.getUnsignedShort(buffer);
	                    	vsaHeaderLen ++;
	                        break;
	                    case 4:
	                        vsaLength = (int) RadiusFormat.getUnsignedInt(buffer);
	                    	vsaHeaderLen += 3;
	                        break;
                    }

                    if (vsa.hasContinuationByte)
                    {
                    	vsa.continuation = (short) RadiusFormat.getUnsignedByte(buffer);
                    	vsaHeaderLen ++;
                    }

                    valueLength = vsaLength - vsaHeaderLen;
                }
            }
            else 
            {
            	attr = borrow(type);

            	if (attr == null)
                {
                	attr = attr(type);
                }
            }
        
            if (valueLength > 0) 
            {
            	attr.setValue(buffer.array(), buffer.position(), valueLength);
            	buffer.position(buffer.position() + valueLength);
            }
            else 
            {
            	attr.setValue(null, 0, 0);
            }
            
            if (op > -1) 
            {
            	attr.setAttributeOp(op);
            }
        }
        catch (Exception e)
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
        return newAttribute((type >> 16), type & 0xFFFF, value, -1);
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
    
    public static byte[] assembleAttributeList(AttributeList list, long type)
    {
        Object[] aList;
        RadiusAttribute a;
        
        aList = list.getArray(type);

        if (aList != null)
        {
            int length = 0;
            for (int i=0; i<aList.length; i++)
            {
                a = (RadiusAttribute) aList[i];
                byte[] b = a.getValue().getBytes();
                if (b != null) length += b.length;
            }

            byte[] byteBuffer = new byte[length];
            
            int offset = 0;
            for (int i=0; i<aList.length; i++)
            {
                a = (RadiusAttribute) aList[i];
                byte[] b = a.getValue().getBytes();
                System.arraycopy(b, 0, byteBuffer, offset, b.length);
                offset += b.length;
            }
            
            return byteBuffer;
        }
        
        return null;
    }

    /**
     * Create a RadiusAttribute by name
     * @param aName The name of the attribute to create
     * @return Returns the newly created RadiusAttribute
     * @throws UnknownAttributeException
     */
    public static RadiusAttribute newAttribute(String aName) throws UnknownAttributeException
    {
        Class<?> c = attributeNameMap.get(aName);
        RadiusAttribute attr = null;

        if (c == null) 
        {
            throw new UnknownAttributeException("Unknown attribute " + aName);
        }
        
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
    public static RadiusAttribute newAttribute(AttributeDescription desc) throws UnknownAttributeException
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
    public static RadiusAttribute newAttribute(String aName, String aValue, String aOp) throws UnknownAttributeException
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
    public static long getTypeByName(String aName) throws UnknownAttributeException
    {
    	Class<?> c = attributeNameMap.get(aName);
        RadiusAttribute attr = null;

        if (c == null) 
        {
            throw new UnknownAttributeException("Unknown attribute " + aName);
        }
        
        try
        {
            attr = (RadiusAttribute)c.newInstance();
            return attr.getFormattedType();
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

    public static void poolStatus()
    {
		if (attributeObjectPool == null) return;
		System.err.println("AttributePool: active="+attributeObjectPool.getNumActive()+" idle="+attributeObjectPool.getNumIdle());
    }
    
    public static String getPoolStatus()
    {
		if (attributeObjectPool == null) return "";
		return "active="+attributeObjectPool.getNumActive()+", idle="+attributeObjectPool.getNumIdle();
    }
    
	public static void recycle(RadiusAttribute a) 
	{
		if (attributeObjectPool == null || !a.recyclable) 
		{
			// System.err.println("Did not recycle " + a.toString());
			return;
		}

		if (a.recycled)
		{
			System.err.println("PROBLEM: Recycling " + a.toString() + " " + a.getFormattedType());
		}
		
		a.setOverflow(false);
		
		try 
		{
			if (a instanceof VSAWithSubAttributes)
			{
				VSAWithSubAttributes aa = (VSAWithSubAttributes) a;
				AttributeList list = aa.getSubAttributes();
				list.clear();
			}
			
			attributeObjectPool.returnObject(new Long(a.getFormattedType()), a);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public static void recycle(AttributeList list) 
	{
		for (RadiusAttribute a : list.getAttributeList())
		{
			recycle(a);
		}
		
		// poolStatus();
	}
}
