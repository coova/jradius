/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2007-2008 David Bird <david@coova.com>
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

package net.jradius.log;

import java.net.InetAddress;
import java.util.Date;

import net.jradius.dictionary.Attr_ClientIPAddress;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.exception.RadiusException;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;
import net.jradius.session.JRadiusSession;


/**
 * The JRadius Log Entry Object (bean).
 * @author David Bird
 */
public class RadiusLogEntry implements JRadiusLogEntry
{
	private static final long serialVersionUID = 1L;
	protected transient JRadiusSession session;
    protected Integer code;
    protected String type;
    protected String message;
    protected String packetId;
    protected String clientIPAddress;
    protected String nasIdentifier;
    protected String userName;
    protected String realm;
    protected Integer userId;
    
    protected String inboundRequest;
    protected String outboundRequest;
    protected String inboundReply;
    protected String outboundReply;
    
    protected boolean finished = true;
    protected boolean committed = false;
    
    protected Date lastUpdate;
    
    public RadiusLogEntry()
    {
    }
    
    public RadiusLogEntry(JRadiusSession session, String packetId)
    {
        this.session = session;
        setPacketId(packetId);
    }
    
	public void init(JRadiusRequest request, JRadiusSession session) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();

        setType(request);
        
        // The client IP should never be changing
        if (getClientIPAddress() == null)
        {
            InetAddress clientAddress = (InetAddress) req.getAttributeValue(Attr_ClientIPAddress.TYPE);
            if (clientAddress != null)
                setClientIPAddress(clientAddress.getHostAddress());
        }
        
        // We want the original NAS-Identifier, not any rewrite
        // Grab the value at the first call to InitHandler
        if (getNasIdentifier() == null)
        {
            String nasId = (String) req.getAttributeValue(Attr_NASIdentifier.TYPE);
            setNasIdentifier(nasId);
        }

        if (getUserName() == null)
            setUserName(session.getUsername());

        if (getRealm() == null)
            setRealm(session.getRealm());

