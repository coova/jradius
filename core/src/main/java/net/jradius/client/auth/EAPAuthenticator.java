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
    
    /**
     * @see net.jradius.client.auth.RadiusAuthenticator#processRequest(net.jradius.packet.RadiusPacket)
     */
    public void processRequest(RadiusPacket p) throws RadiusException
    {
        p.removeAttribute(AttributeDictionary.USER_PASSWORD);
        RadiusAttribute a = AttributeFactory.newAttribute(AttributeDictionary.EAP_MESSAGE, 
                isStartWithIdentity() ? eapResponse(EAP_IDENTITY, (byte)0, getUsername()) : null);
        p.overwriteAttribute(a);
    }
    
    /**
     * EAP requires a challenge/response. The request packet is reset with a new 
     * RADIUS itentifier and the EAP-Message is encoded.
     * @see net.jradius.client.auth.RadiusAuthenticator#processChallenge(net.jradius.packet.RadiusPacket, net.jradius.packet.RadiusPacket)
     */
    public void processChallenge(RadiusPacket p, RadiusPacket r)  throws RadiusException
    {
        p.setIdentifier(-1);

        Object[] aList;
        RadiusAttribute a;
        
        aList = r.findAttributes(AttributeDictionary.EAP_MESSAGE);

        if (aList == null)
        {
            throw new RadiusException("No EAP-Message in AccessChallenge");
        }
        

        // Count how long the EAP-Message is
        int eapLength = 0;
        for (int i=0; i<aList.length; i++)
        {
            a = (RadiusAttribute) aList[i];
            byte[] b = a.getValue().getBytes();
            if (b != null) eapLength += b.length;
        }

        byte[] eapReply = new byte[eapLength];
        
        int eapOffset = 0;
        for (int i=0; i<aList.length; i++)
        {
            a = (RadiusAttribute) aList[i];
            byte[] b = a.getValue().getBytes();
            System.arraycopy(b, 0, eapReply, eapOffset, b.length);
            eapOffset += b.length;
        }
        
        byte[] eapMessage = doEAP(eapReply);
        
        a = r.findAttribute(AttributeDictionary.STATE);
        if (a != null) p.overwriteAttribute(a);

        // Encode the EAP-Message into attribtue(s)
        a = p.findAttribute(AttributeDictionary.EAP_MESSAGE);
        if (a != null) p.removeAttribute(a);
        
        AttributeFactory.addToAttributeList(p.getAttributes(), AttributeDictionary.EAP_MESSAGE, eapMessage);

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
            ByteBuffer bb = ByteBuffer.wrap(eapReply);
            byte rtype = EAP_REQUEST;
            byte id = 0;
            int dlen = 0;

            byte codeOrType = bb.get();
            
            if (!peap || codeOrType == EAP_REQUEST)
            {
                rtype  = codeOrType;
                id     = bb.get();
                dlen   = bb.getShort() - EAP_HEADERLEN - 1;
                codeOrType = bb.get();
            }
            else
            {
                dlen = bb.remaining();
            }

            if (rtype != EAP_REQUEST)
            {
                RadiusLog.error("Expecting an EAP-Request.. got code: " + rtype);
                return null;
            }
            
            byte eapcode = 0;
            byte[] data = null;

            if (dlen > 0)
            {
                eapcode = codeOrType;
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

    public byte[] parseEAP(byte[] eapMessage) throws RadiusException
    {
        if (eapMessage != null)
        {
            ByteBuffer bb = ByteBuffer.wrap(eapMessage);
            byte code = bb.get();
            byte id = bb.get();
            int dlen = bb.getShort() - EAP_HEADERLEN - 1;
            byte type = bb.get();

            if (code != EAP_RESPONSE)
            {
                RadiusLog.error("Expecting an EAP-Response.. got code: " + code);
                return null;
            }
            
            byte[] data = null;

            if (dlen > 0)
            {
                data = new byte[dlen];
                bb.get(data);
            }

            return eapRequest(eapType, id, doEAPType(id, data, eapMessage));
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
            length = 1 + EAP_HEADERLEN + data.length;
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
        length = 1 + EAP_HEADERLEN + data.length;
        response = new byte[length];
        response[0] = EAP_REQUEST;
        response[1] = id;
        response[2] = (byte)(length >> 8 & 0xFF);
        response[3] = (byte)(length & 0xFF);
        offset = 4;
        response[offset] = (byte)(type & 0xFF);
        if (data != null) System.arraycopy(data, 0, response, offset+1, data.length);
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
}
