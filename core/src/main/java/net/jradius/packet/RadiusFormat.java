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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.VSAttribute;
import net.jradius.packet.attribute.value.AttributeValue;
import net.jradius.log.RadiusLog;

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
    public static void setAttributeBytes(RadiusPacket packet, byte[] bAttributes)
    {
        if (bAttributes.length > 0)
        {
        	staticFormat.unpackAttributes(
                    packet.getAttributes(), 
                    bAttributes, 0,
                    bAttributes.length);
        }
    }

    /**
     * Packs a RadiusPacket into a byte array
     * @param packet The RadiusPacket to pack
     * @return Returns the packed RadiusPacket
     */
    public byte[] packPacket(RadiusPacket packet, String sharedSecret) throws IOException
    {
        if(packet == null)
        {
            throw new IllegalArgumentException("Packet is null.");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] attributeBytes = packAttributeList(packet.getAttributes());
        
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

    /**
     * Packs the RadiusPacket into a DataOutputStream
     * @param out The DataOutputStream to write to
     * @param p The RadiusPacket to pack
     * @param attributeBytes The RadiusPacket attributes
     * @throws IOException
     */
    public void packHeader(OutputStream out, RadiusPacket p, byte[] attributeBytes, String sharedSecret) throws IOException
    {
        int length = attributeBytes.length + RadiusPacket.RADIUS_HEADER_LENGTH;
        writeUnsignedByte(out, p.getCode());
        writeUnsignedByte(out, p.getIdentifier());
        writeUnsignedShort(out, length);
        out.write(p.getAuthenticator(attributeBytes, sharedSecret));
    }
    
    /**
     * Packs a RadiusAttribute into a DataOutputStream
     * @param out The DataOutputStream to write attibutes to
     * @param a The RadiusAttribute to pack
     * @throws IOException
     */
    public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        packHeader(out, a);
        attributeValue.getBytes(out);
    }


    /**
     * Packs a RadiusAttribute header into a DataOutputStream
     * @param out The DataOutputStream to write to
     * @param a The RadiusAttribute to pack
     * @throws IOException
     */
    public void packHeader(OutputStream out, RadiusAttribute a) throws IOException
    {
        if (a instanceof VSAttribute) 
        { 
            packHeader(out, (VSAttribute)a); 
            return; 
        }
        AttributeValue attributeValue = a.getValue();
        writeUnsignedByte(out, (int)a.getType());
        writeUnsignedByte(out, attributeValue.getLength() + HEADER_LENGTH);
    }

    /**
     * Packs a VSAttribute header into a DataOutputStream
     * @param out The DataOutputStream to write to
     * @param a The VSAttribute to pack
     * @throws IOException
     */
    public void packHeader(OutputStream out, VSAttribute a) throws IOException
    {
        AttributeValue attributeValue = a.getValue();
        writeUnsignedByte(out, (int)a.getType());
        writeUnsignedByte(out, attributeValue.getLength() + VSA_HEADER_LENGTH);
        writeUnsignedInt(out, a.getVendorId());
        writeUnsignedByte(out, (int)a.getVsaAttributeType());
        writeUnsignedByte(out, attributeValue.getLength() + 2);
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
        ctx.attributeType = readUnsignedByte(in);
        ctx.attributeLength = readUnsignedByte(in);
        ctx.headerLength = 2;
        
        return 0;
    }
}
