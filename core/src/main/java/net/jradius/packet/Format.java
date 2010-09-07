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

package net.jradius.packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import net.jradius.log.RadiusLog;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;

/**
 * @author David Bird
 */
public abstract class Format
{
	//abstract public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException;

    abstract public void packAttribute(ByteBuffer buffer, RadiusAttribute a);

    //abstract public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException;
    
    abstract public void unpackAttributeHeader(ByteBuffer buffer, AttributeParseContext ctx) throws IOException;
    
    /**
     * Packs an AttributeList into a byte array
     * @param attrs The AttributeList to pack
     * @return Returns the packed AttributeList
    public byte[] packAttributeList(AttributeList attrs)
    {
    	return packAttributeList(attrs, false);
    }
     */
    
    /*
    public byte[] packAttributeList(AttributeList attrs, boolean onWire)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Iterator<RadiusAttribute> iterator = attrs.getAttributeList().iterator();

        while (iterator.hasNext())
        {
        	RadiusAttribute attr = iterator.next();

        	if (onWire && attr.getType() > 1024)
        	{
        		continue;
        	}
                
            try
            {
            	packAttribute(out, attr);
            }
            catch (Exception e)
            {
                RadiusLog.warn(e.getMessage(), e);
            }
        }

        try
        {
            out.close();
        }
        catch(Exception e)
        {
            RadiusLog.warn(e.getMessage(), e);
        }
        
        return out.toByteArray();
    }
    */

    public void packAttributeList(AttributeList attrs, ByteBuffer buffer, boolean onWire)
    {
        Iterator<RadiusAttribute> iterator = attrs.getAttributeList().iterator();

        while (iterator.hasNext())
        {
        	RadiusAttribute attr = iterator.next();

        	if (onWire && attr.getType() > 1024)
        	{
        		continue;
        	}

        	if (attr.isOverflow())
        	{
        		continue;
        	}
        	
        	int currentPosition = buffer.position();
        	
        	try
        	{
        		packAttribute(buffer, attr);
        	}
        	catch (Throwable e) 
        	{
        		RadiusAttribute a = attrs.get(AttributeDictionary.CHARGEABLE_USER_IDENTITY);
        		RadiusLog.error("Truncating RADIUS packet " + (a == null ? "unknown" : a.toString())+" :: "+attr.toString(), null);
        		buffer.position(currentPosition);
        		attr.setOverflow(true);
        	}
        }
    }

    protected class AttributeParseContext
    {
        public long attributeType = 0;
        public long attributeLength = 0;
        public long attributeOp = RadiusAttribute.Operator.EQ;
        public long attributeValueLength = 0;
        public byte[] attributeValue = null;
        public int headerLength = 0;
        public int vendorNumber = -1;
        public int padding = 0;
    }
    
    /**
     * Unpacks RadiusAttributes from a byte array into an AttributeList
     * @param attrs The AttributeList to put unpacked attributes
     * @param bytes The bytes to be unpacked
     * @param bLength The length of the bytes to be unpacked
    public void unpackAttributes(AttributeList attrs, byte[] bytes, int bOffset, int bLength) 
    {
    	InputStream attributeInput = new ByteArrayInputStream(bytes, bOffset, bLength);

        try
        {
        	for (int pos = 0; pos < bLength; )
        	{
            	AttributeParseContext ctx = new AttributeParseContext();

                pos += unpackAttributeHeader(attributeInput, ctx);
                
                RadiusAttribute attribute = null;
                ctx.attributeValue = new byte[(int)(ctx.attributeLength - ctx.headerLength)];
                attributeInput.read(ctx.attributeValue, 0, (int)(ctx.attributeLength - ctx.headerLength));
                attribute = AttributeFactory.newAttribute(ctx.vendorNumber, ctx.attributeType, ctx.attributeValue, (int) ctx.attributeOp);

                if (attribute == null)
                {
                	RadiusLog.warn("Unknown attribute with type = " + ctx.attributeType);
                }
                else
                {
                    attrs._add(attribute, false);
                }

                if (ctx.padding > 0) 
                { 
                    pos += ctx.padding; 
                    while (ctx.padding-- > 0) 
                    {
                    	readUnsignedByte(attributeInput);
                    }
                }
                
                pos += ctx.attributeLength;
            }
            attributeInput.close();
        }
        catch (IOException e)
        {
            RadiusLog.warn(e.getMessage(), e);
        }
    }
     */

