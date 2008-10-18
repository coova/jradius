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

package net.jradius.packet.attribute.value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import net.jradius.log.RadiusLog;


/**
 * Base abstract class of all Attribute Value classes.
 *
 * @author David Bird
 */
public abstract class AttributeValue implements Serializable
{
    private static final long serialVersionUID = 0L;

    public abstract void getBytes(OutputStream io) throws IOException;

    public byte[] getBytes()
    { 
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            this.getBytes(out);
            out.close();
        }
        catch (Exception e)
        {
            RadiusLog.error(e.getMessage());
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public int getLength() { return 0; }

    public Serializable getValueObject() { return null; }

    public abstract void setValue(byte[] b);

    public void setValue(String s) { setValue(s.getBytes()); }

    public abstract void setValueObject(Serializable o);

    public String toString() { return "[Binary Data]"; }
    
    public String toXMLString() { return ""; }
}
