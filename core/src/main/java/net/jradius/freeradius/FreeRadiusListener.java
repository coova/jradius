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

package net.jradius.freeradius;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.jradius.exception.RadiusException;
import net.jradius.packet.PacketFactory;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.TCPListener;

/**
 * FreeRADIUS/rlm_jradius Listener
 *
 * @author Gert Jan Verhoog
 */
public class FreeRadiusListener extends TCPListener
{
    private static final FreeRadiusFormat format = new FreeRadiusFormat();

    public JRadiusEvent parseRequest(InputStream inputStream) throws IOException, RadiusException
    {
        FreeRadiusRequest request = new FreeRadiusRequest();
        DataInputStream in = new DataInputStream(inputStream);

        long nameLength  = RadiusFormat.readUnsignedInt(in);

        if (nameLength < 0 || nameLength > 1024) 
        {
            throw new RadiusException("KeepAlive rlm_jradius connection has been closed");
        }
        
        byte[] nameBytes = new byte[(int)nameLength];
        in.readFully(nameBytes);
        
        int messageType = RadiusFormat.readUnsignedByte(in);
        int packetCount = RadiusFormat.readUnsignedByte(in);

        RadiusPacket rp[] = PacketFactory.parse(in, packetCount);
        
        long length  = RadiusFormat.readUnsignedInt(in);
        byte[] bConfig = new byte[(int)length];
        in.readFully(bConfig);

        AttributeList configItems = new AttributeList();
        format.unpackAttributes(configItems, bConfig, 0, (int)length);
        
        request.setConfigItems(configItems);
        request.setSender(new String(nameBytes));
        request.setType(messageType);
        request.setPackets(rp);

        return request;
    }
}
