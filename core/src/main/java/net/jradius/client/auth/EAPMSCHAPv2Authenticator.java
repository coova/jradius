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

import java.security.NoSuchAlgorithmException;

import net.jradius.util.MSCHAP;

/**
 * EAP-MSCHAPv2 Authentication.
 * 
 * @author David Bird
 */
public class EAPMSCHAPv2Authenticator extends EAPAuthenticator 
{
	public static final String NAME = "eap-mschapv2";
    
    public EAPMSCHAPv2Authenticator() 
    {
        setEAPType(EAP_MSCHAPV2);
    }
    
    public EAPMSCHAPv2Authenticator(boolean peap) 
    {
        setEAPType(EAP_MSCHAPV2);
        this.peap = peap;
    }
    
    /**
     * @see net.sf.jradius.client.auth.RadiusAuthenticator#getAuthName()
     */
    public String getAuthName()
    {
        return NAME;
    }
    
    public byte[] doEAPType(byte id, byte[] data) throws NoSuchAlgorithmException
    {
        byte opCode = data[0];
        switch (opCode)
        {
        	case EAP_MSCHAPV2_CHALLENGE: // EAP-MSCHAPv2-CHALLENGE
        	{
                /*
                 *   0                   1                   2                   3
                 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 *  |     Code      |   Identifier  |            Length             |
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 *  |     Type      |   OpCode      |  MS-CHAPv2-ID |  MS-Length...
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 *  |   MS-Length   |  Value-Size   |  Challenge...
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 *  |                             Challenge...
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 *  |                             Name...
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 */

                byte[] challenge = new byte[16];
                System.arraycopy(data, 5, challenge, 0, 16);

                int length = 54 + getUsername().length;
                byte[] response = new byte[length];
                response[0] = EAP_MSCHAPV2_RESPONSE;        // OpCode
                response[1] = data[1];                      // MS-CHAPv2-ID
                response[2] = (byte) (length << 8 & 0xFF);  // MS-Length
                response[3] = (byte) (length & 0xFF);       // MS-Length
                response[4] = 49;                           // Value-Size
                System.arraycopy(MSCHAP.doMSCHAPv2(getUsername(), getPassword(), challenge), 2, response, 5, 48); // Response
                response[53] = 0;                            // Flags
                System.arraycopy(getUsername(), 0, response, 54, getUsername().length); // Name
                return response;
            }
            
            case EAP_MSCHAPV2_SUCCESS: // EAP-MSCHAPv2-SUCCESS
            {
                /*
                 *   0                   1                   2                   3
                 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 *  |     Code      |   Identifier  |            Length             |
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 *  |     Type      |   OpCode      |
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 */
            	setState(STATE_AUTHENTICATED);
                byte[] response = new byte[1];
                response[0] = EAP_MSCHAPV2_SUCCESS;
                return response;
            }
            
            default:
            {
            	setState(STATE_FAILURE);
                byte[] response = new byte[1];
                response[0] = EAP_MSCHAPV2_FAILURE;
                return response;
            }
        }
    }
    
    protected static final byte EAP_MSCHAPV2_ACK          = 0;
    protected static final byte EAP_MSCHAPV2_CHALLENGE    = 1;
    protected static final byte EAP_MSCHAPV2_RESPONSE     = 2;
    protected static final byte EAP_MSCHAPV2_SUCCESS      = 3;
    protected static final byte EAP_MSCHAPV2_FAILURE      = 4; 
}
