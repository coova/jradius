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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.jradius.exception.RadiusException;
import net.jradius.packet.NullPacket;
import net.jradius.packet.PacketFactory;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.ListenerRequest;
import net.jradius.server.TCPListener;
import net.sf.ehcache.CacheManager;

import com.coova.ewt.server.ThreadContextManager;

/**
 * RadSec Listener
 *
 * @author David Bird
 */
public class RadSecListener extends TCPListener
{
	public RadSecListener()
	{
		this.usingSSL = true;
		this.keepAlive = true;
		this.requiresSSL = true;
		this.port = 2083;
	}
	
    public JRadiusEvent parseRequest(ListenerRequest listenerRequest, InputStream inputStream) throws IOException, RadiusException 
    {
        RadSecRequest request = new RadSecRequest();
        DataInputStream in = new DataInputStream(inputStream);

        RadiusPacket req = PacketFactory.parseUDP(in);
        
        if (req == null) 
        {
            throw new RadiusException("RadSec connection has been closed");
        }
        
        request.setSender("RadSec");
        request.setPackets(new RadiusPacket[] { req, new NullPacket() });
        request.setConfigItems(new AttributeList());

        return request;
    }
}
