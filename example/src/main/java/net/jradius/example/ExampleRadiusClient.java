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

package net.jradius.example;

import java.net.InetAddress;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.dictionary.Attr_AcctInputOctets;
import net.jradius.dictionary.Attr_AcctOutputOctets;
import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_AcctSessionTime;
import net.jradius.dictionary.Attr_AcctStatusType;
import net.jradius.dictionary.Attr_AcctTerminateCause;
import net.jradius.dictionary.Attr_NASPort;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.util.RadiusRandom;

/**
 * An example use of the JRadius RADIUS Client API
 *
 * @author David Bird
 */
public class ExampleRadiusClient
{
    public static void main(String[] args)
    {
        try
        {
            AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
            
            InetAddress host = InetAddress.getByName("localhost");
            RadiusClient rc = new RadiusClient(host, "test", 1812, 1813, 1000);
            
            AttributeList attrs = new AttributeList();
            attrs.add(new Attr_UserName("test"));
            attrs.add(new Attr_NASPortType(Attr_NASPortType.Wireless80211));
            attrs.add(new Attr_NASPort(new Long(1)));
            
            RadiusRequest request = new AccessRequest(rc, attrs);
            request.addAttribute(new Attr_UserPassword("test"));

            System.out.println("Sending:\n" + request.toString());

            RadiusResponse reply = rc.authenticate((AccessRequest)request, new MSCHAPv2Authenticator(), 5);

            System.out.println("Received:\n" + reply.toString());

            
            boolean isAuthenticated = (reply instanceof AccessAccept);
            
            String replyMessage = (String)reply.getAttributeValue(Attr_ReplyMessage.TYPE);
            
            if (replyMessage != null)
            {
                System.out.println("Reply Message: " + replyMessage);
            }

            
            if (!isAuthenticated) return;

            attrs.add(new Attr_AcctSessionId(RadiusRandom.getRandomString(24)));
            
            request = new AccountingRequest(rc, attrs);
            request.addAttribute(new Attr_AcctStatusType("Start"));

            System.out.println("Sending:\n" + request.toString());
            
            reply = rc.accounting((AccountingRequest)request, 5);

            System.out.println("Received:\n" + reply.toString());

            request = new AccountingRequest(rc, attrs);
            request.addAttribute(new Attr_AcctStatusType("Interim-Update"));
            request.addAttribute(new Attr_AcctInputOctets(new Long(42949670L)));
            request.addAttribute(new Attr_AcctOutputOctets(new Long(5)));
            request.addAttribute(new Attr_AcctSessionTime(new Long(10)));

            System.out.println("Sending:\n" + request.toString());

            reply = rc.accounting((AccountingRequest)request, 5);

            System.out.println("Received:\n" + reply.toString());

            request = new AccountingRequest(rc, attrs);
            request.addAttribute(new Attr_AcctStatusType("Interim-Update"));
            request.addAttribute(new Attr_AcctInputOctets(new Long(429496700L)));
            request.addAttribute(new Attr_AcctOutputOctets(new Long(5)));
            request.addAttribute(new Attr_AcctSessionTime(new Long(30)));

            System.out.println("Sending:\n" + request.toString());

            reply = rc.accounting((AccountingRequest)request, 5);

            System.out.println("Received:\n" + reply.toString());

            request = new AccountingRequest(rc, attrs);
            request.addAttribute(new Attr_AcctStatusType("Stop"));
            request.addAttribute(new Attr_AcctInputOctets(new Long(4294967000L)));
            request.addAttribute(new Attr_AcctOutputOctets(new Long(10)));
            request.addAttribute(new Attr_AcctSessionTime(new Long(60)));
            request.addAttribute(new Attr_AcctTerminateCause(Attr_AcctTerminateCause.UserRequest));

            System.out.println("Sending:\n" + request.toString());

            reply = rc.accounting((AccountingRequest)request, 5);

            System.out.println("Received:\n" + reply.toString());
}
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
