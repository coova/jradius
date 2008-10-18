/**
 * RadClient
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package net.jradius.client;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.client.auth.TunnelAuthenticator;
import net.jradius.exception.StandardViolatedException;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.standard.RadiusStandard;
import net.jradius.util.RadiusRandom;



/**
 * A Java RADIUS Client program. <br>Usage:<p>
 *   <pre>
 *     RadClient Arguments: [options] server secret file
 *     	server			= RADIUS server hostname or ip
 *     	secret			= Shared secret to use
 *     	file			= File containing the attribute name/value pairs
 * 
 *     Options:
 *     	-d java-class	= Java class name of the attribute dictionary
 *     	                 (default: net.jradius.dictionary.RadiusDictionaryImpl)
 *     	-s java-class	= Java class name of the attribute checker
 *     	                 (e.g net.jradius.standard.WISPrStandard)
 *     	-a auth-mode	= Either PAP (default), CHAP, MSCHAP, MSCHAPv2, 
 *     	                 EAP-MD5, EAP-MSCHAPv2 or EAP-TLS
 *     	-T tunnel-mode	= Only EAP-TTLS currently supported
 *      -A              = Generate a unique Acct-Session-Id in Accounting Requests
 *      -C              = Turn OFF Class attribute support
 *   
 *   </pre>
 * 	Also see http://jradius.net/
 * <p>
 * If the packet attribtue list contains "Acct-Status-Type", then RadClient will know the
 * packet is an AccountingRequest. Otherwise, it assumes you are sending a AccessRequest.
 * The attribute file can contain multiple packets separated by a <b>single</b> blank line.
 * You can optionally also "sleep" for a number of seconds between packets as shown in this
 * example attributes file (authentication, following by a start, interim, and stop accounting):
 * <p>
 * <pre>
 * # Lines starting with a hash are comments
 * User-Name = test
 * User-Password = test
 * 
 * sleep 1
 * 
 * User-Name = test
 * Acct-Status-Type = Start
 *
 * sleep 1
 * 
 * User-Name = test
 * Acct-Status-Type = Interim-Update
 *
 * sleep 1
 * 
 * User-Name = test
 * Acct-Status-Type = Stop
 * </pre><p>
 * 
 * @author David Bird
 */
public class RadClient
{
    private static RadiusClient client;
    
