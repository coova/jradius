/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2006-2008 David Bird <david@coova.com>
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

package net.jradius.handler.dhcp;

import java.net.InetAddress;

import net.jradius.packet.RadiusPacket;

public interface AddressPoolListener 
{
    public InetAddress leaseFind(byte[] chaddr, InetAddress gwaddr, InetAddress requestedIp, AddressPool pool);
    public void leaseSave(byte[] chaddr, InetAddress gwaddr, InetAddress yiaddr, AddressPool pool, RadiusPacket req);
    public void leaseExpired(byte[] chaddr, InetAddress gwaddr, InetAddress yiaddr, AddressPool pool);
}
