package net.jradius.packet.attribute.value;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jradius.packet.Format;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.SubAttribute;

public class TLVFormat extends Format
{

	@Override
	public void packAttribute(OutputStream out, RadiusAttribute a) throws IOException 
	{
        AttributeValue attributeValue = a.getValue();
        writeUnsignedByte(out, (int)a.getType());
        writeUnsignedByte(out, attributeValue.getLength());
        writeUnsignedByte(out, ((SubAttribute)a).getFlags());
        attributeValue.getBytes(out);
	}

	@Override
	public int unpackAttributeHeader(InputStream in, AttributeParseContext ctx) throws IOException 
	{
        ctx.attributeType = readUnsignedByte(in);
        ctx.attributeLength = readUnsignedByte(in);
        int flags = readUnsignedByte(in);
        ctx.headerLength = 0;
        
		return 0;
	}

}
