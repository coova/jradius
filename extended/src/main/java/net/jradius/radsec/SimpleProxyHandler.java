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

import java.net.InetAddress;

import net.jradius.client.RadiusClient;
import net.jradius.handler.PacketHandlerBase;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.server.JRadiusRequest;

import org.springframework.beans.factory.InitializingBean;

public class SimpleProxyHandler extends PacketHandlerBase implements InitializingBean
{
	private RadiusClient radiusClient;
	private String radiusServer = "localhost";
	private String sharedSecret = "testing123";
	private Integer authPort = 1812;
	private Integer acctPort = 1813;
	
    public boolean handle(JRadiusRequest request) throws Exception
    {
        RadiusRequest req = (RadiusRequest) request.getRequestPacket();
        RadiusResponse res = radiusClient.sendReceive(req, 3);
        request.setReplyPacket(res);
        return false;
    }

	public void afterPropertiesSet() throws Exception 
	{
		radiusClient = new RadiusClient(InetAddress.getByName(radiusServer), sharedSecret, authPort, acctPort, 60);
		if (radiusClient == null) throw new RuntimeException("could not create RadSec proxy radius client");
	}

	public void setRadiusClient(RadiusClient radiusClient) {
		this.radiusClient = radiusClient;
	}

	public void setRadiusServer(String radiusServer) {
		this.radiusServer = radiusServer;
	}

	public void setAuthPort(Integer authPort) {
		this.authPort = authPort;
	}

	public void setAcctPort(Integer acctPort) {
		this.acctPort = acctPort;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}
}
