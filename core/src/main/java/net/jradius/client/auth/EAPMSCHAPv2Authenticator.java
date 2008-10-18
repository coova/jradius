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
    
    public byte[] doEAPType(byte id, byte[] data)
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

                short length = 55 + EAP_HEADERLEN; 
                byte[] response = new byte[54];
                response[0] = EAP_MSCHAPV2_RESPONSE;        // OpCode
                response[1] = (byte) (data[1] + 1);         // MS-CHAPv2-ID
                response[2] = (byte) (length << 8 & 0xFF);  // MS-Length
                response[3] = (byte) (length & 0xFF);       // MS-Length
                response[4] = 49;                           // Value-Size
                System.arraycopy(MSCHAP.doMSCHAPv2(getUsername(), getPassword(), challenge), 2, response, 5, 48);
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
                 *  |     Type      |   OpCode      |  MS-CHAPv2-ID |  MS-Length...
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 *  |   MS-Length   |                    Message...
                 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 */
                byte[] response = new byte[4];
                response[0] = EAP_MSCHAPV2_SUCCESS;
                response[1] = data[1];
                response[2] = (byte) 0;
                response[3] = (byte) 0;
                return response;
            }
        }

        return null;
    }
    
    private static final byte EAP_MSCHAPV2_ACK          = 0;
    private static final byte EAP_MSCHAPV2_CHALLENGE    = 1;
    private static final byte EAP_MSCHAPV2_RESPONSE     = 2;
    private static final byte EAP_MSCHAPV2_SUCCESS      = 3;
    private static final byte EAP_MSCHAPV2_FAILURE      = 4; 
}
