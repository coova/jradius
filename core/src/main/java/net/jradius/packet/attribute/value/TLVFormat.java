package net.jradius.packet.attribute.value;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jradius.packet.Format;
import net.jradius.packet.attribute.RadiusAttribute;

public class TLVFormat extends Format
{
	int parentType;
	long vendorId;
	
	public TLVFormat(long vendor, int pt)
	{
		this.vendorId = vendor;
		this.parentType = pt;
	}
	
	@Override
	public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException 
	{
        AttributeValue attributeValue = a.getValue();
        writeUnsignedByte(out, (int)a.getType());
        writeUnsignedByte(out, attributeValue.getLength() + 2);
        attributeValue.getBytes(out);
	}

	@Override
	public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException 
	{
        ctx.attributeType = (readUnsignedByte(in) << 8) | (parentType & 0xFF);
        ctx.attributeLength = readUnsignedByte(in);
        ctx.vendorNumber = (int) vendorId;
        ctx.headerLength = 2;
		return 0;
	}
}
