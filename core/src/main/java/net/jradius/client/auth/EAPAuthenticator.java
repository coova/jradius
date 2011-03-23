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

import java.nio.ByteBuffer;

import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.RadiusAttribute;

/**
 * EAP Authentication. This abstract class is extended to
 * implement EAP protocols.
 * 
 * @author David Bird
 */
public abstract class EAPAuthenticator extends RadiusAuthenticator
{
	protected boolean peap = false;
    
    private boolean startWithIdentity = true;

    private byte eapType;

    protected int state = 0;
	
	public static final int STATE_CHALLENGE = 0;
	public static final int STATE_AUTHENTICATED = 1;
	public static final int STATE_REJECTED = 2;
	public static final int STATE_SUCCESS = 3;
	public static final int STATE_FAILURE = 4;
    
    /**
     * @see net.jradius.client.auth.RadiusAuthenticator#processRequest(net.jradius.packet.RadiusPacket)
     */
    public void processRequest(RadiusPacket p) throws RadiusException
    {
        p.removeAttribute(AttributeDictionary.USER_PASSWORD);
        RadiusAttribute a = AttributeFactory.newAttribute(AttributeDictionary.EAP_MESSAGE, 
                isStartWithIdentity() ? eapResponse(EAP_IDENTITY, (byte)0, getUsername()) : null, p.isRecyclable());
        p.overwriteAttribute(a);
    }
    
    /**
     * EAP requires a challenge/response. The request packet is reset with a new 
     * RADIUS identifier and the EAP-Message is encoded.
     * @see net.jradius.client.auth.RadiusAuthenticator#processChallenge(net.jradius.packet.RadiusPacket, net.jradius.packet.RadiusPacket)
     */
    public void processChallenge(RadiusPacket p, RadiusPacket r)  throws RadiusException
    {
    	super.processChallenge(p, r);
    	
        p.setIdentifier(-1);

        byte[] eapReply = AttributeFactory.assembleAttributeList(r.getAttributes(), AttributeDictionary.EAP_MESSAGE);
        byte[] eapMessage = doEAP(eapReply);
        
        RadiusAttribute a = p.findAttribute(AttributeDictionary.EAP_MESSAGE);
        if (a != null) p.removeAttribute(a);
        
        AttributeFactory.addToAttributeList(p.getAttributes(), 
        		AttributeDictionary.EAP_MESSAGE, eapMessage, p.isRecyclable());

        RadiusLog.debug("Sending Challenge:\n" + p.toString());
    }
    
    /**
     * @return Returns the EAP Type.
     */
    public byte getEAPType() 
    {
        return eapType;
    }
    
    /**
     * @param eapType The eapType to set.
     */
    public void setEAPType(int eapType)
    {
        this.eapType = (byte)eapType;
    }
    
    /**
     * @param id The EAP ID
     * @param data The EAP Data
     * @return Returns the EAP-Type specific EAP-Message
     */
    public abstract byte[] doEAPType(byte id, byte[] data) throws RadiusException;

    /*
     * Override this method if the eap authenticator requires the original eap message.
     * 
     */
    public byte[] doEAPType(byte id, byte[] data, byte[] fullEAPPacket) throws RadiusException
    {
    	return doEAPType(id, data);
    }
 
    protected boolean suedoEAPType(byte[] eap)
    {
    	if (peap) 
    	{
    		if (eap.length > 4 &&
    				(eap[0] == EAP_REQUEST || eap[0] == EAP_RESPONSE) &&
    				eap[4] == EAP_TLV) 
    		{
    			return false;
    		}
    		return true;
    	}
    	return false;
    }
    
    /**
     * From: http://tools.ietf.org/id/draft-kamath-pppext-peapv0-00.txt
     *<pre>   
     * 2.1.  Extensions Request Packet
     *   
     *   A summary of the Extensions Request packet format is shown below.  The
     *   fields are transmitted from left to right.
     *   
     *   0                   1                   2                   3
     *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |     Code      |   Identifier  |            Length             |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |     Type      |                  Data....
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *</pre>   
     * @param eapReply The in-coming EAP-Message
     * @return Returns the out-bound EAP-Message
     */
    public byte[] doEAP(byte[] eapReply) throws RadiusException
    {
        if (eapReply != null)
        {
            byte rtype = EAP_REQUEST;
            byte id = 0;
            int dlen = 0;

            ByteBuffer bb = ByteBuffer.wrap(eapReply);
            byte codeOrType = bb.get();
            
            if (suedoEAPType(eapReply))
            {
                dlen = bb.remaining();
            }
            else
            {
                rtype  = codeOrType;
                id     = bb.get();
                dlen   = bb.getShort() - EAP_HEADERLEN - 1;
                codeOrType = bb.get();
            }
            
            if (rtype != EAP_REQUEST)
            {
                RadiusLog.error("Expecting an EAP-Request.. got code: " + rtype);
                return null;
            }
            
            byte eapcode = codeOrType;
            byte[] data = null;

            if (dlen > 0)
            {
            	// eapcode = codeOrType;
                data = new byte[dlen];
                bb.get(data);
            }

            if (peap && eapcode == EAP_TLV)
            {
                return tlvSuccess(id);
            }

            if (eapcode == EAP_IDENTITY) 
            {
                return eapResponse(EAP_IDENTITY, id, getUsername());
            }

            if (eapcode != eapType)
            {
                return negotiateEAPType(id, eapType);
            }

            return eapResponse(eapType, id, doEAPType(id, data, eapReply));
        }
    
        return null;
    }

