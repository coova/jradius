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

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.jradius.client.RadiusClient;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.security.JRadiusPrincipal;



/**
 * JRadius JAAS LoginModule.
 * @author David Bird
 */
public class JRadiusModule implements LoginModule
{
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;
    
    private String userName;
    private JRadiusPrincipal principal;
    private RadiusClient radiusClient;
    private AttributeList authAttributes;
    private AttributeList acctAttributes;
    
    private boolean debug = false;
    private boolean authenticated = false;
    private boolean committed = false;
    private int retries = 3;
    private int attempts = 0;
    
    public void initialize(Subject subject, CallbackHandler callbackHandler, 
            Map sharedState, Map options)
    {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        
        debug = "true".equalsIgnoreCase((String)options.get("debug"));
    }
    
    public boolean commit() throws LoginException
    {
        if (!authenticated) return false;
        principal = new JRadiusPrincipal(userName);
        if (!subject.getPrincipals().contains(principal))
        {
            subject.getPrincipals().add(principal);
        }
        attempts = 0;
        committed = true;
        return true;
    }
    
    public boolean login() throws LoginException
    {
        if (callbackHandler == null) 
        {
            throw new LoginException("No CallbackHandler for this JRadius LoginModule.");
        }

        if (radiusClient == null)
        {
            radiusClient = new RadiusClient();
        }
        
        NameCallback nameCallback = new NameCallback("User Name: ");
        PasswordCallback passwordCallback = new PasswordCallback("Password: ", true);
        JRadiusCallback radiusCallback = new JRadiusCallback(radiusClient);

        Callback[] callbacks = new Callback[3];
        callbacks[0] = nameCallback;
        callbacks[1] = passwordCallback;
        callbacks[2] = radiusCallback;

        try 
        {
            callbackHandler.handle(callbacks);
        } 
        catch (IOException ioex) 
        {
            throw new LoginException(ioex.getMessage());
        } 
        catch (UnsupportedCallbackException uscbex) 
        {
            StringBuffer sb = new StringBuffer("Error: Callback ");
            sb.append(uscbex.getCallback().toString());
            sb.append(" not supported.");
            throw new LoginException(sb.toString());
        }
        
        userName = nameCallback.getName();
        String password = new String(passwordCallback.getPassword());
        
        try
        {
            AccessRequest request = new AccessRequest(radiusClient, radiusCallback.getAuthAttributes());
            request.addAttribute(AttributeFactory.newAttribute("User-Name", userName, "="));
            request.addAttribute(AttributeFactory.newAttribute("User-Password", password, "="));
            if (debug)
            {
                RadiusLog.debug("Sending:\n" + request.toString());
            }
            RadiusResponse reply = radiusClient.authenticate(request, radiusCallback.getRadiusAuthenticator(), retries);
            if (reply == null) throw new LoginException("no reply from remote RADIUS server");
            if (debug)
            {
                RadiusLog.debug("Received:\n" + reply.toString());
            }
            if (!(reply instanceof AccessAccept)) throw new CredentialExpiredException("authentication failed");
        }
        catch (Exception ioex) 
        {
            throw new LoginException(ioex.getMessage());
        } 

        authenticated = true;
        return true;
    }
    
    public boolean logout() throws LoginException
    {
        subject.getPrincipals().remove(principal);
        authenticated = false;
        committed = false;
        userName = null;
        principal = null;
        attempts = 0;
        return true;
    }
    
    public boolean abort() throws LoginException
    {
        if (!authenticated) return false;
        if (authenticated && !committed)
        {
            authenticated = false;
            attempts = 0;
        } 
        else 
        {
            logout();
        }
        return true;
    }
}
