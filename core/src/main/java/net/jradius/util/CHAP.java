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

package net.jradius.util;

import gnu.crypto.hash.HashFactory;
import gnu.crypto.hash.IMessageDigest;

/**
 * CHAP Utils. 
 * 
 * @author David Bird
 */
public final class CHAP
{
    /**
     * @param id The packet identifier
     * @param Password The User's Password value in bytes
     * @param Challenge The 16 byte authentication challenge
     * @return Returns the CHAP-Password
     */
    public static byte[] chapMD5(byte id, byte[] Password, byte[] Challenge)
    {
        IMessageDigest md = HashFactory.getInstance("MD5");
        md.update(id);
        md.update(Password, 0, Password.length);
        md.update(Challenge, 0, Challenge.length);
        return md.digest();
    }
    
    /**
     * Do CHAP
     * 
     * @param id The packet identifier
     * @param Password The User's Password value in bytes
     * @param Challenge The 16 byte authentication challenge
     * @return Returns the CHAP-Password
     */
    public static byte[] chapResponse(byte id, byte[] Password, byte[] Challenge)
    {
        byte[] Response = new byte[17];
        Response[0] = id;
        System.arraycopy(chapMD5(id, Password, Challenge), 0, Response, 1, 16);
        return Response;
    }
}
