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
import java.nio.ByteBuffer;

import net.jradius.log.RadiusLog;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.VSAttribute;
import net.jradius.packet.attribute.value.AttributeValue;

/**
 * Default RadiusPacket/RadiusAttribute format class. This class formats
 * and parses UDP RADIUS Packets. Derived classes implement other formats.
 *
 * @author David Bird
 */
public class RadiusFormat extends Format
{
    private static final int HEADER_LENGTH = 2;
    public static final int VSA_HEADER_LENGTH = 8;

    private static final RadiusFormat staticFormat = new RadiusFormat();
    
    /**
     * @return Returns a static instnace of this class
     */
    public static RadiusFormat getInstance()
    {
        return staticFormat;
    }
    
    /**
     * Parses attributes and places them in a RadiusPacket
     * @param packet The RadiusPacket to parse attributes into
     * @param bAttributes The attribute bytes to parse
     */
    public static void setAttributeBytes(RadiusPacket packet, ByteBuffer buffer, int length)
    {
    	staticFormat.unpackAttributes(packet.getAttributes(), buffer, length);
    }

    /**
     * Packs a RadiusPacket into a byte array
     * @param packet The RadiusPacket to pack
     * @return Returns the packed RadiusPacket
    public byte[] packPacket(RadiusPacket packet, String sharedSecret) throws IOException
    {
    	return packPacket(packet, sharedSecret, false);
    }
     */
    
    public void packPacket(RadiusPacket packet, String sharedSecret, ByteBuffer buffer, boolean onWire) throws IOException
    {
        if (packet == null)
        {
            throw new IllegalArgumentException("Packet is null.");
        }

    	int initialPosition = buffer.position();
        buffer.position(initialPosition + RadiusPacket.RADIUS_HEADER_LENGTH);
        packAttributeList(packet.getAttributes(), buffer, onWire);

        int totalLength = buffer.position() - initialPosition;
        int attributesLength = totalLength - RadiusPacket.RADIUS_HEADER_LENGTH;
        
        try
        {
        	buffer.position(initialPosition);
        	packHeader(buffer, packet, buffer.array(), initialPosition + RadiusPacket.RADIUS_HEADER_LENGTH, attributesLength, sharedSecret);
        	buffer.position(totalLength + initialPosition);
        }
        catch(Exception e)
        {
            RadiusLog.warn(e.getMessage(), e);
        }
    }

    /*
    public byte[] packPacket(RadiusPacket packet, String sharedSecret, boolean onWire) throws IOException
    {
        if (packet == null)
        {
            throw new IllegalArgumentException("Packet is null.");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] attributeBytes = packAttributeList(packet.getAttributes(), onWire);
        
        try
        {
            packHeader(out, packet, attributeBytes, sharedSecret);
            if (attributeBytes != null) out.write(attributeBytes);
            out.close();
        }
        catch(Exception e)
        {
            RadiusLog.warn(e.getMessage(), e);
        }
        
        return out.toByteArray();
    }
    */

    /**
     * Packs the RadiusPacket into a DataOutputStream
     * @param out The DataOutputStream to write to
     * @param p The RadiusPacket to pack
     * @param attributeBytes The RadiusPacket attributes
     * @throws IOException
    public void packHeader(OutputStream out, RadiusPacket p, byte[] attributeBytes, String sharedSecret) throws IOException
    {
        int length = attributeBytes.length + RadiusPacket.RADIUS_HEADER_LENGTH;
        writeUnsignedByte(out, p.getCode());
        writeUnsignedByte(out, p.getIdentifier());
        writeUnsignedShort(out, length);
        out.write(p.getAuthenticator(attributeBytes, sharedSecret));
    }
     */
    
    public void packHeader(ByteBuffer buffer, RadiusPacket p, byte[] attributeBytes, int offset, int attributesLength, String sharedSecret) throws IOException
    {
        int length = attributesLength + RadiusPacket.RADIUS_HEADER_LENGTH;
        putUnsignedByte(buffer, p.getCode());
        putUnsignedByte(buffer, p.getIdentifier());
        putUnsignedShort(buffer, length);
        buffer.put(p.getAuthenticator(attributeBytes, offset, attributesLength, sharedSecret));
    }
    
    /**
     * Packs a RadiusAttribute into a DataOutputStream
     * @param out The DataOutputStream to write attributes to
     * @param a The RadiusAttribute to pack
     * @throws IOException
    public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        packHeader(out, a);
        attributeValue.getBytes(out);
    }
     */

    public void packAttribute(ByteBuffer buffer, RadiusAttribute a)
    {
        AttributeValue attributeValue = a.getValue();
        
        if (a instanceof VSAttribute)
        {
        	VSAttribute vsa = (VSAttribute) a;
        	if (vsa.hasContinuationByte())
        	{
        		int headerLength = headerLength(vsa);
        		int valueLength = attributeValue.getLength();
        		int maxLength = 255 - headerLength;
        		int len;
        		if (valueLength > maxLength)
        		{
        			for (int off = 0; off < valueLength; off += maxLength)
        			{
        				len = valueLength - off;
        				if (len > maxLength) 
        				{
        					len = maxLength;
                			vsa.setContinuation();
        				}
        				else
        				{
                			vsa.unsetContinuation();
        				}
        		        packHeader(buffer, a, len);
        		        attributeValue.getBytes(buffer, off, len);
        			}
        			return;
        		}
        	}
        }
        
        packHeader(buffer, a);
        attributeValue.getBytes(buffer);
    }

