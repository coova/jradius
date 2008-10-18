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

package net.jradius.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import net.jradius.client.RadiusClient;
import net.jradius.log.RadiusLog;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.security.auth.JRadiusCallback;



public class JAASAuthenticationTest 
{
    public static void main(String[] args) 
    {
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        
        LoginContext lc = null;
        
        try 
        {
            lc = new LoginContext("JRadius", new MyCallbackHandler());
        } 
        catch (LoginException le) 
        {
            System.err.println("Cannot create LoginContext: " + le.getMessage());
            System.exit(-1);
        } 
        catch (SecurityException se) 
        {
            System.err.println("Cannot create LoginContext: " + se.getMessage());
            System.exit(-1);
        } 

        int i;
    	for (i = 0; i < 3; i++) 
    	{
    	    try 
    	    {
    	        lc.login();
    	        break;
    	    } 
    	    catch (LoginException le) 
    	    {
    		  System.err.println("Authentication failed:");
    		  System.err.println("  " + le.getMessage());
    		  try 
    		  {
    		      Thread.sleep(3000);
    		  } 
    		  catch (Exception e) 
    		  {
    		  } 
    	    }
    	}

    	if (i == 3) 
    	{
    	    System.out.println("Sorry");
    	    System.exit(-1);
    	}

    	System.out.println("Authentication succeeded!");

    	Subject mySubject = lc.getSubject();

    	Iterator principalIterator = mySubject.getPrincipals().iterator();
    	System.out.println("Authenticated user has the following Principals:");

    	while (principalIterator.hasNext()) 
    	{
    	    Principal p = (Principal)principalIterator.next();
    	    System.out.println("\t" + p.toString());
    	}

    	System.out.println("User has " + mySubject.getPublicCredentials().size() + " Public Credential(s)");

    	PrivilegedAction action = new TestAction();
    	Subject.doAsPrivileged(mySubject, action, null);
    	System.exit(0);
    }
}

class MyCallbackHandler implements CallbackHandler 
{
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException 
    {
    	for (int i = 0; i < callbacks.length; i++) 
    	{
    	    if (callbacks[i] instanceof TextOutputCallback) 
    	    {
        		TextOutputCallback toc = (TextOutputCallback)callbacks[i];
        		switch (toc.getMessageType()) 
        		{
        			case TextOutputCallback.INFORMATION:
        			    System.out.println(toc.getMessage());
         		    	break;
        			case TextOutputCallback.ERROR:
        			    System.out.println("ERROR: " + toc.getMessage());
         		    	break;
        			case TextOutputCallback.WARNING:
        			    System.out.println("WARNING: " + toc.getMessage());
         		    	break;
        			default:
        			    throw new IOException("Unsupported message type: " + toc.getMessageType());
         		}
     	    } 
    	    else if (callbacks[i] instanceof NameCallback) 
    	    {
         		NameCallback nc = (NameCallback)callbacks[i];
         		System.err.print(nc.getPrompt());
         		System.err.flush();
         		nc.setName((new BufferedReader(new InputStreamReader(System.in))).readLine());
     	    } 
    	    else if (callbacks[i] instanceof PasswordCallback) 
    	    {
         		PasswordCallback pc = (PasswordCallback)callbacks[i];
         		System.err.print(pc.getPrompt());
         		System.err.flush();
         		pc.setPassword(readPassword(System.in));
    	    } 
    	    else if (callbacks[i] instanceof JRadiusCallback) 
    	    {
    	        JRadiusCallback rcb = (JRadiusCallback)callbacks[i];
    	        RadiusClient rc = rcb.getRadiusClient();
    	        AttributeList list = new AttributeList();
    	        
    	        rcb.setAuthAttributes(list);
    	        rcb.setAcctAttributes(list);
    	        
    	        System.err.print("Radius Server: ");
         		System.err.flush();
    	        rc.setRemoteInetAddress(InetAddress.getByName((new BufferedReader(new InputStreamReader(System.in))).readLine()));

    	        System.err.print("Shared Secret: ");
         		System.err.flush();
    	        rc.setSharedSecret((new BufferedReader(new InputStreamReader(System.in))).readLine());

    	        System.err.print("Auth Protocol: ");
         		System.err.flush();
         		String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
         		rcb.setRadiusAuthenticator(RadiusClient.getAuthProtocol(input));
         		
         		promptAttribute("NAS-Identifier", list);
    	    } 
    	    else 
    	    {
         		throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
    	    }
    	}
    }

    private void promptAttribute(String attr, AttributeList list)
    {
 		try
 		{
 		    // Standard Attributes:
 		    System.err.print(attr + ": ");
 		    System.err.flush();
     		String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
 		    list.add(AttributeFactory.newAttribute(attr, input, "="));
 		}
 		catch (Exception e)
 		{
 		    RadiusLog.error(e.getMessage());
 		}
    }
    
    private char[] readPassword(InputStream in) throws IOException 
    {
        char[] lineBuffer;
        char[] buf;

        buf = lineBuffer = new char[128];

        int room = buf.length;
        int offset = 0;
        int c;

        loop: while (true)
        {
            switch (c = in.read())
            {
            case -1:
            case '\n':
                break loop;

            case '\r':
                int c2 = in.read();
                if ((c2 != '\n') && (c2 != -1))
                {
                    if (!(in instanceof PushbackInputStream))
                    {
                        in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream) in).unread(c2);
                }
                else
                    break loop;

            default:
                if (--room < 0)
                {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(lineBuffer, 0, buf, 0, offset);
                    Arrays.fill(lineBuffer, ' ');
                    lineBuffer = buf;
                }
                buf[offset++] = (char) c;
                break;
            }
        }

        if (offset == 0)
        {
            return null;
        }

        char[] ret = new char[offset];
        System.arraycopy(buf, 0, ret, 0, offset);
        Arrays.fill(buf, ' ');

        return ret;
    }
}
