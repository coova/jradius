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

package net.jradius.packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.LinkedHashMap;

import net.jradius.exception.RadiusException;
import net.jradius.freeradius.FreeRadiusFormat;


/**
 * RADIUS Packet Factor. Parses RADIUS packets and constructs
 * the appropriate Java class instance. 
 *
 * @author David Bird
 */
public class PacketFactory
{
    private static LinkedHashMap codeMap = new LinkedHashMap();
    
    static
    {
        codeMap.put(new Integer(AccessRequest.CODE),       AccessRequest.class);        // 1
        codeMap.put(new Integer(AccessAccept.CODE),        AccessAccept.class);         // 2
        codeMap.put(new Integer(AccessReject.CODE),        AccessReject.class);         // 3
        codeMap.put(new Integer(AccountingRequest.CODE),   AccountingRequest.class);    // 4
        codeMap.put(new Integer(AccountingResponse.CODE),  AccountingResponse.class);   // 5
        codeMap.put(new Integer(AccountingStatus.CODE),    AccountingStatus.class);     // 6
        codeMap.put(new Integer(PasswordRequest.CODE),     PasswordRequest.class);      // 7
        codeMap.put(new Integer(PasswordAck.CODE),         PasswordAck.class);          // 8
        codeMap.put(new Integer(PasswordReject.CODE),      PasswordReject.class);       // 9
        codeMap.put(new Integer(AccessChallenge.CODE),     AccessChallenge.class);      // 11
        codeMap.put(new Integer(DisconnectRequest.CODE),   DisconnectRequest.class);    // 40
        codeMap.put(new Integer(DisconnectACK.CODE),       DisconnectACK.class);        // 41
        codeMap.put(new Integer(DisconnectNAK.CODE),       DisconnectNAK.class);        // 42
        codeMap.put(new Integer(CoARequest.CODE),          CoARequest.class);           // 43
        codeMap.put(new Integer(CoAACK.CODE),              CoAACK.class);               // 44
        codeMap.put(new Integer(CoANAK.CODE),              CoANAK.class);               // 45

        codeMap.put(new Integer(DHCPDiscover.CODE),        DHCPDiscover.class);         // 1025
        codeMap.put(new Integer(DHCPOffer.CODE),           DHCPOffer.class);            // 1026
        codeMap.put(new Integer(DHCPRequest.CODE),         DHCPRequest.class);          // 1027
        codeMap.put(new Integer(DHCPDecline.CODE),         DHCPDecline.class);          // 1028
        codeMap.put(new Integer(DHCPAck.CODE),             DHCPAck.class);              // 1029
        codeMap.put(new Integer(DHCPNack.CODE),            DHCPNack.class);             // 1030
        codeMap.put(new Integer(DHCPRelease.CODE),         DHCPRelease.class);          // 1031
        codeMap.put(new Integer(DHCPInform.CODE),          DHCPInform.class);           // 1032
        codeMap.put(new Integer(DHCPForceRenew.CODE),      DHCPForceRenew.class);       // 1033
    }
    
    /**
     * Parse a UDP RADIUS message
     * @param dp The Datagram to be parsed
     * @return Returns the RadiusPacket
     * @throws RadiusException
     */
    public static RadiusPacket parse(DatagramPacket dp) throws RadiusException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
        DataInputStream input = new DataInputStream(bais);
        RadiusPacket rp = null;

        try
        {
            int code = RadiusFormat.readUnsignedByte(input);
            int identifier = RadiusFormat.readUnsignedByte(input);

            Class c = (Class)codeMap.get(new Integer(code));
         
            if (c == null)
            {
                throw new RadiusException("bad radius code");
            }

            int length = RadiusFormat.readUnsignedShort(input);
            byte[] bAuthenticator = new byte[16];
            input.readFully(bAuthenticator);

            byte[] bAttributes = new byte[length - RadiusPacket.RADIUS_HEADER_LENGTH];
            input.readFully(bAttributes);
          
            try
            {
                rp = (RadiusPacket)c.newInstance();
                //rp.setCode(code);
                rp.setIdentifier(identifier);
                rp.setAuthenticator(bAuthenticator);
                RadiusFormat.setAttributeBytes(rp, bAttributes);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return rp;
    }

    private static RadiusPacket parsePacket(DataInputStream input) throws RadiusException, IOException
    {
        RadiusPacket rp = null;
        int code = (int)RadiusFormat.readUnsignedInt(input);
        int identifier = (int)RadiusFormat.readUnsignedInt(input);
        
        Class c;
        if (code == 0)
        {
            c = NullPacket.class;
        }
        else
        {
            c = (Class)codeMap.get(new Integer(code));
        }
     
        if (c == null)
        {
            throw new RadiusException("bad radius packet type: " + code);
        }
        
        int length = input.readInt();
        byte[] bAttributes = new byte[length];
        input.readFully(bAttributes);
        
        try
        {
            rp = (RadiusPacket)c.newInstance();
            //rp.setCode(code);
            rp.setIdentifier(identifier);
            FreeRadiusFormat.setAttributeBytes(rp, bAttributes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return rp;
    }
    
    /**
     * Parse multiple RadiusPackets from a data stream
     * @param input The input data stream
     * @param packetCount Number of packets to expect
     * @return Returns an array of RadiusPackets
     * @throws RadiusException
     */
    public static RadiusPacket[] parse(DataInputStream input, int packetCount) throws RadiusException
    {
        RadiusPacket rp[] = new RadiusPacket[packetCount];
        
        try
        {
            for (int i=0; i < packetCount; i++)
            {
                rp[i] = parsePacket(input);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return rp;
    }
}
