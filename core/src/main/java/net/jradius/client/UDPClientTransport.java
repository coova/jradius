/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2009 Coova Technologies, LLC <support@coova.com>
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

package net.jradius.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.PacketFactory;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;

public class UDPClientTransport extends RadiusClientTransport
{
    private static final RadiusFormat format = RadiusFormat.getInstance();
    
    public static final int defaultAuthPort = 1812;
    public static final int defaultAcctPort = 1813;

    protected DatagramSocket socket;

	public UDPClientTransport(DatagramSocket socket) 
	{
		this.socket = socket;
		this.remoteInetAddress = socket.getInetAddress();
	}

	public UDPClientTransport() throws SocketException 
	{
		this(new DatagramSocket());
	}

	public void close()
	{
		socket.close();
	}
	
    protected void send(RadiusRequest req, int attempt) throws Exception
    {
    	int port = req instanceof AccountingRequest ? acctPort : authPort;
    	
    	if (statusListener != null)
    	{
    		statusListener.onBeforeSend(this, req);
    	}
    	
        if (attempt > 1)
        {
            RadiusLog.warn("RadiusClient retrying request (attempt " + attempt + ")...");
        }

        ByteBuffer buffer = ByteBuffer.allocate(1500);
        format.packPacket(req, sharedSecret, buffer, true);
        DatagramPacket request = new DatagramPacket(buffer.array(), buffer.position(), getRemoteInetAddress(), port);
        socket.send(request);

        if (statusListener != null)
        {
    		statusListener.onAfterSend(this);
        }
    }
    
    protected RadiusResponse receive(RadiusRequest req) throws Exception
    {
        if (statusListener != null)
        {
        	statusListener.onBeforeReceive(this);
        }
        
        byte replyBytes[] = new byte[RadiusPacket.MAX_PACKET_LENGTH];
        DatagramPacket reply = new DatagramPacket(replyBytes, replyBytes.length);
        
        socket.receive(reply);
        
        RadiusPacket replyPacket = PacketFactory.parse(reply);
        
        if (!(replyPacket instanceof RadiusResponse))
        {
            throw new RadiusException("Received something other than a RADIUS Response to a Request");
        }

        if (statusListener != null)
        {
        	statusListener.onAfterReceive(this, replyPacket);
        }
        
        return (RadiusResponse)replyPacket;
    }
    
}
