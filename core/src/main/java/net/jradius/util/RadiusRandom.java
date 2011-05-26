/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2007 David Bird <david@coova.com>
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

import java.util.Random;

import net.jradius.log.RadiusLog;

/**
 * A Random Number Generator (wrapper) for JRadius
 *
 * @author David Bird
 */
public class RadiusRandom
{
    static final Random rand = new Random();
    
    /**
     * Generates an array of random bytes.
     * @param length number of random bytes to generate
     * @return array of random bytes
     */
    public static byte[] getBytes(int length)
    {
        byte result[] = new byte[length];
        synchronized (rand)
        {
            for (int i = 0; i < length; i++)
            {
                try
                {
                    result[i] ^= rand.nextInt();
                }
                catch (Exception e)
                {
                    RadiusLog.error("Invalid operation", e);
                }
            }
        }
        return result;
    }
    
    public static String getRandomPassword(int length)
    {
        String pseudo[] = { "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "m", "n", "o", "p", "q", "r", "u", "s", "t", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        StringBuffer out = new StringBuffer(length);
        byte[] in = getBytes(length);
        for (int i=0; i < length; i++)
        {
            out.append(pseudo[((char)in[i]) % pseudo.length]);
        }
        String rslt = new String(out);
        return rslt;
    }

    public static String getRandomPassword(int length, String allowedCharacters)
    {
        StringBuffer out = new StringBuffer(length);
        byte[] in = getBytes(length);
        for (int i=0; i < length; i++)
        {
            out.append(allowedCharacters.charAt(((char)in[i]) % allowedCharacters.length()));
        }
        String rslt = new String(out);
        return rslt;
    }

    public static String getRandomString(int length)
    {
        return RadiusUtils.byteArrayToHexString(getBytes(length));
    }
}
