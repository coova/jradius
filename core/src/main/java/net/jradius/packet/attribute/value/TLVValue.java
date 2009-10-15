package net.jradius.packet.attribute.value;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import net.jradius.packet.attribute.AttributeList;
import net.jradius.util.Hex;

public class TLVValue extends OctetsValue
{
	private static final long serialVersionUID = 1L;
	private TLVFormat format = new TLVFormat();
	private AttributeList list;
	
	public TLVValue(AttributeList subAttributes) {
		list = subAttributes;
	}

	@Override
	public void getBytes(OutputStream out) throws IOException {
		byteValue = format.packAttributeList(list);
		super.getBytes(out);
	}

	@Override
	public int getLength() {
		byteValue = format.packAttributeList(list);
		return super.getLength();
	}

	@Override
	public Serializable getValueObject() {
		return super.getValueObject();
	}

	@Override
	public void setValue(byte[] b) {
		super.setValue(b);
	}

	@Override
	public void setValueObject(Serializable o) {
		super.setValueObject(o);
	}

	@Override
    public String toDebugString()
    {
    	return "["+list.toString().trim().replaceAll("\n", ", ")+"]";
    }

	@Override
    public String toString()
    {
    	return toDebugString();
    }

	
}
