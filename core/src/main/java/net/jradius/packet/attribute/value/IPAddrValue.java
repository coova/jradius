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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import net.jradius.log.RadiusLog;


/**
 * The IPv4 attribute value
 *
 * @author David Bird
 */
public class IPAddrValue extends AttributeValue
{
    private static final long serialVersionUID = 0L;
    protected InetAddress inetAddressValue;
    
    public IPAddrValue() { }
    
    public IPAddrValue(InetAddress i)
    {
        inetAddressValue = i;
    }
    
    public IPAddrValue(String s)
    {
        setValue(s);
    }

    public IPAddrValue(byte[] bytes)
    {
        setValue(bytes);
    }

    public void copy(AttributeValue value)
    {
    	IPAddrValue ipValue = (IPAddrValue) value;
    	this.inetAddressValue = ipValue.inetAddressValue;
    }
    
    public void setValue(String s)
    {
        try
        {
            inetAddressValue = InetAddress.getByName(s);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public int getLength()
    {
        return 4;
    }
    
    public void getBytes(OutputStream out) throws IOException
    {
        if (inetAddressValue != null)
        {
        	byte[] a = inetAddressValue.getAddress();
        	if (a.length != getLength())
        		throw new RuntimeException("Wrong IP address size for attribute");
            out.write(a);
        }
    }
    
    public void getBytes(ByteBuffer buffer)
    {
        if (inetAddressValue != null)
        {
        	byte[] a = inetAddressValue.getAddress();
        	if (a.length != getLength())
        		throw new RuntimeException("Wrong IP address size for attribute");
        	buffer.put(a);
        }
    }
    
    public void setValue(byte[] b)
    {
        if (b == null) return;
        try
        {
        	if (b.length != getLength())
        		throw new RuntimeException("Wrong IP address size for attribute");
            inetAddressValue = InetAddress.getByAddress(b);
        }
        catch (Exception e)
        {
        }
    }
    
    public void setValue(byte[] b, int off, int len)
    {
        if (b == null) return;
        try
        {
        	if (len != getLength())
        	{
        		throw new RuntimeException("Wrong IP address size for attribute");
        	}
        	byte[] bf = new byte[len];
        	System.arraycopy(b, off, bf, 0, len);
            inetAddressValue = InetAddress.getByAddress(bf);
        }
        catch (Exception e)
        {
        }
    }
    
    public String toString()
    {
        if (inetAddressValue != null)
        {
            return inetAddressValue.getHostAddress();
        }
        return "[Bad IP Address Value]";
    }

    public String toXMLString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<ip>");
        if (inetAddressValue != null) 
        {
            sb.append(inetAddressValue.getHostAddress());
        }
        sb.append("</ip>");
        return sb.toString();
    }
    
    public void setInetAddress(InetAddress inet)
    {
        this.inetAddressValue = inet;
    }

    public Serializable getValueObject()
    {
        return inetAddressValue;
    }
    
    public void setValueObject(Serializable o)
    {
		if (o instanceof InetAddress)
		{
			setInetAddress((InetAddress)o);
		}
		else if (o instanceof byte[])
		{
			setValue((byte[])o);
		}
		else
		{
			try
			{
				setInetAddress(InetAddress.getByName(o.toString()));
			}
			catch(Exception e)
			{
				RadiusLog.warn(e.getMessage());
			}
		}
    }
}
