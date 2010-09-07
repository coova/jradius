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

package net.jradius.radsec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.jradius.dictionary.Attr_SharedSecret;
import net.jradius.exception.RadiusException;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.NullResponse;
import net.jradius.packet.PacketFactory;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.ListenerRequest;
import net.jradius.server.TCPListener;
import net.jradius.util.MessageAuthenticator;

/**
 * RadSec Listener
 *
 * @author David Bird
 */
public class RadSecListener extends TCPListener
{
	private String tunnelSharedSecret = "radsec";
	
	public RadSecListener()
	{
		this.sslWantClientAuth = true;
		this.sslNeedClientAuth = true;
		this.keepAlive = true;
		this.requiresSSL = true;
		this.port = 2083;
	}
	
	public JRadiusEvent parseRequest(ListenerRequest listenerRequest, ByteBuffer byteBuffer, InputStream inputStream) throws IOException, RadiusException 
    {
        RadSecRequest request = new RadSecRequest();
        ByteBuffer buffer = request.buffer_in;
        
        int code = RadiusFormat.readUnsignedByte(inputStream);
        int identifier = RadiusFormat.readUnsignedByte(inputStream);
        int length = RadiusFormat.readUnsignedShort(inputStream);
        
        length -= 4;

        if (length <= 0)
        	return null;
        
        buffer.clear();
        buffer.limit(inputStream.read(buffer.array(), 0, length));
        
        if (buffer.limit() != length)
        	return null;
        
        RadiusRequest req = (RadiusRequest) PacketFactory.parseUDP(code, identifier, length, buffer);

        System.err.println(req);
        
        if (req == null) 
        	return null;
        
        if (req instanceof AccountingRequest)
        {
        	if (req.verifyAuthenticator(tunnelSharedSecret)) 
        	{
        		req.addAttribute(new Attr_SharedSecret(tunnelSharedSecret));
        	}
        	else
        	{
                throw new RadiusException("Bad RadSec tunnel shared secret, set to "+tunnelSharedSecret);
        	}
        }
        else if (req instanceof RadiusRequest)
        {
        	try 
        	{
        		Boolean verified = MessageAuthenticator.verifyRequest(req, tunnelSharedSecret);
        		if (verified == null)
        		{
        			throw new RadiusException("Message-Authenticator required");
        		}
        		if (Boolean.TRUE.equals(verified))
        		{
        			req.addAttribute(new Attr_SharedSecret(tunnelSharedSecret));
        		}
        		else
        		{
                    throw new RadiusException("Bad RadSec tunnel shared secret, set to "+tunnelSharedSecret);
        		}
        	} 
        	catch (IOException e) 
        	{
        	}
        }
        
        req.addAttribute(new Attr_SharedSecret(tunnelSharedSecret));
        
        request.setSender("RadSec");
        request.setPackets(new RadiusPacket[] { req, new NullResponse() });
        request.setConfigItems(new AttributeList());

        return request;
    }

	public void setTunnelSharedSecret(String tunnelSharedSecret) {
		this.tunnelSharedSecret = tunnelSharedSecret;
	}
    
}