    /**
     * Packs a RadiusAttribute header into a DataOutputStream
     * @param out The DataOutputStream to write to
     * @param a The RadiusAttribute to pack
     * @throws IOException
    public void packHeader(OutputStream out, RadiusAttribute a) throws IOException
    {
        if (a instanceof VSAttribute) 
        { 
            packHeader(out, (VSAttribute)a); 
            return; 
        }
        AttributeValue attributeValue = a.getValue();
        writeUnsignedByte(out, (int) a.getType());
        writeUnsignedByte(out, attributeValue.getLength() + HEADER_LENGTH);
    }
     */

    public void packHeader(ByteBuffer buffer, RadiusAttribute a)
    {
    	packHeader(buffer, a, a.getValue().getLength());
    }

    public void packHeader(ByteBuffer buffer, RadiusAttribute a, int valueLength)
    {
        if (a instanceof VSAttribute) 
        { 
        	packHeader(buffer, (VSAttribute) a, valueLength); 
            return; 
        }
        putUnsignedByte(buffer, (int) a.getType());
        putUnsignedByte(buffer, valueLength + HEADER_LENGTH);
    }


    /**
     * Packs a VSAttribute header into a DataOutputStream
     * @param out The DataOutputStream to write to
     * @param a The VSAttribute to pack
     * @throws IOException
    public void packHeader(OutputStream out, VSAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        int len = attributeValue.getLength();
        int vsaHeader = VSA_HEADER_LENGTH;
        if (a.hasContinuationByte()) vsaHeader ++;
        writeUnsignedByte(out, (int)a.getType());
        writeUnsignedByte(out, len + vsaHeader);
        writeUnsignedInt(out, a.getVendorId());
        writeUnsignedByte(out, (int)a.getVsaAttributeType());
        len += 2;
        if (a.hasContinuationByte()) len ++;
        switch(a.getLengthLength())
        {
	        case 1:
	            writeUnsignedByte(out, len);
	            break;
	        case 2:
	            writeUnsignedShort(out, len);
	            break;
	        case 4:
	            writeUnsignedInt(out, len);
	            break;
        }
        if (a.hasContinuationByte())
        {
        	writeUnsignedByte(out, a.getContinuation());
        }
    }
     */
    
    public int headerLength(VSAttribute a)
    {
        int vsaHeader = 6;
        vsaHeader += a.getTypeLength();
        vsaHeader += a.getLengthLength();
        if (a.hasContinuationByte()) 
        {
        	vsaHeader ++;
        }
        return vsaHeader;
    }

    public int headerLength(RadiusAttribute a)
    {
        if (a instanceof VSAttribute) 
        { 
        	return headerLength((VSAttribute) a); 
        }
        return HEADER_LENGTH;
    }

    public void packHeader(ByteBuffer buffer, VSAttribute a) 
    {
    	packHeader(buffer, a, a.getValue().getLength());
    }
    
    public void packHeader(ByteBuffer buffer, VSAttribute a, int len) 
    {
        int vsaHeader = headerLength(a);

        /*
         *  Enforce the maximum packet length here.
         */
        if (len > (255 - vsaHeader))
        {
        	throw new RuntimeException("RADIUS attribute value too long ("+len+")");
        }
        
        putUnsignedByte(buffer, (int)a.getType());
        putUnsignedByte(buffer, len + vsaHeader);
        putUnsignedInt(buffer, a.getVendorId());
        putUnsignedByte(buffer, (int)a.getVsaAttributeType());
        
        len += 2;
        if (a.hasContinuationByte()) 
        {
        	len ++;
        }

        switch(a.getLengthLength())
        {
	        case 1:
	        	putUnsignedByte(buffer, len);
	            break;
	        case 2:
	            putUnsignedShort(buffer, len);
	            break;
	        case 4:
	            putUnsignedInt(buffer, len);
	            break;
        }
        if (a.hasContinuationByte())
        {
        	putUnsignedByte(buffer, a.getContinuation());
        }
    }
 
    /**
     * Unpacks the header of a RadiusAttribute from a DataInputStream
     * @param in The DataInputStream to read from
     * @param ctx The Attribute Parser Context 
     * @return Returns the additional offset length for this header
     * @throws IOException
     */
    public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException
    {
    	ctx.attributeOp = 0;
    	ctx.vendorNumber = -1;
    	ctx.padding = 0;

        ctx.attributeType = readUnsignedByte(in);
        ctx.attributeLength = readUnsignedByte(in);
        ctx.headerLength = 2;
        
        return 0;
    }

    public int unpackAttributeHeader(ByteBuffer buffer, AttributeParseContext ctx) throws IOException
    {
    	ctx.attributeOp = 0;
    	ctx.vendorNumber = -1;
    	ctx.padding = 0;

    	ctx.attributeType = getUnsignedByte(buffer);
        ctx.attributeLength = getUnsignedByte(buffer);
        ctx.headerLength = 2;
        
        ctx.attributeLength -= ctx.headerLength;
        
        return 2;
    }
}
