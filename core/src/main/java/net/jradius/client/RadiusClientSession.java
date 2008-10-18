/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2007 David Bird <david@coova.com>
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

import java.util.Date;

import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.packet.attribute.RadiusAttribute;


public class RadiusClientSession implements Runnable
{
    private RadiusClient radiusClient;
    
    private long octetsIn;
    private long octetsOut;
    private long packetsIn;
    private long packetsOut;
    private long sessionTime;

    private long idleTimeout;
    private long sessionTimeout;
    private long interimInterval;

    private boolean authenticated = false;
    private boolean stopped = false;
    private RadiusAttribute classAttribute;
    private RadiusAuthenticator radiusAuthenticator;
    private Date startTime;
    private Thread thread;

    /*
     * Runs the radius session in a thread sending interim updates until
     * we have been told to logout.
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        while (authenticated && !stopped)
        {
        }
    }

    public synchronized void start() 
    {
        if (authenticated)
        {
            thread = new Thread(this);
            thread.start();
        }
    }

    public synchronized void stop()
    {
        stopped = true;
        if (thread != null)
        {
            thread.interrupt();
        }
    }
    
    public synchronized void incrementOctetsIn(long l)
    {
        octetsIn += l;
    }
    public synchronized void incrementOctetsOut(long l)
    {
        octetsOut += l;
    }
    public synchronized void incrementPacketsIn(long l)
    {
        packetsIn += l;
    }
    public synchronized void incrementPacketsOut(long l)
    {
        packetsOut += l;
    }
    
    class RadiusClientSessionException extends Exception 
    {
        public RadiusClientSessionException(String s)
        {
            super(s);
        }
    }
    
    /* Getters and Setters */
    
    public RadiusAttribute getClassAttribute()
    {
        return classAttribute;
    }
    
    public void setClassAttribute(RadiusAttribute classAttribute)
    {
        this.classAttribute = classAttribute;
    }
    
    public long getIdleTimeout()
    {
        return idleTimeout;
    }
    
    public void setIdleTimeout(long idleTimeout)
    {
        this.idleTimeout = idleTimeout;
    }
    
    public long getInterimInterval()
    {
        return interimInterval;
    }
    
    public void setInterimInterval(long interimInterval)
    {
        this.interimInterval = interimInterval;
    }
    
    public long getOctetsIn()
    {
        return octetsIn;
    }
    
    public void setOctetsIn(long octetsIn)
    {
        this.octetsIn = octetsIn;
    }
    
    public long getOctetsOut()
    {
        return octetsOut;
    }
    
    public void setOctetsOut(long octetsOut)
    {
        this.octetsOut = octetsOut;
    }
    
    public long getPacketsIn()
    {
        return packetsIn;
    }
    
    public void setPacketsIn(long packetsIn)
    {
        this.packetsIn = packetsIn;
    }
    
    public long getPacketsOut()
    {
        return packetsOut;
    }
    
    public void setPacketsOut(long packetsOut)
    {
        this.packetsOut = packetsOut;
    }
    
    public RadiusAuthenticator getRadiusAuthenticator()
    {
        return radiusAuthenticator;
    }
    
    public void setRadiusAuthenticator(RadiusAuthenticator radiusAuthenticator)
    {
        this.radiusAuthenticator = radiusAuthenticator;
    }
    
    public RadiusClient getRadiusClient()
    {
        return radiusClient;
    }
    
    public void setRadiusClient(RadiusClient radiusClient)
    {
        this.radiusClient = radiusClient;
    }
    
    public long getSessionTime()
    {
        return sessionTime;
    }
    
    public void setSessionTime(long sessionTime)
    {
        this.sessionTime = sessionTime;
    }
    
    public long getSessionTimeout()
    {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout)
    {
        this.sessionTimeout = sessionTimeout;
    }
}