    private static void usage()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("RadClient Arguments: [options] server secret file\n");
        sb.append("\tserver			= RADIUS server hostname or ip\n");
        sb.append("\tsecret			= Shared secret to use\n");
        sb.append("\tfile			= File containing the attribute name/value pairs\n");
        sb.append("\nOptions:\n");
        sb.append("\t-d java-class	= Java class name of the attribute dictionary\n");
        sb.append("\t                 (default: net.jradius.dictionary.RadiusDictionaryImpl)\n");
        sb.append("\t-s java-class	= Java class name of the attribute checker\n");
        sb.append("\t                 (e.g net.jradius.standard.WISPrStandard)\n");
        sb.append("\t-a auth-mode	= Either PAP (default), CHAP, MSCHAP, MSCHAPv2,\n");
        sb.append("\t                 EAP-MD5, EAP-MSCHAPv2, or EAP-TLS (see below for format)\n");
        sb.append("\t                 (provide the plain-text password in User-Password)\n");
        sb.append("\t-T tunnel-mode	= Only EAP-TTLS is currently supported (see below for info)\n");
        sb.append("\t-A		= Generate a unique Acct-Session-Id in Accounting Requests\n");
        sb.append("\t-C		= Turn OFF Class attribute support\n");
        sb.append("\nUsing EAP-TLS and EAP-TTLS:\n");
        sb.append("\n");
        sb.append("More information at http://jradius.net/\n");
        sb.append("\n");
        System.out.print(sb.toString());
    }
    
    private static boolean loadAttributes(AttributeList list, BufferedReader in) throws IOException
    {
        String line;
        boolean allowLine = true;
        
        while ((line = in.readLine()) != null)
        {
            line = line.trim();
            if (line.startsWith("#")) continue;
            
            if (line.equals("")) 
            {
                if (!allowLine) break;
                continue;
            }

            if (line.startsWith("sleep "))
            {
                allowLine = true;
                try
                {
                    int i = Integer.parseInt(line.substring(6));
                    if (i > 0) Thread.sleep(i * 1000);
                } catch(Exception e) { e.printStackTrace(); }
                continue;
            }

            allowLine = false;

            try
            {
                RadiusAttribute a = AttributeFactory.attributeFromString(line);
                if (a != null) list.add(a, false);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        
        return (line != null);
    }
   
    public static void main(String[] args)
    {
        Locale.setDefault(Locale.US);
        Getopt g = new Getopt("RadClient", args, "s:a:d:t:p:T:A");

        String dictClass = "net.jradius.dictionary.AttributeDictionaryImpl";
        String check  = null;

        RadiusAuthenticator auth = null;
        int authPort = 1812;
        int acctPort = 1813;
        int timeout = 60;

        boolean sendbackClass = true;
        boolean tunneledRequest = false;
        boolean generateSessionId = false;
        RadiusAttribute generatedSessionId = null;

        int op;
        while ((op = g.getopt()) != -1)
        {
            switch(op)
            {
            	case 'A':
            	{
            	    generateSessionId = true;
            	}
            	break;
            	
            	case 's':
            	{
            	    check = g.getOptarg();
            	}
            	break;

            	case 'd':
            	{
            	    dictClass = g.getOptarg();
            	}
            	break;

            	case 't':
            	{
            	    timeout = Integer.parseInt(g.getOptarg());
            	}
            	break;

            	case 'p':
            	{
            	    authPort = Integer.parseInt(g.getOptarg());
            	    acctPort = authPort + 1;
            	}
            	break;

            	case 'C':
            	{
            	    sendbackClass = false;
            	}
            	break;
            	
            	case 'T':
            	{
            	    tunneledRequest = true;
            	}
            	// fall-through
            	case 'a':
            	{
            	    String arg = g.getOptarg();
            	    if ((auth = RadiusClient.getAuthProtocol(arg)) == null)
            	    {
            	        System.err.println("Unsupported authentication protocol " + arg);
            	    }
            	}
            	break;

            	default:
            	{
            	    usage();
            		return;
            	}
            }
        }
        
        int gidx = g.getOptind();
        
        if (args.length - gidx < 3)
        {
            usage();
            return;
        }
        
        String host   = args[gidx];
        String secret = args[gidx + 1];
        String file   = args[gidx + 2];
        
        AttributeFactory.loadAttributeDictionary(dictClass);
        
        try
        {
            boolean active = true;
            InetAddress inet = InetAddress.getByName(host);
            BufferedReader in = new BufferedReader(new FileReader(file));
            RadiusStandard standard = null;
            
            client = new RadiusClient(inet, secret, authPort, acctPort, timeout);

            if (check != null)
            {
                Class c = Class.forName(check);
                try
                {
                    standard = (RadiusStandard)c.newInstance();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            RadiusAttribute classAttr = null;

            while (active)
            {
                AttributeList attributes = new AttributeList();
                active = loadAttributes(attributes, in);
                if (attributes.getSize() == 0) continue;
                if (classAttr != null) attributes.add(classAttr);
            
                RadiusRequest request;
                RadiusResponse reply;
                RadiusAttribute attr;
                Long status;
                
                if ((attr = attributes.get(AttributeDictionary.ACCT_STATUS_TYPE)) != null &&
                    (status = (Long)attr.getValue().getValueObject()) != null &&
                    status.intValue() <= 3) 
                {
                    request = new AccountingRequest(client, attributes);
                    if (generateSessionId)
                    {
                        if (generatedSessionId == null)
                        {
                            generatedSessionId = AttributeFactory.newAttribute("Acct-Session-Id", "JRadius-" + RadiusRandom.getRandomString(16), "=");
                        }
                        request.overwriteAttribute(generatedSessionId);
                    }
                    reply = (RadiusResponse)client.accounting((AccountingRequest)request, 5);
                }
                else
                {
                    request = new AccessRequest(client, attributes);
                    if (tunneledRequest)
                    {
                        if (auth instanceof TunnelAuthenticator)
                        {
                            AttributeList attrs = new AttributeList();
                            active = loadAttributes(attrs, in);
                            ((TunnelAuthenticator)auth).setTunneledAttributes(attrs);
                        }
                        else
                        {
                	        System.err.println("Error: -T option used with a non-tunnel authenticator: " + auth.getClass().getName());
                        }
                    }
                    reply = client.authenticate((AccessRequest)request, auth, 5);
                    if (reply != null) classAttr = reply.findAttribute("Class");
                }

                if (standard != null)
                {
                    try
                    {
                        standard.checkPacket(request);
                    }
                    catch (StandardViolatedException e)
                    {
                        System.err.println("Warning: Access Request Missing " + standard.getName() + " Attributes:\n\t" + e.listAttributes());
                    }

                    try
                    {
                        standard.checkPacket(reply);
                    }
                    catch (StandardViolatedException e)
                    {
                        System.err.println("Warning: Access Reply Missing " + standard.getName() + " Attributes:\n\t" + e.listAttributes());
                    }
                }

                if (request != null) System.out.println(request.toString());
                if (reply != null)   System.out.println(reply.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
