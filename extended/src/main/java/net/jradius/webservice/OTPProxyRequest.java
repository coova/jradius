/**
 * JRadius - A Radius Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2007 David Bird <david@coova.com>
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

package net.jradius.webservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.EAPAuthenticator;
import net.jradius.dictionary.Attr_AcctDelayTime;
import net.jradius.dictionary.Attr_AcctInputGigawords;
import net.jradius.dictionary.Attr_AcctInputOctets;
import net.jradius.dictionary.Attr_AcctInputPackets;
import net.jradius.dictionary.Attr_AcctOutputGigawords;
import net.jradius.dictionary.Attr_AcctOutputOctets;
import net.jradius.dictionary.Attr_AcctOutputPackets;
import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_AcctSessionStartTime;
import net.jradius.dictionary.Attr_AcctSessionTime;
import net.jradius.dictionary.Attr_AcctStatusType;
import net.jradius.dictionary.Attr_AcctTerminateCause;
import net.jradius.dictionary.Attr_CalledStationId;
import net.jradius.dictionary.Attr_CallingStationId;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_NASPort;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_ServiceType;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.vsa_jradius.Attr_JRadiusSessionId;
import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.realm.JRadiusRealm;
import net.jradius.server.JRadiusThread;
import net.jradius.util.Base64;
import net.jradius.util.RadiusRandom;

/**
 * OTP Proxy Web Service Request. This thread give the client a one-time
 * username and password and does the EAP proxy in a RadiusClient for the request.
 *
 * @author David Bird
 */
public class OTPProxyRequest extends JRadiusThread implements WebServiceRequestObject
{
    private final WebServiceListener wsListener;
    private String userName;
    private JRadiusRealm radiusRealm;
    private String otpName;
    private String otpPassword;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private RadiusClient radiusClient;
    private AttributeList reqList = new AttributeList();
    
    private long timeout = 30000;

    private class PacketLocker
    {
        RadiusPacket packet;
        public synchronized RadiusPacket getPacket() throws InterruptedException
        {
            if (packet == null) wait(timeout);
            return packet;
        }
        public synchronized void setPacket(RadiusPacket packet)
        {
            this.packet = packet;
            notify();
        }
    }
    
    private PacketLocker accessRequest = new PacketLocker();
    private PacketLocker accessResponse = new PacketLocker();
    
    public OTPProxyRequest(WebServiceListener wsListener, String userName, JRadiusRealm realm, Socket socket, BufferedReader reader, BufferedWriter writer) throws OTPProxyException
    {
        this.wsListener = wsListener;
        this.userName = userName;
        this.otpName = RadiusRandom.getRandomString(16);
        this.otpPassword = RadiusRandom.getRandomString(16);
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.radiusRealm = realm;

        try
        {
            radiusClient = new RadiusClient(InetAddress.getByName(this.radiusRealm.getServer()), this.radiusRealm.getSharedSecret());
        }
        catch (Exception e)
        {
            throw new OTPProxyException(e.getMessage());
        }
    }
    
    public void delete()
    {
        shutdown();
    }

    public String getKey()
    {
        return getOtpName();
    }

    public void run()
    {
        try
        {
            writer.write(getOtpName());
            writer.write("\n");
            writer.write(getOtpPassword());
            writer.write("\n");
            writer.flush();

            RadiusRequest request = getAccessRequest();

            if (request == null)
            {
                RadiusLog.error("we never got the access request");
                abort(null);
                return;
            }
            
            AttributeList attrs = request.getAttributes();

            for (Iterator i=attrs.getAttributeList().iterator(); i.hasNext();)
            {
                RadiusAttribute at = (RadiusAttribute)i.next();
                long type = at.getFormattedType();
                if (type == Attr_CalledStationId.TYPE ||
                    type == Attr_CallingStationId.TYPE ||
                    type == Attr_NASIPAddress.TYPE ||
                    type == Attr_NASIdentifier.TYPE ||
                    type == Attr_ServiceType.TYPE ||
                    type == Attr_JRadiusSessionId.TYPE ||
                    type == Attr_NASPortType.TYPE ||
                    type == Attr_NASPort.TYPE)
                        reqList.add(AttributeFactory.newAttribute(type, at.getValue().getBytes()));
            }

            reqList.add(new Attr_UserName(userName));
            reqList.add(new Attr_AcctSessionId(RadiusRandom.getRandomString(16)));

            AccessRequest realRequest = new AccessRequest(radiusClient, reqList);
            
            RadiusLog.debug(
                    "------------------------------------------------\n"+
                    "OTP Proxy Request:\n" + realRequest.toString()+
                    "------------------------------------------------\n");

            RadiusResponse reply = radiusClient.authenticate(realRequest, new EAPRelayAuthenticator(), 5);
            
            setAccessResponse(reply);
        }
        catch (Exception e)
        {
            RadiusLog.warn(e.getMessage(), e);
            abort(e);
        }
    }
    
