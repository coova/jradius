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

package net.jradius.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/* fails, revert to gnu-crypto version
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
*/

import gnu.crypto.cipher.CipherFactory;
import gnu.crypto.cipher.IBlockCipher;
import gnu.crypto.cipher.WeakKeyException;
import gnu.crypto.hash.HashFactory;
import gnu.crypto.hash.IMessageDigest;

import net.jradius.log.RadiusLog;

/**
 * MSCHAP implementation translated into Java from the original 
 * pseudocode can be found in RFC 2759 and 2433. 
 * 
 * @author David Bird
 */
public final class MSCHAP
{
    private static void parity_key(byte[] szOut, final byte[] szIn, final int offset)
    {
        int i;
        int cNext = 0;
        int cWorking = 0;

        for (i = 0; i < 7; i++)
        {
            cWorking = 0xFF & szIn[i + offset];
            szOut[i] = (byte)(((cWorking >> i) | cNext | 1) & 0xff);
            cWorking = 0xFF & szIn[i + offset];
            cNext    = ((cWorking << (7 - i)));
        }

        szOut[i] = (byte) (cNext | 1);
    }

    private static byte[] unicode(byte[] in)
    {
        byte b[] = new byte[in.length * 2];
        for (int i = 0; i < b.length; i++)
            b[i] = 0;
        for (int i = 0; i < in.length; i++)
            b[(2 * i)] = in[i];
        return b;
    }

    private static byte[] ChallengeHash(final byte[] PeerChallenge, final byte[] AuthenticatorChallenge, final byte[] UserName) throws NoSuchAlgorithmException
    {
        byte Challenge[] = new byte[8];
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(PeerChallenge, 0, 16);
        md.update(AuthenticatorChallenge, 0, 16);
        md.update(UserName, 0, UserName.length);
        System.arraycopy(md.digest(), 0, Challenge, 0, 8);
        return Challenge;
    }

    /* fails without MD4 provider, revert to gnu-crypto version
    private static byte[] NtPasswordHash(byte[] Password) throws NoSuchAlgorithmException
    {
        byte PasswordHash[] = new byte[16];
        byte uniPassword[] = unicode(Password);
        MessageDigest md = MessageDigest.getInstance("MD4");
        md.update(uniPassword, 0, uniPassword.length);
        System.arraycopy(md.digest(), 0, PasswordHash, 0, 16);
        return PasswordHash;
    }
    */

    private static byte[] NtPasswordHash(byte[] Password) throws NoSuchAlgorithmException
    {
        byte PasswordHash[] = new byte[16];
        byte uniPassword[] = unicode(Password);
        IMessageDigest md = HashFactory.getInstance("MD4");
        md.update(uniPassword, 0, uniPassword.length);
        System.arraycopy(md.digest(), 0, PasswordHash, 0, 16);
        return PasswordHash;
    }

    /* not used currently
    private static byte[] HashNtPasswordHash(byte[] PasswordHash)
    {
        byte PasswordHashHash[] = new byte[16];
        IMessageDigest md = HashFactory.getInstance("MD4");
        md.update(PasswordHash, 0, 16);
        System.arraycopy(md.digest(), 0, PasswordHashHash, 0, 16);
        return PasswordHashHash;
    }
    */

    /* fails, revert to gnu-crypto version
    private static void DesEncrypt(byte[] Clear, int clearOffset, byte[] Key, int keyOffset, byte[] Cypher, int cypherOffset)
    {
        byte szParityKey[] = new byte[8];
        parity_key(szParityKey, Key, keyOffset);

        try
        {
            KeySpec ks = new DESKeySpec(szParityKey);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
            SecretKey sk = skf.generateSecret(ks);
            Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
            IvParameterSpec ips = new IvParameterSpec(new byte[] {0,0,0,0,0,0,0,0});
            c.init(Cipher.ENCRYPT_MODE, sk, ips);

            c.doFinal(Clear, clearOffset, Clear.length - clearOffset, Cypher, cypherOffset);
        }
        catch (Exception e)
        {
            RadiusLog.warn(e.getMessage(), e);
        }
    }
    */

    private static void DesEncrypt(byte[] Clear, int clearOffset, byte[] Key, int keyOffset, byte[] Cypher, int cypherOffset)
    {
        byte szParityKey[] = new byte[8];
        parity_key(szParityKey, Key, keyOffset);

        IBlockCipher cipher = CipherFactory.getInstance("DES");
        Map attributes = new HashMap();

        attributes.put(IBlockCipher.CIPHER_BLOCK_SIZE, new Integer(8));
        attributes.put(IBlockCipher.KEY_MATERIAL, szParityKey);

        try
        {
            cipher.init(attributes);
            cipher.encryptBlock(Clear, clearOffset, Cypher, cypherOffset);
        }
        catch (WeakKeyException e) { }
        catch (Exception e)
        {
            RadiusLog.warn(e.getMessage(), e);
        }
    }

