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


/**
 * Sub-TLV attribute, as specified by WiMAX.
 * It is a VSA with a TLV type.
 * {@see VSAttribute#vsaAttributeType} is used as the TLV type.
 * <p>
 * It should be used when communicating with FreeRADIUS
 *
 * @author Danilo Levantesi <danilo.levantesi@witech.it>
 */
public abstract class VSAWithSubAttributes extends VSAttribute
{
    private static final long serialVersionUID = 0L;

    private AttributeList subAttributes = new AttributeList();

	protected long subTlvType;

    /**
     * Encode the sub-TLV type like FreeRADIUS does.
     * <p>
     * Returns the VSA type (lower 1 bytes) encoded with the Vendor ID
     * (upper 2 bytes) and the TLV type ("middle" byte). sub-TLV type cannot be
     * larger than 1 byte.
     * <pre>
     *  0                   1                   2
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |      Vendor-Id                |   TLV-Type    |   VSA-Type    |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * </pre>
     * @see net.jradius.packet.attribute.RadiusAttribute#getFormattedType()
     */
    public long getFormattedType()
    {
        return vsaAttributeType | (subTlvType << 8) | (vendorId << 16);
    }

    public long getSubTlvType() {
        return subTlvType;
    }

    public void setSubTlvType(long subTlvType) {
        this.subTlvType = subTlvType;
    }

 	public AttributeList getSubAttributes() {
		return subAttributes;
	}

	public void setSubAttributes(AttributeList subAttributes) {
		this.subAttributes = subAttributes;
	}
	
}