    public String getOtpName()
    {
        return otpName;
    }
    
    public String getOtpPassword()
    {
        return otpPassword;
    }
    
    public String getUserName()
    {
        return userName;
    }
    
    public JRadiusRealm getRadiusRealm()
    {
        return radiusRealm;
    }
    
    public void setAccessRequest(RadiusRequest accessRequest)
    {
        this.accessRequest.setPacket(accessRequest);
    }

    public void setAccessResponse(RadiusResponse accessResponse)
    {
        this.accessResponse.setPacket(accessResponse);
    }
    
    public RadiusRequest getAccessRequest() throws InterruptedException
    {
        RadiusPacket p = this.accessRequest.getPacket();
        if (p == null)
        {
            RadiusLog.error("we never got the access request");
            abort(null);
        }
        return (RadiusRequest)p;
    }
    
    public RadiusResponse getAccessResponse() throws InterruptedException
    {
        RadiusPacket p = this.accessResponse.getPacket();
        if (p == null)
        {
            RadiusLog.error("we never got the access response");
            abort(null);
        }
        return (RadiusResponse)p;
    }

    public void updateAccounting(AccountingRequest acctRequest) throws RadiusException
    {
        AccountingRequest newRequest = new AccountingRequest(radiusClient, reqList);

        AttributeList attrs = acctRequest.getAttributes();
        for (Iterator i=attrs.getAttributeList().iterator(); i.hasNext();)
        {
            RadiusAttribute at = (RadiusAttribute)i.next();
            long type = at.getFormattedType();
            if (type == Attr_AcctInputOctets.TYPE ||
                type == Attr_AcctOutputOctets.TYPE ||
                type == Attr_AcctInputGigawords.TYPE ||
                type == Attr_AcctOutputGigawords.TYPE ||
                type == Attr_AcctInputPackets.TYPE ||
                type == Attr_AcctOutputPackets.TYPE ||
                type == Attr_AcctTerminateCause.TYPE ||
                type == Attr_AcctSessionStartTime.TYPE ||
                type == Attr_AcctDelayTime.TYPE ||
                type == Attr_AcctSessionTime.TYPE ||
                type == Attr_AcctStatusType.TYPE)
                newRequest.addAttribute(AttributeFactory.newAttribute(type, at.getValue().getBytes()));
        }
        radiusClient.accounting(newRequest, 2);
    }
    
    private byte[] readData() 
    {
        try
        {
            String line = reader.readLine();
            RadiusLog.debug("OtpProxy: read-"+line);
            if (line.startsWith("eap:"))
            {
                return Base64.decode(line.substring(4));
            }
        }
        catch (Exception e)
        {
            abort(e);
        }
        return null;
    }
    
    private byte[] relayEAP(byte[] eapIn)
    {
        try
        {
            String line = "eap:"+Base64.encodeBytes(eapIn, Base64.DONT_BREAK_LINES)+"\n";
            writer.write(line);
            writer.flush();
            RadiusLog.debug("OtpProxy: write-"+line);
            return readData();
        }
        catch (IOException e)
        {
            abort(e);
        }
        return null;
    }
    
    public void abort(Exception e)
    {
        if (e==null)
        {
            RadiusLog.error("aborting otp proxy request");
        }
        else
        {
            RadiusLog.error(e.getMessage(), e);
        }

        wsListener.remove(this);
        shutdown();
    }
    
    public void shutdown()
    {
        try
        {
            writer.close();
            reader.close();
            socket.close();
            radiusClient.close();
        }
        catch (IOException e) { }
    }

    private class EAPRelayAuthenticator extends EAPAuthenticator
    {
        public void processRequest(RadiusPacket p) throws RadiusException
        {
            p.addAttribute(AttributeFactory.newAttribute(AttributeDictionary.EAP_MESSAGE, readData()));
        }
        
        public byte[] doEAP(byte[] eapReply)
        {
            return relayEAP(eapReply);
        }

        public byte[] doEAPType(byte id, byte[] data)
        {
            return null;
        }

        public String getAuthName()
        {
            return "OTPProxy-EAP-Callback";
        }
    }
}
