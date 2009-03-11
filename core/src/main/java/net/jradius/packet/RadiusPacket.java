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

package net.jradius.packet;

import java.io.Serializable;

import net.jradius.exception.UnknownAttributeException;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.value.AttributeValue;

/**
 * Represents a Radius Packet. All radius packet classes are derived
 * from this abstract class.
 *
 * @author David Bird
 */
public abstract class RadiusPacket implements Serializable
{
    private static final long serialVersionUID 		= 0L;
    public static final int MIN_PACKET_LENGTH       = 20;
    public static final int MAX_PACKET_LENGTH       = 4096;
    public static final short RADIUS_HEADER_LENGTH  = 20;
    
    private static Object nextPacketIdLock = new Object();
    private static int nextPacketId = 1;
   
    protected int code;
    protected int identifier = -1;
    protected byte[] authenticator;
    
    protected final AttributeList attributes = new AttributeList();
    
    /**
     * Default Constructor
     */
    public RadiusPacket()
    {
    }

    /**
     * Constructs a RadiusPacket with an AttributeList
     * @param list Initial AttributeList
     */
    public RadiusPacket(AttributeList list)
    {
    	if (list != null)
    		attributes.add(list);
    }

    /**
     * @param code The code to set
     */
    public void setCode(int code)
    {
        this.code = (byte)code;
    }
    
    /**
     * @return Returns the code of the RadiusPacket
     */
    public int getCode()
    {
        return code;
    }
  
    /**
     * Adds an attribute to a RadiusPacket (without overriding any
     * existing attributes)
     * @param attribute The attribute to add
     */
    public void addAttribute(RadiusAttribute attribute) 
    {
        if (null != attribute) attributes.add(attribute, false);
    }
    
    /**
     * Adds an attribute to a RadiusPacket overwriting any existing attribute
     * @param attribute The attribute to add
     */
    public void overwriteAttribute(RadiusAttribute attribute) 
    {
        if (null != attribute) attributes.add(attribute, true);
    }
    
    /**
     * Adds the contents of an AttributeList to a RadiusPacket
     * @param list The attributes to add
     */
    public void addAttributes(AttributeList list)
    {
        attributes.add(list);
    }
    
    /**
     * Removes an attribute
     * @param attribute The RadiusAttribute to be removed
     */
    public void removeAttribute(RadiusAttribute attribute)
    {
        attributes.remove(attribute);
    }

    /**
     * Removes an attribute
     * @param attributeType The attribute type to be removed
     */
    public void removeAttribute(long attributeType)
    {
        attributes.remove(attributeType);
    }

    /**
     * Get the Identifier of the RadiusPacket (creating one if needed)
     * @return Returns the RadiusPacket Identifier
     */
    public int getIdentifier()
    {
        if (this.identifier < 0)
        {
            this.identifier = getNewPacketId();
        }
        return this.identifier;
    }
    
    /**
     * Set the Identifier byte of a RadiusPacket
     * @param i The new Identifier
     */
    public void setIdentifier(int i)
    {
        this.identifier = i;
    }
    
    /**
     * Get the attributes of a RadiusPacket
     * @return Returns the AttributeList of the packet
     */
    public AttributeList getAttributes()
    {
        return attributes;
    }
    
    /**
     * Derived RadiusRequest classes must override this
     * @param attributes
     * @return Returns 16 bytes
     */
    public byte[] createAuthenticator(byte[] attributes, String sharedSecret)
    {
    	return new byte[16];
    }

    public boolean verifyAuthenticator(String sharedSecret)
    {
    	return false;
    }

    /**
     * @param authenticator The authenticator to set.
     */
    public void setAuthenticator(byte[] authenticator)
    {
        this.authenticator = authenticator;
    }
    
    /**
     * @return Returns the packet authenticator
     */
    public byte[] getAuthenticator() 
    {
        return this.authenticator;
    }

    /**
     * Get (or generate) the RADIUS Authenticator
     * @param attributes
     * @return Returns the packet authenticator
     */
    public byte[] getAuthenticator(byte[] attributes, String sharedSecret) 
    {
        if (this.authenticator == null)
        {
        	if (sharedSecret != null)
        		this.authenticator = createAuthenticator(attributes, sharedSecret);
        	else
        		this.authenticator = new byte[16];
        }
        
        return this.authenticator;
    }

    /**
     * @param type The attribute type
     * @return Returns the attribute, if found
     */
    public RadiusAttribute findAttribute(long type)
    {
        return attributes.get(type);
    }

    /**
     * @param type The integer type of the attribute to find
     * @return Returns an array of RadiusAttributes
     */
    public Object[] findAttributes(long type)
    {
        return attributes.getArray(type);
    }

    /**
     * @param aName The name of the attribute to find
     * @return Returns the RadiusAttribute, null if not found
     * @throws UnknownAttributeException
     */
    public RadiusAttribute findAttribute(String aName)
    	throws UnknownAttributeException
    {
        return attributes.get(aName);
    }

    /**
     * @param type The integer type of the attribute to find
     * @return Returns the Object value of the found attribute, otherwise null
     */
    public Object getAttributeValue(long type)
    {
        RadiusAttribute attribute = findAttribute(type);
        if (attribute != null)
        {
            AttributeValue value = attribute.getValue();
            if (value != null)
            {
                return value.getValueObject();
            }
        }
        return null;
    }
    
    /**
     * @param aName The name of the attribute to find
     * @return Returns the Object value of the found attribute, otherwise null
     * @throws UnknownAttributeException
     */
    public Object getAttributeValue(String aName)
    	throws UnknownAttributeException
    {
        RadiusAttribute attribute = findAttribute(aName);
        if (attribute != null)
        {
            AttributeValue value = attribute.getValue();
            if (value != null)
            {
                return value.getValueObject();
            }
        }
        return null;
    }
    
    /**
     * @return Returns the next RadiusPacket Identifier to be used
     */
    private static int getNewPacketId()
    {
        synchronized (nextPacketIdLock) 
        {
            nextPacketId = (nextPacketId + 1) % 255;
            return nextPacketId;
        }
    }
    
    /**
     * Formats the RadiusPacket into a String
     */
    public String toString(boolean nonStandardAtts, boolean unknownAttrs)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Class: ").append(this.getClass().toString()).append("\n");
        sb.append("Attributes:\n");
        sb.append(attributes.toString(nonStandardAtts, unknownAttrs));
        return sb.toString();
    }
    
    public String toString() 
    { 
        return toString(true, true); 
    }
}
