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

import java.nio.ByteBuffer;
import java.util.Arrays;

import net.jradius.client.RadiusClient;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.value.NamedValue;
import net.jradius.util.RadiusUtils;


/**
 *  The RADIUS Accounting-Request Packet.
 *  See http://www.iana.org/assignments/radius-types
 *
 * @author David Bird
 */
public class AccountingRequest extends RadiusRequest
{
    public static final byte CODE = (byte)4;
    private static final long serialVersionUID = (long)CODE;
    
    /**
     * Default constructor
     */
    public AccountingRequest() 
    {
        code = CODE;
    }

    /**
     * Constructor
     * @param client The client context to be used (when creating UDP packets)
     */
    public AccountingRequest(RadiusClient client) 
    {
        super(client);
        code = CODE;
    }

    /**
     * Constructor
     * @param attributes The attribute list to be used
     */
    public AccountingRequest(AttributeList attributes) 
    {
        super(attributes);
        code = CODE;
    }

    /**
     * Constructor
     * @param client The client context to be used (when creating UDP packets)
     * @param attributes The attribute list to be used
     */
    public AccountingRequest(RadiusClient client, AttributeList attributes) 
    {
        super(client, attributes);
        code = CODE;
    }
    
    public static final int ACCT_STATUS_START 		   = 1;
    public static final int ACCT_STATUS_STOP 		   = 2;
    public static final int ACCT_STATUS_INTERIM 	   = 3;
    public static final int ACCT_STATUS_ACCOUNTING_ON  = 7;
    public static final int ACCT_STATUS_ACCOUNTING_OFF = 8;
    
    public int getAccountingStatusType()
    {
	    Long i = (Long)getAttributeValue(AttributeDictionary.ACCT_STATUS_TYPE);
	    if (i != null) return i.intValue();
	    return -1;
    }
    
    public void setAccountingStatusType(int type)
    {
        RadiusAttribute a = AttributeFactory.newAttribute(AttributeDictionary.ACCT_STATUS_TYPE, null);
        NamedValue s = (NamedValue)a.getValue();
        s.setValue(new Long(type));
        overwriteAttribute(a);
    }
    
    /**
     * Creates a Accounting-Request Authenticator
     * @see net.jradius.packet.RadiusPacket#createAuthenticator(byte[])
     */
    public byte[] createAuthenticator(byte[] attributes, int offset, int length, String sharedSecret) 
    {
        this.authenticator = RadiusUtils.makeRFC2866RequestAuthenticator(sharedSecret,
        		(byte)getCode(), (byte)getIdentifier(), attributes.length + RADIUS_HEADER_LENGTH, attributes, offset, length);

        return this.authenticator;
    }

    public boolean verifyAuthenticator(String sharedSecret)
    {
    	ByteBuffer buffer = ByteBuffer.allocate(1500);
    	RadiusFormat.getInstance().packAttributeList(getAttributes(), buffer, true);

        byte[] newauth = RadiusUtils.makeRFC2866RequestAuthenticator(sharedSecret,
                (byte)getCode(), (byte)getIdentifier(), buffer.position() + RADIUS_HEADER_LENGTH, 
                buffer.array(), 0, buffer.position());

        return Arrays.equals(newauth, this.authenticator);
    }
}