    public void unpackAttributes(AttributeList attrs, ByteBuffer buffer, int length) 
    {
    	AttributeParseContext ctx = new AttributeParseContext();
		int pos = 0;

		while (pos < length)
    	{
	    	try
	        {
        		unpackAttributeHeader(buffer, ctx);
	        }
	    	catch (Exception e)
	        {
	    		RadiusLog.error(e.getMessage(), e);
	            return;
	        }

	    	RadiusAttribute attribute = AttributeFactory.newAttribute(
	    			ctx.vendorNumber, ctx.attributeType, 
	    			ctx.attributeLength - ctx.headerLength, 
	    			(int) ctx.attributeOp, buffer);
    		
            if (attribute == null)
            {
            	RadiusLog.warn("Unknown attribute with type = " + ctx.attributeType);
            }
            else
            {
                attrs._add(attribute, false);
            }

            if (ctx.padding > 0) 
            { 
                pos += ctx.padding; 
                while (ctx.padding-- > 0) 
                {
                	getUnsignedByte(buffer);
                }
            }
            
            pos += ctx.attributeLength;
    	}
    }

    public static long readUnsignedInt(InputStream in) throws IOException
    {
    	byte[] b = new byte[4];
    	in.read(b);
    	
    	long value = b[3] & 0xFF;
    	value |= (b[2] & 0xFF) << 8;
    	value |= (b[1] & 0xFF) << 16;
    	value |= (b[0] & 0xFF) << 24;

    	return value;
    }
    
    public static int readUnsignedShort(InputStream in) throws IOException
    {
    	byte[] b = new byte[2];
    	in.read(b);

    	int value = b[1] & 0xFF;
    	value |= (b[0] & 0xFF) << 8;
    	
    	return value;
    }
    
    public static int readUnsignedByte(InputStream in) throws IOException
    {
        return in.read() & 0xFF;
    }
    
    public static void writeUnsignedByte(OutputStream out, int b) throws IOException
    {
        out.write(b);
    }
    
    public static void writeUnsignedShort(OutputStream out, int s) throws IOException
    {
        out.write((s >> 8) & 0xFF);
        out.write(s & 0xFF);
    }
    
    public static void writeUnsignedInt(OutputStream out, long i) throws IOException
    {
        writeUnsignedShort(out, (int)(i >> 16) & 0xFFFF);
        writeUnsignedShort(out, (int)i & 0xFFFF);
    }
    
    public static short getUnsignedByte (ByteBuffer bb)
    {
       return ((short)(bb.get() & 0xff));
    }

    public static void putUnsignedByte (ByteBuffer bb, int value)
    {
       bb.put ((byte)(value & 0xff));
    }

    public static short getUnsignedByte (ByteBuffer bb, int position)
    {
       return ((short)(bb.get (position) & (short)0xff));
    }

    public static void putUnsignedByte (ByteBuffer bb, int position,
       int value)
    {
       bb.put (position, (byte)(value & 0xff));
    }

    public static int getUnsignedShort (ByteBuffer bb)
    {
       return (bb.getShort() & 0xffff);
    }

    public static void putUnsignedShort (ByteBuffer bb, int value)
    {
       bb.putShort ((short)(value & 0xffff));
    }

    public static int getUnsignedShort (ByteBuffer bb, int position)
    {
       return (bb.getShort (position) & 0xffff);
    }

    public static void putUnsignedShort (ByteBuffer bb, int position, int value)
    {
       bb.putShort (position, (short)(value & 0xffff));
    }

    public static long getUnsignedInt (ByteBuffer bb)
    {
       return ((long)bb.getInt() & 0xffffffffL);
    }

    public static void putUnsignedInt (ByteBuffer bb, long value)
    {
       bb.putInt ((int)(value & 0xffffffffL));
    }

    public static long getUnsignedInt (ByteBuffer bb, int position)
    {
       return ((long)bb.getInt (position) & 0xffffffffL);
    }

    public static void putUnsignedInt (ByteBuffer bb, int position, long value)
    {
       bb.putInt (position, (int)(value & 0xffffffffL));
    }
}
