/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
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
package net.jradius.packet.attribute.value;

/**
 * WiMAX signed attribute.
 *
 * 4-octet signed integer in network byte order.
 * It seems reasonable (at least for me ;-P ) to use the
 * {@see net.jradius.packet.attribute.value.IntegerValue}, overriding the
 * {@see net.jradius.packet.attribute.value.IntegerValue#setValue} and.
 * {@see net.jradius.packet.attribute.value.IntegerValue#isValid} methods.
 *
 * @author Danilo Levantesi <danilo.levantesi@witech.it>
 */
public class SignedValue extends IntegerValue {

    private static final long serialVersionUID = 0L;

    public SignedValue() {
    }

    public SignedValue(Long l) {
        super(l);
    }

    public SignedValue(Integer i) {
        super(i);
    }

    public SignedValue(int i) {
        super(i);
    }

    public SignedValue(long l) {
        super(l);
    }

    public String toString() {
        if (integerValue != null) {
            return integerValue.toString();
        }
        return "[Bad Signed Value]";
    }

    public String toXMLString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<signed>");
        if (integerValue != null) {
            sb.append(integerValue);
        }
        sb.append("</signed>");
        return sb.toString();
    }

    // This seems the only method which validates the input
    public void setValue(long l) throws NumberFormatException
    {
        if (isValid(l) == false) throw new NumberFormatException("[bad signed integer value: " + String.valueOf(l) + "]");
        integerValue = new Long(l);
    }

    public static boolean isValid(long l) {
        // dictionary.wimax states:
        // signed   - 4-octet signed integer in network byte order.
        // so it seems a Java integer value...
        if ((l < Integer.MIN_VALUE) || (l > Integer.MAX_VALUE)) {
            return false;
        }
        return true;
    }

}