    /**
     * Negotiates the EAP Authentication Protocol to use
     * @param id The EAP ID
     * @param eapType The wanted EAP Protocol Type
     * @return Returns the EAP-Message
     */
    protected byte[] negotiateEAPType(byte id, byte eapType)
    {
    	return eapResponse(EAP_NAK, id, new byte[] { eapType });
    }

    /**
     * Encodes an EAP-Response
     * @param type The EAP-Type
     * @param id The EAP Packet ID
     * @param data The EAP-Message data
     * @return Returns the EAP-Message
     */
    protected byte[] eapResponse(int type, byte id, byte[] data)
    {
        int offset, length;
        byte[] response;
        
        if (!peap || type == EAP_TLV)
        {
            length = 1 + EAP_HEADERLEN + (data == null ? 0 : data.length);
            response = new byte[length];
            response[0] = EAP_RESPONSE;
            response[1] = id;
            response[2] = (byte)(length >> 8 & 0xFF);
            response[3] = (byte)(length & 0xFF);
            offset = 4;
        }
        else
        {
            length = 1 + data.length;
            response = new byte[length];
            offset = 0;
        }
        response[offset] = (byte)(type & 0xFF);
        if (data != null) System.arraycopy(data, 0, response, offset+1, data.length);
        return response;
    }

    protected byte[] eapRequest(int type, byte id, byte[] data)
    {
        int offset, length;
        byte[] response;
        length = 1 + EAP_HEADERLEN + (data == null ? 0 : data.length);
        response = new byte[length];
        response[0] = EAP_REQUEST;
        response[1] = id;
        response[2] = (byte)(length >> 8 & 0xFF);
        response[3] = (byte)(length & 0xFF);
        offset = 4;
        response[offset] = (byte)(type & 0xFF);
        if (data != null) 
        	System.arraycopy(data, 0, response, offset+1, data.length);
        return response;
    }

    public byte[] eapSuccess(byte id)
    {
        byte[] response;
        int length = EAP_HEADERLEN;
        response = new byte[length];
        response[0] = EAP_SUCCESS;
        response[1] = id;
        response[2] = (byte)(length >> 8 & 0xFF);
        response[3] = (byte)(length & 0xFF);
        return response;
    }

    public byte[] eapFailure(byte id)
    {
        byte[] response;
        int length = EAP_HEADERLEN;
        response = new byte[length];
        response[0] = EAP_FAILURE;
        response[1] = id;
        response[2] = (byte)(length >> 8 & 0xFF);
        response[3] = (byte)(length & 0xFF);
        return response;
    }

    /*
     *<pre>   
     * 2.3.1.  Result AVP
     *   
     *   The Result AVP provides support for acknowledged Success and Failure
     *   within EAP. It is defined as follows:
     *   
     *   0                   1                   2                   3
     *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |M|R|         AVP Type          |            Length             |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |              Status           |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *</pre>   
     */
    public byte[] tlvSuccess(byte id)
    {
        byte[] b = new byte[6];
        b[0] = (byte)0x80;
        b[1] = (byte)0x03;
        b[2] = (byte)0x00;
        b[3] = (byte)0x02;
        b[4] = (byte)0x00;
        b[5] = (byte)0x01;
        return eapResponse(EAP_TLV, id, b);
    }
    
    public static final int EAP_HEADERLEN = 4;

    public static final int EAP_REQUEST = 1;
    public static final int EAP_RESPONSE = 2;
    public static final int EAP_SUCCESS = 3;
    public static final int EAP_FAILURE = 4;
    
    public static final int EAP_IDENTITY = 1;
    public static final int EAP_NOTIFICATION = 2;
    public static final int EAP_NAK = 3;
    public static final int EAP_MD5 = 4;
    public static final int EAP_OTP = 5;
    public static final int EAP_GTC = 6;
    public static final int EAP_TLS = 13;
    public static final int EAP_LEAP = 17;
    public static final int EAP_SIM = 18;
    public static final int EAP_TTLS = 21;
    public static final int EAP_AKA = 23;
    public static final int EAP_PEAP = 25;
    public static final int EAP_MSCHAPV2 = 26;
    public static final int EAP_CISCO_MSCHAPV2 = 29;
    public static final int EAP_TLV = 33;

    public boolean isStartWithIdentity()
    {
        return startWithIdentity;
    }

    public void setStartWithIdentity(boolean startWithIdentity)
    {
        this.startWithIdentity = startWithIdentity;
    }
    

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

}
