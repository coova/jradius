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

package net.jradius.standard;

import java.util.LinkedList;

import net.jradius.dictionary.Attr_AcctDelayTime;
import net.jradius.dictionary.Attr_AcctInputOctets;
import net.jradius.dictionary.Attr_AcctInputPackets;
import net.jradius.dictionary.Attr_AcctInterimInterval;
import net.jradius.dictionary.Attr_AcctOutputOctets;
import net.jradius.dictionary.Attr_AcctOutputPackets;
import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_AcctSessionTime;
import net.jradius.dictionary.Attr_AcctStatusType;
import net.jradius.dictionary.Attr_CalledStationId;
import net.jradius.dictionary.Attr_Class;
import net.jradius.dictionary.Attr_FramedIPAddress;
import net.jradius.dictionary.Attr_IdleTimeout;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_ServiceType;
import net.jradius.dictionary.Attr_SessionTimeout;
import net.jradius.dictionary.Attr_State;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.exception.StandardViolatedException;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessReject;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;

/**
 * Implementation of the WISPr requirements for RADIUS packets as defined below:
 * <pre>
 *   Attribute              Type     Au R Ac  (Au=Auth Req, R=Auth Reply, Ac=Acct Req)
 * ------------------------------------------------------------------------------------
 *   User-Name              1 String  X   X User enters full NAI
 *   User-Password          2 String  X
 *   NAS-IP-Address         4 Ipaddr  X   X IP Address of the Access Gateway
 *   Service-Type           6 Integer X     Must be set to Login (1)
 *   Framed-IP-Address      8 Ipaddr  X   X IP Address of the User
 *   Reply-Message         18 String    X   Text of reject reason if present
 *   State                 24 String  X X
 *   Class                 25 String    X X
 *   Session-Timeout       27 Integer   X   Forced logout once timeout period
 *                                          reached (seconds)
 *   Idle-Timeout          28 Integer   X   Implicit logout inactivity timeout period
 *                                          (seconds)
 *   Called-Station-ID     30 String  X   X This field should contain the MAC
 *                                          address or other information identifying
 *                                          the Access Gateway
 *   NAS-ID                32 String  X   X
 *   Acct-Status-Type      40 Integer     X 1 = Start, 2 = Stop, 3 = Interim Update
 *   Acct-Delay-Time       41 Integer     X Delay (seconds) between Acctg Event
 *                                          and when Acct-Req sent (doesn't
 *                                          include estimated network transit time)
 *   Acct-Input-Octets     42 Integer     X
 *   Acct-Output-Octets    43 Integer     X
 *   Acct-Session-ID       44 String  X X X
 *   Acct-Session-Time     46 Integer     X Call duration in seconds (already
 *                                          compensated for idle timeout)
 *   Acct-Input-Packets    47 Integer     X
 *   Acct-Output-Packets   48 Integer     X
 *   Acct-Terminate-Cause  49 Integer     X 1 = Explicit Logoff, 4 = Idle Timeout,
 *                                          5 = Session Timeout, 6 = Admin Reset,
 *                                          9 = NAS Error, 10 = NAS Request,
 *                                          11 = NAS Reboot
 *   NAS-Port-Type         61 Integer X   X 15 = Ethernet, 19 = 802.11
 *   Acct-Interim-Interval 85 Integer   X   Interval (seconds) to send accounting
 *                                          updates
 * ------------------------------------------------------------------------------------
 * </pre>
 *
 * @author David Bird
 */
public class WISPrStandard extends RadiusStandard
{
    // TODO: use the class TYPEs instead of the raw numbers... see example below
    static final long[] requiredAccessRequest  = 
    { 
            Attr_UserName.TYPE, 
            Attr_UserPassword.TYPE, 
            Attr_NASIPAddress.TYPE, 
            Attr_ServiceType.TYPE, 
            Attr_FramedIPAddress.TYPE, 
            Attr_State.TYPE, 
            Attr_CalledStationId.TYPE, 
            Attr_NASIdentifier.TYPE, 
            Attr_AcctSessionId.TYPE, 
            Attr_NASPortType.TYPE
    };
    
    static final long[] requiredAccessAccept   = { 
            Attr_ReplyMessage.TYPE, 
            Attr_State.TYPE, 
            Attr_Class.TYPE, 
            Attr_SessionTimeout.TYPE, 
            Attr_IdleTimeout.TYPE, 
            Attr_AcctSessionId.TYPE, 
            Attr_AcctInterimInterval.TYPE
    };
    
    static final long[] requiredAccounting     = { 
            Attr_UserName.TYPE, 
            Attr_NASIPAddress.TYPE, 
            Attr_FramedIPAddress.TYPE, 
            Attr_Class.TYPE, 
            Attr_CalledStationId.TYPE, 
            Attr_NASIdentifier.TYPE, 
            Attr_AcctStatusType.TYPE,
            Attr_AcctDelayTime.TYPE,
            Attr_AcctInputOctets.TYPE,
            Attr_AcctOutputOctets.TYPE,
            Attr_AcctSessionId.TYPE, 
            Attr_AcctSessionTime.TYPE,
            Attr_AcctInputPackets.TYPE,
            Attr_AcctOutputPackets.TYPE,
            Attr_NASPortType.TYPE
    };

    public String getName() { return "WISPr"; }
    
    /** 
     * @see net.jradius.standard.RadiusStandard#checkPacket(net.jradius.packet.RadiusPacket)
     */
    public void checkPacket(RadiusPacket p, long[] ignore) throws StandardViolatedException
    {
        LinkedList missing = new LinkedList();
        
        switch(p.getCode())
        {
        	case AccessRequest.CODE:
        	    checkMissing(p, missing, requiredAccessRequest, ignore);
        		break;

        	case AccessAccept.CODE:
        	    checkMissing(p, missing, requiredAccessAccept, ignore);
    			break;

        	case AccessReject.CODE:
    			break;

        	case AccountingRequest.CODE:
        	    checkMissing(p, missing, requiredAccounting, ignore);
        		break;
        }
        if (!missing.isEmpty())
            throw new StandardViolatedException(this.getClass(), missing);
    }
}
