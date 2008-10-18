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

package net.jradius.client.auth;

import net.jradius.util.CHAP;

/**
 * EAP-MD5 Authentication.
 * 
 * @author David Bird
 */
public class EAPMD5Authenticator extends EAPAuthenticator 
{
    public static final String NAME = "eap-md5";
    
    public EAPMD5Authenticator()
    {
        setEAPType(EAP_MD5);
    }
    
    /**
     * @see net.sf.jradius.client.auth.RadiusAuthenticator#getAuthName()
     */
    public String getAuthName()
    {
        return NAME;
    }
    
    public byte[] doEAPType(byte id, byte[] data)
    {
        byte md5len = data[0];
        byte[] md5data = new byte[md5len];
        System.arraycopy(data, 1, md5data, 0, md5len);

        byte[] response = new byte[17];
        response[0] = 16;

        System.arraycopy(CHAP.chapMD5(id, getPassword(), md5data), 0, response, 1, 16);
        
        return response;
    }
}