        // The inbound request should be got at the preacct or authorize
        // at the first position in the chain
        if (getInboundRequest() == null)
            setInboundRequest(req.toString(false, true));
	}

	protected Object setValue(Object n, Object o)
    {
        if (o == null || !o.equals(n)) { committed = false; return n; }
        return o;
    }
    
    /**
     * @return Returns the sessionKey.
     */
    public String getSessionKey()
    {
        return session.getSessionKey();
    }

    /**
     * @return Returns the code.
     */
    public Integer getCode()
    {
        return code;
    }
    /**
     * @param code The code to set.
     */
    public void setCode(Integer code)
    {
        this.code = (Integer)setValue(code, this.code);
    }
    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type)
    {
        this.type = (String)setValue(type, this.type);
    }
    
    public void setType(JRadiusRequest request) throws RadiusException
    {
        switch(request.getType())
        {
        	case JRadiusServer.JRADIUS_pre_proxy:
        	case JRadiusServer.JRADIUS_post_proxy:
        	case JRadiusServer.JRADIUS_post_auth:
        	case JRadiusServer.JRADIUS_checksimul:
        	break;
            case JRadiusServer.JRADIUS_preacct:
        	case JRadiusServer.JRADIUS_accounting:
        	{
        	    AccountingRequest req = (AccountingRequest)request.getRequestPacket();
        	    switch(req.getAccountingStatusType())
        	    {
        	    	case AccountingRequest.ACCT_STATUS_START:
                    case AccountingRequest.ACCT_STATUS_ACCOUNTING_ON:
        	    	    setType("start");
        	    		break;
        	    	case AccountingRequest.ACCT_STATUS_STOP:
                    case AccountingRequest.ACCT_STATUS_ACCOUNTING_OFF:
        	    	    setType("stop");
        	    		break;
        	    	case AccountingRequest.ACCT_STATUS_INTERIM:
        	    	    setType("interim");
        	    		break;
        	    	default:
        	    	    setType("accounting");
        	    		break;
        	    }
        	}
        	break;
            default:
            {
        	    setType(request.getTypeString());
            }
        }
    }
    
    /**
     * @return Returns the message.
     */
    public String getMessage()
    {
        return message;
    }
    
    /**
     * @param message The message to set.
     */
    public void setMessage(String message)
    {
        this.message = (String)setValue(message, this.message);
    }
    
    public void addMessage(String message)
    {
        StringBuffer sb = new StringBuffer();
        if (this.message != null) sb.append(this.message).append(", ");
        sb.append(message);
        setMessage(sb.toString());
    }
    
    /**
     * @return Returns the clientIP.
     */
    public String getClientIPAddress()
    {
        return clientIPAddress;
    }
    
    /**
     * @param clientIP The clientIP to set.
     */
    public void setClientIPAddress(String clientIP)
    {
        this.clientIPAddress = (String)setValue(clientIP, this.clientIPAddress);
    }
    
    /**
     * @return Returns the nasId.
     */
    public String getNasIdentifier()
    {
        return nasIdentifier;
    }
    /**
     * @param nasId The nasId to set.
     */
    public void setNasIdentifier(String nasId)
    {
        this.nasIdentifier = (String)setValue(nasId, this.nasIdentifier);
    }
    
    public String getUserName()
    {
        return userName;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    
    public String getRealm()
    {
        return realm;
    }
    public void setRealm(String realm)
    {
        this.realm = realm;
    }
    /**
     * @return Returns the userId.
     */
    public Integer getUserId()
    {
        return userId;
    }
    /**
     * @param userId The userId to set.
     */
    public void setUserId(Integer userId)
    {
        this.userId = (Integer)setValue(userId, this.userId);
    }
    
    /**
     * @return Returns the packetId.
     */
    public String getPacketId()
    {
        return packetId;
    }
    /**
     * @param packetId The packetId to set.
     */
    public void setPacketId(String packetId)
    {
        this.packetId = packetId;
    }
    /**
     * @return Returns the inboundReply.
     */
    public String getInboundReply()
    {
        return inboundReply;
    }
    /**
     * @param inboundReply The inboundReply to set.
     */
    public void setInboundReply(String inboundReply)
    {
        this.inboundReply = (String)setValue(inboundReply, this.inboundReply);
    }
    /**
     * @return Returns the inboundRequest.
     */
    public String getInboundRequest()
    {
        return inboundRequest;
    }
    /**
     * @param inboundRequest The inboundRequest to set.
     */
    public void setInboundRequest(String inboundRequest)
    {
        this.inboundRequest = (String)setValue(inboundRequest, this.inboundRequest);
    }
    /**
     * @return Returns the outboundReply.
     */
    public String getOutboundReply()
    {
        return outboundReply;
    }
    /**
     * @param outboundReply The outboundReply to set.
     */
    public void setOutboundReply(String outboundReply)
    {
        this.outboundReply = (String)setValue(outboundReply, this.outboundReply);
    }
    /**
     * @return Returns the outboundRequest.
     */
    public String getOutboundRequest()
    {
        return outboundRequest;
    }
    /**
     * @param outboundRequest The outboundRequest to set.
     */
    public void setOutboundRequest(String outboundRequest)
    {
        this.outboundRequest = (String)setValue(outboundRequest, this.outboundRequest);
    }
    
    /**
     * @return Returns the finished.
     */
    public boolean isFinished()
    {
        return finished;
    }
    /**
     * @param finished The finished to set.
     */
    public void setFinished(boolean finished)
    {
        this.finished = finished;
    }
    
    /**
     * @return Returns the committed.
     */
    public boolean isCommitted()
    {
        return committed;
    }
    /**
     * @param committed The committed to set.
     */
    public void setCommitted(boolean committed)
    {
        this.committed = committed;
    }

    public String toString()
    {
        return "RadiusLogEntry: { type = " + type + ", packetId = " + packetId + " }: " + getMessage();
    }

    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }
}