    private static byte[] ChallengeResponse(final byte[] Challenge, final byte[] PasswordHash)
    {
        byte Response[] = new byte[24];
        byte ZPasswordHash[] = new byte[21];

        for (int i = 0; i < 16; i++)
            ZPasswordHash[i] = PasswordHash[i];

        for (int i = 16; i < 21; i++)
            ZPasswordHash[i] = 0;

        DesEncrypt(Challenge, 0, ZPasswordHash, 0, Response, 0);
        DesEncrypt(Challenge, 0, ZPasswordHash, 7, Response, 8);
        DesEncrypt(Challenge, 0, ZPasswordHash, 14, Response, 16);
        
        return Response;
    }

    private static byte[] NtChallengeResponse(byte[] Challenge, byte[] Password) throws NoSuchAlgorithmException
    {
        byte[] PasswordHash = NtPasswordHash(Password);
        return ChallengeResponse(Challenge, PasswordHash);
    }
    
    private static byte[] GenerateNTResponse(byte[] AuthenticatorChallenge, byte[] PeerChallenge, byte[] UserName, byte[] Password) throws NoSuchAlgorithmException
    {
    	byte Challenge[] = ChallengeHash(PeerChallenge, AuthenticatorChallenge, UserName);
        byte PasswordHash[] = NtPasswordHash(Password);
        return ChallengeResponse(Challenge, PasswordHash);
    }
    
    public static void DesHash(byte[] key, int offsetKey, byte[] Cypher, int offsetCypher)
    {
        /*
         * Make Cypher an irreversibly encrypted form of Clear by
         * encrypting known text using Clear as the secret key.
         * The known text consists of the string
         *
         *              KGS!@#$%
         */
        String ClearText = "KGS!@#$%";
        DesEncrypt(ClearText.getBytes(), 0, key, offsetKey, Cypher, offsetCypher);
    }
    
    public static byte[] LmPasswordHash(byte[] Password)
    {
        String pString = (new String(Password)).toUpperCase();
        byte[] PasswordHash = new byte[16];
        byte[] pByte = new byte[14];

        for (int i=0; i<14; i++) pByte[i] = 0;

        Password = pString.getBytes();
        for (int i=0; i < 14 && i < Password.length; i++)
            pByte[i] = Password[i];
        
        	DesHash(pByte, 0, PasswordHash, 0);
        	DesHash(pByte, 7, PasswordHash, 8);
        	
        	return PasswordHash;
    }
    
    public static byte[] LmChallengeResponse(byte[] Challenge, byte[] Password)
    {
        byte[] PasswordHash = LmPasswordHash(Password);
        return ChallengeResponse(Challenge, PasswordHash);
    }

    /**
     * Do MSCHAPv1 (supports using NT Password)
     * 
     * @param Password The User's Password value in bytes
     * @param AuthChallenge The 16 byte authentication challenge
     * @return Returns a 50 byte array - the MSCHAP Response
     * @throws NoSuchAlgorithmException 
     */
    public static byte[] doMSCHAPv1(byte[] Password, byte[] AuthChallenge) throws NoSuchAlgorithmException
    {
        byte[] response = new byte[50];
        // There is currently a problem with the LmChallengeResponse value!
        byte[] LmResponse = LmChallengeResponse(AuthChallenge, Password);
        byte[] NtResponse = NtChallengeResponse(AuthChallenge, Password);
        System.arraycopy(LmResponse, 0, response, 2, 24);
        System.arraycopy(NtResponse, 0, response, 26, 24);
        // Lets only use the NT password 
        response[1] = 0x01;
        return response;
    }

    /**
     * Do MSCHAPv2
     * 
     * @param UserName The User-Name attribute value bytes
     * @param Password The User's Password value in bytes
     * @param AuthChallenge The 16 byte authentication challenge
     * @return Returns a 50 byte array - the MSCHAPv2 Response
     * @throws NoSuchAlgorithmException 
     */
    public static byte[] doMSCHAPv2(byte[] UserName, byte[] Password, byte[] AuthChallenge) throws NoSuchAlgorithmException
    {
        byte[] response = new byte[50];
        byte peerChallenge[] = RadiusRandom.getBytes(16);
        byte ntResponse[] = GenerateNTResponse(AuthChallenge, peerChallenge, UserName, Password);
        System.arraycopy(peerChallenge, 0, response, 2, 16);
        System.arraycopy(ntResponse, 0, response, 26, 24);
        return response;
    }
    
    public static boolean verifyMSCHAPv2(byte[] UserName, byte[] Password, byte[] Challenge, byte[] Response) throws NoSuchAlgorithmException
    {
		byte peerChallenge[] = new byte[16];
		byte sentNtResponse[] = new byte[24];

		System.arraycopy(Response, 2, peerChallenge, 0, 16);
		System.arraycopy(Response, 26, sentNtResponse, 0, 24);

        byte ntResponse[] = GenerateNTResponse(Challenge, peerChallenge, UserName, Password);

        return Arrays.equals(ntResponse, sentNtResponse);
    }
}
