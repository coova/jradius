/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;

import net.jradius.client.RadiusClient;
import net.jradius.dictionary.Attr_EAPAkaCK;
import net.jradius.dictionary.Attr_EAPAkaIK;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.exception.RadiusException;
import net.jradius.packet.RadiusPacket;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * EAP-AKA Authentication. 
 * http://www.rfc-editor.org/rfc/rfc4187.txt
 * 
 * @author David Bird
 */
public class EAPAKAAuthenticator extends EAPAuthenticator
{
    public static final String NAME = "eap-aka";

    public EAPAKAAuthenticator()
    {
        setEAPType(EAP_AKA);
    }
    
    /**
     * @see net.jradius.client.auth.RadiusAuthenticator#getAuthName()
     */
    public String getAuthName()
    {
        return NAME;
    }
    
    private String username;
    private byte[] rand;
    private byte[] autn;
    private byte[] mac;
    
    private byte[] ik;
    private byte[] ck;
    
    private byte[] masterKey;
    private byte[] K_enc;
    private byte[] K_aut;
    private byte[] msk;
    private byte[] emsk;
    
    private byte[] lastEapMessage;
    
    public byte[] doEAPType(byte id, byte[] data, byte[] fullEAPPacket) throws RadiusException
    {
    	lastEapMessage = new byte[fullEAPPacket.length];
    	System.arraycopy(fullEAPPacket, 0, lastEapMessage, 0, fullEAPPacket.length);
    	return doEAPType(id, data);
    }
    
 	/* 
 	 * @see net.jradius.client.auth.EAPAuthenticator#doEAPType(byte, byte[])
 	 */
 	public byte[] doEAPType(byte id, byte[] data) throws RadiusException
 	{
 		int len = data.length;
 		if (len <= 3) throw new RadiusException("EAP-AKA too short");
 		int subType = data[0] & 0xFF;
 		int macOffset = 0;

 		// data[1], data[2]: reserved
 		switch(subType)
 		{
 			case AKA_CHALLENGE:
 			{
		 		for (int i=3; i < len; )
		 		{
		 			int attributeType = data[i++] & 0xFF;
		 			int attributeLength = data[i++] & 0xFF;
		 			attributeLength = (attributeLength * 4) - 2;
		 			byte[] attribute = new byte[attributeLength];
		            System.arraycopy(data, i, attribute, 0, attributeLength);
		            switch(attributeType) 
		            {
			            case AT_RAND:
			            	rand = attribute;
			            	break;
			            case AT_AUTN:
			            	autn = attribute;
			            	break;
			            case AT_MAC:
			            	macOffset = i;
			            	mac = attribute;
			            	break;
		            }
		            i += attributeLength;
		 		}
 			}
 			break;
 			
 			default: throw new RadiusException("Unhandled EAP AKA subType "+subType);
 		}
 		
 		if (rand == null || autn == null || mac == null)
 			throw new RadiusException("AUTN, RAND, and MAC needed in AKA challenge");

        try
 		{
        	// zero out mac in original eap message
        	System.arraycopy(new byte[18], 0, lastEapMessage, macOffset+5, 18);
        	// hmac_sha1 the eap message and compaire with AT_MAC attribute
        	// if mac do not equal throw new RadiusException("Could not authenticate home server");
 		} 
 		catch (Exception e)
 		{
 			throw new RadiusException(e);
 		}

 		return null;
	}

	/**
     * @see net.jradius.client.auth.RadiusAuthenticator#setupRequest(net.jradius.client.RadiusClient, net.jradius.packet.RadiusPacket)
     */
    public void setupRequest(RadiusClient c, RadiusPacket p) throws RadiusException
    {
        super.setupRequest(c, p);

        username = (String)p.getAttributeValue(Attr_UserName.TYPE);
        
        ik = (byte[])p.getAttributeValue(Attr_EAPAkaIK.TYPE);
        p.removeAttribute(Attr_EAPAkaIK.TYPE);

        ck = (byte[])p.getAttributeValue(Attr_EAPAkaCK.TYPE);
        p.removeAttribute(Attr_EAPAkaCK.TYPE);

        if (username == null || ik == null || ck == null)
        	throw new RadiusException("Request must have a User-Name, EAP-Aka-IK, and EAP-Aka-CK attributes");
        
        try
 		{
 			MessageDigest md = MessageDigest.getInstance("SHA");
 			
 			md.update(username.getBytes());
 			md.update(ik);
 			md.update(ck);
 			
 			masterKey = md.digest();
            
 			Signature sig = Signature.getInstance("NONEwithDSA", new BouncyCastleProvider());
 			
            sig.initSign(new DSAPrivateKey() 
            {
                public BigInteger getX()
                {
                    return null;
                }

                public String getAlgorithm()
                {
                    return null;
                }

                public byte[] getEncoded()
                {
                    return masterKey;
                }

                public String getFormat()
                {
                    return null;
                }

                public DSAParams getParams()
                {
                    return null;
                }
            });

            sig.update(masterKey);

 			byte[] result = sig.sign();

 			K_enc = new byte[16];
 			System.arraycopy(result, 0, K_enc, 0, 16);
 			
 			K_aut = new byte[16];
 			System.arraycopy(result, 16, K_aut, 0, 16);
 			
 			msk = new byte[64];
 			System.arraycopy(result, 32, msk, 0, 64);
 			
 			emsk = new byte[64];
 			System.arraycopy(result, 96, emsk, 0, 64);
 		} 
 		catch (Exception e)
 		{
 			throw new RadiusException("Requires NONEwithDSA from crypto provider", e);
 		}
    }

    // eap aka lengths
    public final static int AKA_IK_LENGTH = 16;
    public final static int AKA_CK_LENGTH = 16;
    public final static int AKA_MASTER_KEY = 20;
    
    // eap aka subTypes
    public final static int AKA_CHALLENGE = 1;
    public final static int AKA_REJECT = 2;
    public final static int AKA_NOTIFICATION = 12;
    public final static int AKA_REAUTH = 13;
    public final static int AKA_CLIENT_ERROR = 14;
    
    // eap aka attributes
    public final static int AT_RAND = 1;
    public final static int AT_AUTN = 2;
    public final static int AT_RES = 3;
    public final static int AT_AUTS = 4;
    public final static int AT_PADDING = 6;
    public final static int AT_NONCE_MT = 7;
    public final static int AT_PERMANENT_ID_REQ = 10;
    public final static int AT_MAC = 11;
    public final static int AT_NOTIFICATION = 12;
    public final static int AT_ANY_ID_REQ = 13;
    public final static int AT_IDENTITY = 14;
    public final static int AT_VERSION_LIST = 15;
    public final static int AT_SELECTED_VERSION = 16;
    public final static int AT_FULLAUTH_ID_REQ = 17;
    public final static int AT_COUNTER = 19;
    public final static int AT_COUNTER_TOO_SMALL = 20;
    public final static int AT_NONCE_S = 21;
    public final static int AT_CLIENT_ERROR_CODE = 22;
    public final static int AT_IV = 129;
    public final static int AT_ENCR_DATA = 130;
    public final static int AT_NEXT_PSEUDONYM = 132;
    public final static int AT_NEXT_REAUTH_ID = 133;
    public final static int AT_CHECKCODE = 134;
    public final static int AT_RESULT_IND = 135;
}
