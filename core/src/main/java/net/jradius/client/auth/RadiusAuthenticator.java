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

package net.jradius.client.auth;

import net.jradius.client.RadiusClient;
import net.jradius.exception.RadiusException;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.RadiusAttribute;



/**
 * RADIUS Authentication Protocol Implementations. All Authenticators
 * are extended from this abstract class.
 * 
 * @author David Bird
 */
public abstract class RadiusAuthenticator 
{
    protected RadiusClient client;
    protected RadiusAttribute username;
    protected RadiusAttribute password;
    
    /**
     * @return Returns the name(s) of the protocol(s) provided.
     */
    public abstract String getAuthName();

    /**
     * @param c The RadiusClient context being used
     * @param p Setup the Authenticator with packet data
     * @throws RadiusException
     */
    public void setupRequest(RadiusClient c, RadiusPacket p) throws RadiusException
    {
        client = c;
        username = p.findAttribute(AttributeDictionary.USER_NAME);
        
        if (username == null)
        {
            throw new RadiusException("You must at least have a User-Name attribute in a Access-Request");
        }
        
        password = p.findAttribute(AttributeDictionary.USER_PASSWORD);
    }

    /**
     * @param p The RadiusPacket to be processed
     * @throws RadiusException
     */
    public abstract void processRequest(RadiusPacket p) throws RadiusException;
    
    /**
     * If the protocol has a request/challenge process, this function must
     * be implemented.
     * @param request The original AccessRequest RadiusPacket
     * @param challenge The AccessChallenge packet
     * @throws RadiusException
     */
    public void processChallenge(RadiusPacket request, RadiusPacket challenge)  throws RadiusException
    {
        throw new RadiusException("A RequestChallenge was returned for a "
                + getAuthName() + " authentication!\n" 
                + request.toString() + "\n" + challenge.toString());
    }
    
    /**
     * @return Returns the client.
     */
    public RadiusClient getClient()
    {
        return client;
    }
    
    /**
     * @param client The client to set.
     */
    public void setClient(RadiusClient client)
    {
        this.client = client;
    }
    
    /**
     * @return Returns the username.
     */
    protected byte[] getUsername()
    {
        return username == null ? null : username.getValue().getBytes();
    }

    /**
     * @return Returns the password.
     */
    protected byte[] getPassword()
    {
        if (password != null)
            return password.getValue().getBytes();
        
        return null;
    }
}
