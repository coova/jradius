/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006 David Bird <david@coova.com>
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

package net.jradius.session;

import java.io.Serializable;

import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_CalledStationId;
import net.jradius.dictionary.Attr_CallingStationId;
import net.jradius.dictionary.Attr_Class;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_ServiceType;
import net.jradius.dictionary.Attr_State;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPClientHardwareAddress;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPGatewayIPAddress;
import net.jradius.dictionary.vsa_dhcp.Attr_DHCPTransactionId;
import net.jradius.dictionary.vsa_jradius.Attr_JRadiusSessionId;
import net.jradius.exception.RadiusException;
import net.jradius.handler.RadiusSessionHandler;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.DHCPPacket;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.value.AttributeValue;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;

/**
 * The Default SessionKeyProvider.
 * @author David Bird
 */
public class RadiusSessionKeyProvider implements SessionKeyProvider
{
    /**
     * Generates the session key for the given session. If the key is changing, as
     * in the case when we move from authentication to accounting, this method will
     * return an Object[2] which instructs the session manage to "rehash" the session
     * under a new key (for uniqueness).
     * 
     * @param request The JRadiusRequest
     * @return the session key, or an array of 2 keys, the second replacing the first
     * as the session hash key.
     * @throws RadiusException
     */
    public Serializable getRequestSessionKey(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        
        if (req == null)
        {
            return null;
        }
        else if (req instanceof AccessRequest)
        {
            return getAccessRequestKey(request);
        }
        else if (req instanceof DHCPPacket)
        {
        	return getDHCPRequestKey(request);
        }
        else if (req instanceof AccountingRequest) 
        {
            int type = request.getType();
            int status = ((AccountingRequest)req).getAccountingStatusType();

            Serializable key = getAccountingRequestKey(request);

            if (type == JRadiusServer.JRADIUS_preacct && 
                 (  status == AccountingRequest.ACCT_STATUS_START ||
                    status == AccountingRequest.ACCT_STATUS_ACCOUNTING_ON) )
            {
                // rekey the request during pre-accounting
                return new Serializable[] { getAccessRequestKey(request), key };
            }

            return key;
        }
        
        return null;
    }
    

    /**
     * Generates a session hash key based on access-request attributes.
     * @param request The JRadiusRequest
     * @return the session key
     * @throws RadiusException
     */
    public Serializable getAccessRequestKey(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        StringBuffer sb = new StringBuffer((String)request.getSender());
        sb.append(":").append(getKeyFromAttributeType(req, Attr_NASIPAddress.TYPE, true));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_NASIdentifier.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_UserName.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_CallingStationId.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_CalledStationId.TYPE, false));
        return sb.toString();
    }

    /**
     * Generates a session hash key based on dhcp-packet attributes.
     * @param request The JRadiusRequest
     * @return the session key
     * @throws RadiusException
     */
    public Serializable getDHCPRequestKey(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        StringBuffer sb = new StringBuffer((String)request.getSender());
        sb.append(":").append(getKeyFromAttributeType(req, Attr_DHCPGatewayIPAddress.TYPE, true));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_DHCPTransactionId.TYPE, true));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_DHCPClientHardwareAddress.TYPE, true));
        return sb.toString();
    }

    /**
     * Gets the JRadius Class attribute containing the session key, if attribute exists.
     * @param request The JRadiusRequest
     * @return the session key
     * @throws RadiusException
     */
    public Serializable getClassKey(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        // Look for our own Class attribute value
        byte[] bClass = (byte[]) req.getAttributeValue(Attr_Class.TYPE);
        if (bClass != null)
        {
            String sClass = new String(bClass);
            if (sClass.startsWith(RadiusSessionHandler.ClassPrefix))
            {
                RadiusLog.debug("Using " + sClass);
                return sClass.substring(RadiusSessionHandler.ClassPrefix.length());
            }
        }
        bClass = (byte[]) req.getAttributeValue(Attr_State.TYPE);
        if (bClass != null)
        {
            String sClass = new String(bClass);
            if (sClass.startsWith(RadiusSessionHandler.ClassPrefix))
            {
                RadiusLog.debug("Using " + sClass);
                return sClass.substring(RadiusSessionHandler.ClassPrefix.length());
            }
        }
        return null;
    }
    
    /**
     * Generates a session hash key based on accounting-request attribtues.
     * @param request The JRadiusRequest
     * @return the session key
     * @throws RadiusException
     */
    public Serializable getAccountingRequestKey(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        StringBuffer sb = new StringBuffer((String)request.getSender());
        sb.append(":").append(getKeyFromAttributeType(req, Attr_NASIPAddress.TYPE, true));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_AcctSessionId.TYPE, true));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_NASIdentifier.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_UserName.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_CallingStationId.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_CalledStationId.TYPE, false));
        return sb.toString();
    }
    
    /**
     * Gets the session key based on the JRadiusSessionId attribute in the configItems.
     * @param request The JRadiusRequest
     * @return the session key
     * @throws RadiusException
     */
    public Serializable getAppSessionKey(JRadiusRequest request) throws RadiusException
    {
        AttributeList ci = request.getConfigItems();

        // If we already have seen this packet (in the chain or
        // within the same FreeRADIUS request - multiple calls to JRadius)
        // we can grab the JRadius-Session-Id.
        RadiusAttribute a = ci.get(Attr_JRadiusSessionId.TYPE);
        if (a != null) return a.getValue().getValueObject();

        return null;
    }
    
    public Serializable getTunneledRequestKey(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        StringBuffer sb = new StringBuffer((String)request.getSender());

        sb.append(":").append(getKeyFromAttributeType(req, Attr_NASIdentifier.TYPE, true));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_NASIPAddress.TYPE, true));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_AcctSessionId.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_CallingStationId.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_CalledStationId.TYPE, false));
        sb.append(":").append(getKeyFromAttributeType(req, Attr_ServiceType.TYPE, false));
        
        return sb.toString();
    }

    protected Serializable getKeyFromAttributeType(RadiusPacket req, long type, boolean required) throws RadiusException
    {
        RadiusAttribute a = req.findAttribute(type);

        if (a == null) 
        {
            if (required)
            {
                a = AttributeFactory.newAttribute(type, null);
                throw new RadiusException("Missing required attribute: " + a.getAttributeName());
            }
            return null;
        }

        AttributeValue v = a.getValue();
        return v.toString();
    }
}
