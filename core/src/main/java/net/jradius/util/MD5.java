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
import gnu.crypto.mac.IMac;
import gnu.crypto.mac.MacFactory;

import java.util.HashMap;

/**
 * MD5 Utils including HMAC-MD5
 * @author David Bird
 */
public class MD5 
{
	private static class ThreadLocalMD5 extends ThreadLocal<IMessageDigest> 
	{
		public IMessageDigest initialValue() 
		{
			return HashFactory.getInstance("MD5");
		}

		public IMessageDigest getMD5() 
		{ 
			IMessageDigest md = super.get();
			md.reset();
			return md;
		}
	}

	private static ThreadLocalMD5 md5 = new ThreadLocalMD5();

	private static class ThreadLocalHMACMD5 extends ThreadLocal<IMac> 
	{
		public IMac initialValue() 
		{
			return MacFactory.getInstance("HMAC-MD5");
		}

		public IMac getHMACMD5() 
		{ 
			IMac md = super.get();
			md.reset();
			return md;
		}
	}

	private static ThreadLocalHMACMD5 hmacmd5 = new ThreadLocalHMACMD5();

	public static IMessageDigest getMD5() { return md5.getMD5(); }

	public static IMac getHMACMD5() { return hmacmd5.getHMACMD5(); }
	
    public static byte[] md5(byte[] text)
    {
    	IMessageDigest md = md5.getMD5();
        md.update(text, 0, text.length);
        return md.digest();
    }

    public static byte[] md5(byte[] text1, byte[] text2)
    {
    	IMessageDigest md = md5.getMD5();
        md.update(text1, 0, text1.length);
        md.update(text2, 0, text2.length);
        return md.digest();
    }

    public static byte[] hmac_md5(byte[] text, byte[] key)
    {
        int minKeyLen = 64;
        byte[] digest = new byte[16];
        
        if (key.length < minKeyLen)
        {
            byte[] t = new byte[minKeyLen];
            System.arraycopy(key, 0, t, 0, key.length);
            key = t;
        }
        
        IMac mac = hmacmd5.getHMACMD5();
        HashMap attributes = new HashMap();
        
        attributes.put(IMac.MAC_KEY_MATERIAL, key);
        attributes.put(IMac.TRUNCATED_SIZE, new Integer(16));
    
        try
        {
            mac.init(attributes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        mac.update(text, 0, text.length);
        System.arraycopy(mac.digest(), 0, digest, 0, 16);
        return digest;
    }
}
