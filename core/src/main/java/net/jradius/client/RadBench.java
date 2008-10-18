/**
 * RadBench
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

import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;



/**
 * A Java RADIUS Benchmarking program. <br>Usage:<p>
 *   <pre>
 *     RadBench Arguments: [options] server secret file
 *     	server			= RADIUS server hostname or ip
 *     	secret			= Shared secret to use
 *     	file			= File containing the attribute name/value pairs
 * 
 *     Options:
 *     	-d java-class	= Java class name of the attribute dictionary
 *     	                 (default: net.jradius.dictionary.RadiusDictionaryImpl)
 *     	-a auth-mode	= Either PAP (default), CHAP, MSCHAP, MSCHAPv2, 
 *     	                 EAP-MD5, or EAP-MSCHAPv2
 *     	                 (always provide the plain-text password in User-Password)
 *   </pre>
 * <p>
 * If the packet attribtue list contains "Acct-Status-Type", then RadClient will know the
 * packet is an AccountingRequest. Otherwise, it assumes you are sending a AccessRequest.
 * The attribute file can contain multiple packets separated by a <b>single</b> blank line.
 * You can optionally also "sleep" for a number of seconds between packets as shown in this
 * example attributes file (authentication, following by a start, interim, and stop accounting):
 * @author David Bird
 */
public class RadBench
{
    protected static RadiusClient client;
    protected static RadiusAuthenticator auth;
    
    protected static void usage()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("RadBench Arguments: [options] server secret file\n");
        sb.append("\tserver			= RADIUS server hostname or ip\n");
        sb.append("\tsecret			= Shared secret to use\n");
        sb.append("\tfile			= File containing the attribute name/value pairs\n");
        sb.append("\nOptions:\n");
        sb.append("\t-d java-class	= Java class name of the attribute dictionary\n");
        sb.append("\t                 (default: net.jradius.dictionary.RadiusDictionaryImpl)\n");
        sb.append("\t-a auth-mode	= Either PAP (default), CHAP, MSCHAP, MSCHAPv2,\n");
        sb.append("\t                 EAP-MD5, or EAP-MSCHAPv2\n");
        sb.append("\t                 (always provide the plain-text password in User-Password)\n");
        sb.append("\n");
        System.out.print(sb.toString());
    }
    
    protected static boolean loadAttributes(AttributeList list, BufferedReader in) throws IOException
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
        Getopt g = new Getopt("RadBench", args, "a:d:t:p:r:c:");

        String dictClass = "net.jradius.dictionary.AttributeDictionaryImpl";

        int authPort = 1812;
        int acctPort = 1813;
        int timeout = 60;
        
        int requesters = 5;
        int requests = 10;

        int op;
        while ((op = g.getopt()) != -1)
        {
            switch(op)
            {
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

            	case 'a':
            	{
            	    String arg = g.getOptarg();
            	    if ((auth = RadiusClient.getAuthProtocol(arg)) == null)
            	    {
            	        System.err.println("Unsupported authentication protocol " + arg);
            	    }
            	}
            	break;

            	case 'r':
            	{
            	    requesters = Integer.parseInt(g.getOptarg());
            	}
            	break;

            	case 'c':
            	{
            	    requests = Integer.parseInt(g.getOptarg());
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
            InetAddress inet = InetAddress.getByName(host);
            client = new RadiusClient(inet, secret, authPort, acctPort, timeout);

            BenchThread thread[] = new BenchThread[requesters];
            int i = 0;
            
            RadiusLog.info("Starting Requester Threads...");
            long startTime = System.currentTimeMillis();
            
            for (i = 0; i < requesters; i++)
            {
                (thread[i] = new BenchThread(requests, file)).start();
            }
            
            int sent = 0;
            int received = 0;

            for (i = 0; i < thread.length; i++)
            {
                thread[i].join();
                sent += thread[i].getSent();
                received += thread[i].getReceived();
            }

            long endTime = System.currentTimeMillis();
            RadiusLog.info("Completed.");
            RadiusLog.info("Results:");
            RadiusLog.info("	Requesters:       " + requesters);
            RadiusLog.info("	Requests:         " + requests);
            RadiusLog.info("	Packets Sent:     " + sent);
            RadiusLog.info("	Packets Received: " + received);
            RadiusLog.info("	Secconds:         " + (double)(endTime - startTime) / 1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static class BenchThread extends Thread
    {
        int requests;
        String file;
        int sent = 0;
        int received = 0;
        
        BenchThread(int requests, String file) 
        {
            this.requests = requests;
            this.file = file;
            this.setDaemon(true);
        }

        public void run()
        {
            try
            {
                runRequester();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public void runRequester() throws Exception
        {
            while (requests-- > 0)
            {
                BufferedReader in = new BufferedReader(new FileReader(file));
                boolean active = true;

                while (active)
                {
                    AttributeList attributes = new AttributeList();
                    active = loadAttributes(attributes, in);
                    if (attributes.getSize() == 0) continue;
                
                    RadiusRequest request;
                    RadiusResponse reply;
                    RadiusAttribute attr;
                    Long status;
                    
                    if ((attr = attributes.get(AttributeDictionary.ACCT_STATUS_TYPE)) != null &&
                        (status = (Long)attr.getValue().getValueObject()) != null &&
                        status.intValue() <= 3) 
                    {
                        request = new AccountingRequest(client, attributes);
                        reply = (RadiusResponse)client.accounting((AccountingRequest)request, 5);
                    }
                    else
                    {
                        request = new AccessRequest(client, attributes);
                        reply = client.authenticate((AccessRequest)request, auth, 5);
                    }

                    sent++;
                    
                    if (reply == null)
                    {
                        RadiusLog.error("Timed out on request! Not a good benchmark!");
                    }
                    else
                    {
                        received++;
                    }
                }
            }
        }
        
        /**
         * @return Returns the received.
         */
        public int getReceived()
        {
            return received;
        }
        
        /**
         * @return Returns the sent.
         */
        public int getSent()
        {
            return sent;
        }
    }
}
