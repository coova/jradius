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

package net.jradius.freeradius;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.VSAWithSubAttributes;
import net.jradius.packet.attribute.value.AttributeValue;


/**
 * Packs and Unpacks Radius Packets and Attributes for the FreeRADIUS Server.
 *
 * @author David Bird
 */
public class FreeRadiusFormat extends RadiusFormat
{
    private static final FreeRadiusFormat staticFormat = new FreeRadiusFormat();
    
    /**
     * @see net.jradius.packet.RadiusFormat#setAttributeBytes(net.jradius.packet.RadiusPacket, byte[])
    public static void setAttributeBytes(RadiusPacket p, byte[] bAttributes)
    {
        int attributesLength = bAttributes.length;
        
        if (attributesLength > 0)
        {
            staticFormat.unpackAttributes(p.getAttributes(), bAttributes, 0, attributesLength);
        }
    }
     */

    public static void setAttributeBytes(RadiusPacket p, ByteBuffer buffer, int length)
    {
    	staticFormat.unpackAttributes(p.getAttributes(), buffer, length);
    }

    /*
    public void packAttribute(OutputStream out, RadiusAttribute a)
    {
    	if (a instanceof VSAWithSubAttributes)
    	{
    		VSAWithSubAttributes sa = (VSAWithSubAttributes) a;

    		AttributeList subList = sa.getSubAttributes();

    		for (RadiusAttribute ra : subList.getAttributeList())
    		{
    			try
    			{
    				super.packAttribute(out, ra);
    			}
    			catch (Exception e)
    			{
    				RadiusLog.warn(e.getMessage(), e);
    			}
    		}
    	}
        else 
        {
			try
			{
				super.packAttribute(out, a);
			}
			catch (Exception e)
			{
				RadiusLog.warn(e.getMessage(), e);
			}
        }
    }
    */
    
    public void packAttribute(ByteBuffer buffer, RadiusAttribute a)
    {
    	if (a instanceof VSAWithSubAttributes)
    	{
    		VSAWithSubAttributes sa = (VSAWithSubAttributes) a;

    		AttributeList subList = sa.getSubAttributes();

    		for (RadiusAttribute ra : subList.getAttributeList())
    		{
    			try
    			{
    				super.packAttribute(buffer, ra);
    			}
    			catch (Exception e)
    			{
    				RadiusLog.warn(e.getMessage(), e);
    			}
    		}
    	}
        else 
        {
			try
			{
				super.packAttribute(buffer, a);
			}
			catch (Exception e)
			{
				RadiusLog.warn(e.getMessage(), e);
			}
        }
    }
    
    /**
     * @see net.jradius.packet.RadiusFormat#packHeader(java.io.OutputStream, net.jradius.packet.RadiusPacket, byte[], String)
    public void packHeader(OutputStream out, RadiusPacket p, byte[] attributeBytes, String sharedSecret) throws IOException
    {
        writeUnsignedInt(out, p.getCode());
        writeUnsignedInt(out, p.getIdentifier());
        writeUnsignedInt(out, attributeBytes == null ? 0 : attributeBytes.length);
    }
     */
    
    public void packHeader(ByteBuffer buffer, RadiusPacket p, int attributesLength, String sharedSecret)
    {
    	putUnsignedInt(buffer, p.getCode());
        putUnsignedInt(buffer, p.getIdentifier());
        putUnsignedInt(buffer, attributesLength);
    }

    public void packPacket(RadiusPacket packet, String sharedSecret, ByteBuffer buffer, boolean onWire) throws IOException
    {
        if (packet == null)
        {
            throw new IllegalArgumentException("Packet is null.");
        }

        int initialPosition = buffer.position();
        buffer.position(initialPosition + 12);
        packAttributeList(packet.getAttributes(), buffer, onWire);

        int finalPosition = buffer.position();
        int totalLength = finalPosition - initialPosition;
        int attributesLength = totalLength - 12;
        
        try
        {
        	buffer.position(initialPosition);
        	packHeader(buffer, packet, attributesLength, sharedSecret);
        	buffer.position(finalPosition);
        }
        catch(Exception e)
        {
            RadiusLog.warn(e.getMessage(), e);
        }
    }

    /**
     * @see net.jradius.packet.RadiusFormat#packHeader(java.io.OutputStream, net.jradius.packet.attribute.RadiusAttribute)
    public void packHeader(OutputStream out, RadiusAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        writeUnsignedInt(out, a.getFormattedType());
        writeUnsignedInt(out, attributeValue.getLength());
        writeUnsignedInt(out, a.getAttributeOp());
    }
     */
    
    public void packHeader(ByteBuffer buffer, RadiusAttribute a)
    {
        AttributeValue attributeValue = a.getValue();
        putUnsignedInt(buffer, a.getFormattedType());
        putUnsignedInt(buffer, attributeValue.getLength());
        putUnsignedInt(buffer, a.getAttributeOp());
    }
    
    /**
     * @see net.jradius.packet.RadiusFormat#unpackAttributeHeader(java.io.InputStream, net.jradius.packet.RadiusFormat.AttributeParseContext)
    public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException
    {
	    ctx.attributeType = readUnsignedInt(in);
	    ctx.attributeLength = readUnsignedInt(in);
	    ctx.attributeOp = readUnsignedInt(in);
		
        if (ctx.attributeType > (1 << 16))
        {
            // FreeRADIUS encodes the vendor number in the type
            // with: if (vendor) attr->attr |= (vendor << 16);
            ctx.vendorNumber = (int)((ctx.attributeType >> 16) & 0xffff);
            ctx.attributeType &= 0xffff;
        }
        
        return 12;
    }
     */

    public void unpackAttributeHeader(ByteBuffer buffer, AttributeParseContext ctx) throws IOException
    {
    	ctx.attributeOp = 0;
    	ctx.vendorNumber = -1;
    	ctx.padding = 0;

	    ctx.attributeType = getUnsignedInt(buffer);
	    ctx.attributeLength = getUnsignedInt(buffer) + 12;
	    ctx.attributeOp = getUnsignedInt(buffer);
	    ctx.headerLength = 12;
		
        if (ctx.attributeType > (1 << 16))
        {
            // FreeRADIUS encodes the vendor number in the type
            // with: if (vendor) attr->attr |= (vendor << 16);
            ctx.vendorNumber = (int)((ctx.attributeType >> 16) & 0xffff);
            ctx.attributeType &= 0xffff;
        }
    }
}
