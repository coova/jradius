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

package net.jradius.security.auth;

import javax.security.auth.callback.Callback;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.packet.attribute.AttributeList;



/**
 * JRadius JAAS Callback.
 * @author David Bird
 */
public class JRadiusCallback implements Callback
{
    private final RadiusClient radiusClient;
    private RadiusAuthenticator radiusAuthenticator;
    private AttributeList authAttributes;
    private AttributeList acctAttributes;
    
    public JRadiusCallback(RadiusClient radiusClient)
    {
        this.radiusClient = radiusClient;
    }
    
    public RadiusClient getRadiusClient()
    {
        return radiusClient;
    }
    
    public RadiusAuthenticator getRadiusAuthenticator()
    {
        return radiusAuthenticator;
    }
    
    public void setRadiusAuthenticator(RadiusAuthenticator radiusAuthenticator)
    {
        this.radiusAuthenticator = radiusAuthenticator;
    }
    
    public AttributeList getAcctAttributes()
    {
        return acctAttributes;
    }

    public void setAcctAttributes(AttributeList acctAttributes)
    {
        this.acctAttributes = acctAttributes;
    }
    
    public AttributeList getAuthAttributes()
    {
        return authAttributes;
    }
    
    public void setAuthAttributes(AttributeList authAttributes)
    {
        this.authAttributes = authAttributes;
    }
}
