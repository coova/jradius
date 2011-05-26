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

import java.security.NoSuchAlgorithmException;

import net.jradius.client.RadiusClient;
import net.jradius.exception.RadiusException;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
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
    protected RadiusAttribute classAttribute;
    protected RadiusAttribute stateAttribute;

    /**
     * @return Returns the name(s) of the protocol(s) provided.
     */
    public abstract String getAuthName();

    /**
     * @param c The RadiusClient context being used
     * @param p Setup the Authenticator with packet data
     * @throws RadiusException
     * @throws NoSuchAlgorithmException 
     */
    public void setupRequest(RadiusClient c, RadiusPacket p) throws RadiusException, NoSuchAlgorithmException
    {
    	RadiusAttribute a;
        client = c;
        
        if (username == null)
        {
        	a = p.findAttribute(AttributeDictionary.USER_NAME);
            
        	if (a == null)
            	throw new RadiusException("You must at least have a User-Name attribute in a Access-Request");

        	username = AttributeFactory.copyAttribute(a, false);
        }
        
        if (password == null)
        {
        	a = p.findAttribute(AttributeDictionary.USER_PASSWORD);

        	if (a != null)
        	{
        		password = AttributeFactory.copyAttribute(a, false);
        	}
        }
    }

    /**
     * @param p The RadiusPacket to be processed
     * @throws RadiusException
     * @throws NoSuchAlgorithmException 
     */
    public abstract void processRequest(RadiusPacket p) throws RadiusException, NoSuchAlgorithmException;
    
    /**
     * If the protocol has a request/challenge process, this function must
     * be implemented.
     * @param request The original AccessRequest RadiusPacket
     * @param challenge The AccessChallenge packet
     * @throws RadiusException
     * @throws NoSuchAlgorithmException 
     */
    public void processChallenge(RadiusPacket request, RadiusPacket challenge)  throws RadiusException, NoSuchAlgorithmException
    {
    	classAttribute = challenge.findAttribute(AttributeDictionary.CLASS);
        if (classAttribute != null)
        	request.overwriteAttribute(AttributeFactory.copyAttribute(classAttribute, false));
        
        stateAttribute = challenge.findAttribute(AttributeDictionary.STATE);
        if (stateAttribute != null)
        	request.overwriteAttribute(AttributeFactory.copyAttribute(stateAttribute, false));
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
        
        return "".getBytes();
    }


	public void setUsername(RadiusAttribute userName) 
	{
		username = userName;
	}

	public void setPassword(RadiusAttribute cleartextPassword) 
	{
		password = cleartextPassword;
	}

    protected byte[] getClassAttribute()
    {
        return classAttribute == null ? null : classAttribute.getValue().getBytes();
    }

    protected byte[] getStateAttribute()
    {
        return stateAttribute == null ? null : stateAttribute.getValue().getBytes();
    }
}
