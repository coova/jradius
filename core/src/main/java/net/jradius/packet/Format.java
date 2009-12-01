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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import net.jradius.log.RadiusLog;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;

/**
 * @author David Bird
 */
public abstract class Format
{
    abstract public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException;

    abstract public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException;
    
    /**
     * Packs an AttributeList into a byte array
     * @param attrs The AttributeList to pack
     * @return Returns the packed AttributeList
     */
    public byte[] packAttributeList(AttributeList attrs)
    {
    	return packAttributeList(attrs, false);
    }
    
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

    protected class AttributeParseContext
    {
        public int attributeType = 0;
        public int attributeLength = 0;
        public int attributeOp = RadiusAttribute.Operator.EQ;
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
     */
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
                attributeInput.read(ctx.attributeValue, 0, ctx.attributeLength - ctx.headerLength);
                attribute = AttributeFactory.newAttribute(ctx.vendorNumber, ctx.attributeType, ctx.attributeValue, ctx.attributeOp);

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

    public static long readUnsignedInt(InputStream in) throws IOException
    {
        return ((long)readUnsignedShort(in) << 16) | (long)readUnsignedShort(in);
    }
    
    public static int readUnsignedShort(InputStream in) throws IOException
    {
        return (readUnsignedByte(in) << 8) | readUnsignedByte(in);
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
}
