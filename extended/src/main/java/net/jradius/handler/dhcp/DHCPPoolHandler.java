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

import net.jradius.dictionary.vsa_dhcp.Attr_DHCPClientHardwareAddress;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPClientIPAddress;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPDHCPServerIdentifier;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPDomainNameServer;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPIPAddressLeaseTime;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPMessageType;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPRequestedIPAddress;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPRouterAddress;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPSubnetMask;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPYourIPAddress;
import net.jradius.handler.PacketHandlerChain;
import net.jradius.packet.DHCPAck;
import net.jradius.packet.DHCPDecline;
import net.jradius.packet.DHCPDiscover;
import net.jradius.packet.DHCPInform;
import net.jradius.packet.DHCPNack;
import net.jradius.packet.DHCPOffer;
import net.jradius.packet.DHCPRelease;
import net.jradius.packet.DHCPRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * Simple DHCP IP Pool Handler for FreeRADIUS. 
 * 
 * @author David Bird
 */
public class DHCPPoolHandler extends PacketHandlerChain
{

    public DHCPPoolHandler()
    {
    }

    private AddressPoolImpl _pool;
    private AddressPool getDefaultPool()
    {
        if (_pool != null) return _pool;
        
        try
        {
            InetAddress[] dns = new InetAddress[1];
            dns[0] = InetAddress.getByName("10.1.0.1");
            
            _pool = new AddressPoolImpl();
            _pool.setNetwork(InetAddress.getByName("10.1.0.0"));
            _pool.setNetmask(InetAddress.getByName("255.255.0.0"));
            _pool.setRouter(InetAddress.getByName("10.1.0.1"));
            _pool.setLeaseTime(900);
            _pool.setDns(dns);
            
            CacheManager cacheManager = CacheManager.create();
            Cache _leases = new Cache("ippool", 10000, true, false, 0, _pool.getLeaseTime() + 30);
            cacheManager.addCache(_leases);
            _pool.setLeases(_leases);

            //pool.addOption(Attr_DHCPProxyAutoDiscovery.VSA_TYPE, wpadURL);
            return _pool;
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean handle(JRadiusRequest request) throws Exception
    {
        RadiusPacket req = request.getRequestPacket();

        if (req.getCode() < 1024) return false;

        byte[] hwAddress = (byte[])req.getAttributeValue(Attr_DHCPClientHardwareAddress.TYPE);
        if (hwAddress == null) throw new DHCPException("no hardware address");

        return handle(request, hwAddress, getDefaultPool());
    }
    
    public boolean handle(JRadiusRequest request, byte[] hwAddress, AddressPool pool) throws Exception
    {
        RadiusPacket req = request.getRequestPacket();
        RadiusPacket rep = request.getReplyPacket();
        //AttributeList ci = request.getConfigItems();

        InetAddress requested = (InetAddress)req.getAttributeValue(Attr_DHCPRequestedIPAddress.TYPE);
        if (requested == null) requested = (InetAddress)req.getAttributeValue(Attr_DHCPClientIPAddress.TYPE);

        boolean forceRenew = req.getCode() == DHCPDecline.CODE;
        
        InetAddress ipAddress = pool.getIP(hwAddress, requested, forceRenew);

        // Get the reply attributes before overwriting the reply
        // with the correct response, based on code.
        AttributeList attributes = rep.getAttributes();

        switch(req.getCode())
        {
            case DHCPDiscover.CODE:
                if (ipAddress == null && requested != null)
                {
                    // Try to get a new IP if we did not like the request IP
                    ipAddress = pool.getIP(hwAddress, null, true);
                }

                rep = (ipAddress == null) ? new DHCPNack() : new DHCPOffer();
                break;

            case DHCPRequest.CODE:
                rep = (ipAddress == null) ? new DHCPNack() : new DHCPAck();
                break;

            case DHCPDecline.CODE:
                rep = (ipAddress == null) ? new DHCPNack() : new DHCPOffer();
                break;

            case DHCPInform.CODE:
                rep = new DHCPAck();
                break;

            case DHCPRelease.CODE:
                rep = new DHCPAck();
                break;

            default: 
                return true;
        }

        attributes.add(new Attr_DHCPMessageType(rep.getCode() - 1024));

        if (ipAddress != null)
        {
            switch(req.getCode())
            {
                case DHCPDiscover.CODE:
                case DHCPRequest.CODE:
                    attributes.add(new Attr_DHCPYourIPAddress(ipAddress));
                    attributes.add(new Attr_DHCPIPAddressLeaseTime(pool.getLeaseTime()));
                    attributes.add(new Attr_DHCPDHCPServerIdentifier(pool.getRouter()));

                    InetAddress[] dns = pool.getDns();
                    if (dns != null)
                    {
                        for (int i=0; i<dns.length; i++)
                        {
                            attributes.add(new Attr_DHCPDomainNameServer(dns[i]), false);
                        }
                    }

                    attributes.add(new Attr_DHCPSubnetMask(pool.getNetmask()));
                    attributes.add(new Attr_DHCPRouterAddress(pool.getRouter()));
                    /* drop through */
                    
                case DHCPInform.CODE:
                    //attributes.add(new Attr_DHCPProxyAutoDiscovery(wpadURL));
                    break;

                default: 
                    break;
            }

            //attributes.add(new Attr_DHCPWWWServerAddress("coova.org"));
        }
        
        rep.getAttributes().add(attributes);
        request.setReplyPacket(rep);

        return true; // do not continue in chain!
    }
}
