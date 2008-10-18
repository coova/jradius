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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import net.jradius.util.RadiusRandom;
import net.jradius.util.RadiusUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

public class AddressPoolImpl implements AddressPool, CacheEventListener
{
    protected String leaseFile = "/tmp/leases.dhcp";
    protected InetAddress network;
    protected InetAddress netmask;
    protected InetAddress router;
    protected InetAddress[] dns;
    protected byte next = RadiusRandom.getBytes(1)[0];
    protected int fudge = 10;
    protected int leaseTime;
    protected AddressPoolListener listener;
    protected Cache leases;
    
    class MACKey implements Serializable
    {
        private static final long serialVersionUID = 0L;
        byte[] mac;
     
        public MACKey(byte[] b) { mac = b; }
        public byte[] getMAC() { return mac; }
        
        public boolean equals(Object o)
        {
            if (!(o instanceof MACKey))
                return false;
            
            if (this == o)
                return true;
            
            byte[] omac = ((MACKey) o).getMAC();
            
            if (mac.length != omac.length)
                return false;

            for (int i = 0; i < mac.length; i++)
            {
                if (mac[i] != omac[i])
                    return false;
            }
            
            return true;
        }
        
        public int hashCode()
        {
            return Arrays.hashCode(mac);
        }
    }

    public AddressPoolImpl()
    {
    }
    
    public AddressPoolImpl(InetAddress network, InetAddress netmask, InetAddress router, int leaseTime)
    {
        this.network = network;
        this.netmask = netmask;
        this.router = router;
        this.leaseTime = leaseTime;
    }
    
    public boolean contains(InetAddress ip) 
    {
        if (getNetwork() == null || getNetmask() == null)  
        	throw new RuntimeException("network/netmask requierd");

        byte[] networkBytes = getNetwork().getAddress();
        byte[] netmaskBytes = getNetmask().getAddress();
        byte[] ipBytes = ip.getAddress();
        
        if (networkBytes.length != netmaskBytes.length || netmaskBytes.length != ipBytes.length)
        {
            return false;
        }
        
        for (int i=0; i < netmaskBytes.length; i++) 
        {
            int mask = netmaskBytes[i] & 0xff;
            if ((networkBytes[i] & mask) != (ipBytes[i] & mask)) 
            {
                return false;
            }
        }
        
        return true;
    }

    public InetAddress nextIP() throws UnknownHostException
    {
        if (getNetwork() == null || getNetmask() == null)  
        	throw new RuntimeException("network/netmask requierd");

        InetAddress nextAddress = null;

        do
        {
            byte b[] = getNetwork().getAddress();
            b[b.length-1] = next++;
            nextAddress = InetAddress.getByAddress(b);
        }
        while(leases.get(nextAddress) != null ||
             (router != null && nextAddress.equals(router)));
        
        return nextAddress;
    }
    
    private static InetAddress anyIPAddress;

    static {
        try { anyIPAddress = InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 }); }
        catch (Exception e) { }
    }
    
    public InetAddress getIP(byte[] hwa, InetAddress requested, boolean forceNew) throws UnknownHostException
    {
        if (leases == null) throw new RuntimeException("leases not set");

        MACKey hwKey = new MACKey(hwa);
        Element eHW = leases.get(hwKey);
        Element eIP = leases.get(requested);
        
        if (anyIPAddress.equals(requested))
            requested = null;
        
        if (eHW == null)
        {
            /**
             *   Client does not yet have a leased IP address
             */
            
            if (requested != null) 
            {
                /**
                 *   Client is requesting an IP
                 */

                if (!contains(requested))
                {
                    /**
                     *  IP address not in our range!
                     */

                    return null;
                }

                if (eIP != null && hwKey.equals(eIP.getValue()))
                {
                    /**
                     *  We owned the lease, so let's go ahead and update the IP
                     */
                    
                    leases.remove(eIP.getKey());
                }
                else
                {
                    /**
                     *  IP address is already leased
                     */

                    return null;
                }

                eHW = new Element(hwKey, requested);
            }
            else
            {
                eHW = new Element(hwKey, nextIP());
            }
        }
        else
        {
            /**
             *  Client already has a leased IP
             */

            if (forceNew)
            {
                if (eIP != null && hwKey.equals(eIP.getValue()))
                {
                    /**
                     *  We owned the lease, so let's go ahead and update the IP
                     */

                    leases.remove(eIP.getKey());
                }

                eHW = new Element(hwKey, nextIP());
            }
            else if (requested != null) 
            {
                if (!requested.equals(eHW.getValue()))
                {
                    /**
                     *  Requested IP address does not match leased IP
                     */

                    if (eIP != null && hwKey.equals(eIP.getValue()))
                    {
                        /**
                         *  We owned the lease, so let's go ahead and update the IP
                         */

                        leases.remove(eIP.getKey());
                    }
                    else
                    {
                        return null;
                    }
                }
            }
        }

        eIP = new Element(eHW.getValue(), eHW.getKey());

        leases.put(eHW);
        leases.put(eIP);
        
        writeLeaseFile();
        
        return (InetAddress) eHW.getValue();
    }

    public void writeLeaseFile()
    {
        if (getLeaseFile() == null) return;
        try
        {
            File file = new File(getLeaseFile());
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            for (Object o : leases.getKeys())
            {
                if (o instanceof MACKey)
                {
                    InetAddress inet = (InetAddress)leases.get(o).getValue();
                    MACKey macKey = (MACKey)o;

                    writer.print(inet.getHostAddress());
                    writer.print(" ");
                    writer.println(RadiusUtils.byteArrayToHexString(macKey.getMAC()));
                }
            }
            
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void notifyElementEvicted(Ehcache cache, Element e)
    {
    }

    public void notifyElementExpired(Ehcache cache, Element e)
    {
    }

    public void notifyElementPut(Ehcache cache, Element e) throws CacheException
    {
    }

    public void notifyElementRemoved(Ehcache cache, Element e) throws CacheException
    {
    }

    public void notifyElementUpdated(Ehcache cache, Element e) throws CacheException
    {
    }

    public void notifyRemoveAll(Ehcache cache)
    {
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public void dispose()
    {
    }

    public void setFudge(int fudge)
    {
        this.fudge = fudge;
    }

    public void setLeaseFile(String leaseFile)
    {
        this.leaseFile = leaseFile;
    }

    public void setLeases(Cache leases)
    {
        this.leases = leases;
    }

    public void setLeaseTime(int leaseTime)
    {
        this.leaseTime = leaseTime;
    }

    public void setNetmask(InetAddress netmask)
    {
        this.netmask = netmask;
    }

    public void setNetwork(InetAddress network)
    {
        this.network = network;
    }

    public void setRouter(InetAddress router)
    {
        this.router = router;
    }

    public String getLeaseFile()
    {
        return leaseFile;
    }

    public Ehcache getLeases()
    {
        return leases;
    }

    public int getLeaseTime()
    {
        return leaseTime;
    }

    public InetAddress getNetmask()
    {
        return netmask;
    }

    public InetAddress getNetwork()
    {
        return network;
    }

    public InetAddress getRouter()
    {
        return router;
    }

    public InetAddress[] getDns()
    {
        return dns;
    }

    public void setDns(InetAddress[] dns)
    {
        this.dns = dns;
    }

    public AddressPoolListener getListener()
    {
        return listener;
    }

    public void setListener(AddressPoolListener listener)
    {
        this.listener = listener;
    }
}
