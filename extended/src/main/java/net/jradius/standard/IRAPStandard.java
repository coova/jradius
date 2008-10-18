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
import net.jradius.dictionary.Attr_AcctOutputOctets;
import net.jradius.dictionary.Attr_AcctOutputPackets;
import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_AcctSessionTime;
import net.jradius.dictionary.Attr_AcctStatusType;
import net.jradius.dictionary.Attr_AcctTerminateCause;
import net.jradius.dictionary.Attr_CalledStationId;
import net.jradius.dictionary.Attr_CallingStationId;
import net.jradius.dictionary.Attr_EAPMessage;
import net.jradius.dictionary.Attr_FramedIPAddress;
import net.jradius.dictionary.Attr_MessageAuthenticator;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_NASPort;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_TerminationAction;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.dictionary.vsa_microsoft.Attr_MSMPPERecvKey;
import net.jradius.dictionary.vsa_microsoft.Attr_MSMPPESendKey;
import net.jradius.exception.StandardViolatedException;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessChallenge;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;

/**
 * Implementation of the IRAP Interface 2 & 3 requirements for RADIUS.
 * See http://www.goirap.org/ for more details and documenation.
 *
 * @author David Bird
 */
public class IRAPStandard extends RadiusStandard
{
    static final long[] requiredAccessRequest 	= { 
            Attr_UserName.TYPE,
            Attr_NASIPAddress.TYPE,
            Attr_NASPort.TYPE,
            Attr_NASPortType.TYPE,
            Attr_NASIdentifier.TYPE,
            Attr_CalledStationId.TYPE,
            Attr_CallingStationId.TYPE,
    };
    
    static final long[] requiredUAMAccessRequest 	= { 
            Attr_UserPassword.TYPE,
    };

    static final long[] requiredEAPAccessRequest 	= { 
            Attr_EAPMessage.TYPE,
            Attr_MessageAuthenticator.TYPE,
    };

    static final long[] requiredEAPAccessChallenge 	= { 
            Attr_EAPMessage.TYPE,
            Attr_MessageAuthenticator.TYPE,
    };

    static final long[] requiredEAPAccessReject 	= { 
            Attr_EAPMessage.TYPE,
            Attr_MessageAuthenticator.TYPE,
    };

    static final long[] requiredAccessAccept 	= { 
            Attr_UserName.TYPE,
    };

    static final long[] requiredEAPAccessAccept 	= { 
            Attr_EAPMessage.TYPE,
            Attr_MessageAuthenticator.TYPE,
            Attr_TerminationAction.TYPE,
            Attr_MSMPPERecvKey.TYPE,
            Attr_MSMPPESendKey.TYPE,
    };
    
    static final long[] requiredAccountingRequest	= { 
            Attr_UserName.TYPE,
            Attr_NASIPAddress.TYPE,
            Attr_NASPort.TYPE,
            Attr_NASPortType.TYPE,
            Attr_NASIdentifier.TYPE,
            Attr_AcctStatusType.TYPE,
            Attr_AcctDelayTime.TYPE,
            Attr_AcctSessionId.TYPE,
            Attr_FramedIPAddress.TYPE,
            Attr_CalledStationId.TYPE,
            Attr_CallingStationId.TYPE,
    };

    static final long[] requiredAccountingInterimRequest	= { 
            Attr_AcctInputOctets.TYPE,
            Attr_AcctOutputOctets.TYPE,
            Attr_AcctInputPackets.TYPE,
            Attr_AcctOutputPackets.TYPE,
            Attr_AcctSessionTime.TYPE,
    };

    static final long[] requiredAccountingStopRequest	= { 
            Attr_AcctTerminateCause.TYPE,
    };

    private boolean IEEE8021XRequired = false;
    
    public String getName() { return "IRAP"; }
    
    /** 
     * @see net.jradius.standard.RadiusStandard#checkPacket(net.jradius.packet.RadiusPacket)
     */
    public void checkPacket(RadiusPacket p, long[] ignore) throws StandardViolatedException
    {
        LinkedList missing = new LinkedList();
        boolean testAs8021X = false;
        
        if (isIEEE8021XRequired()) testAs8021X = true;
        else testAs8021X = p.findAttribute(Attr_EAPMessage.TYPE) != null;
        
        switch(p.getCode())
        {
        		case AccessRequest.CODE:
        		    checkMissing(p, missing, requiredAccessRequest, ignore);
        		    checkMissing(p, missing, testAs8021X ? requiredEAPAccessRequest : requiredUAMAccessRequest, ignore);
        			break;

        		case AccessChallenge.CODE:
        		    if (testAs8021X) checkMissing(p, missing, requiredEAPAccessChallenge, ignore);
        			break;

        		case AccessAccept.CODE:
        		    checkMissing(p, missing, requiredAccessAccept, ignore);
        			if (testAs8021X) checkMissing(p, missing, requiredEAPAccessAccept, ignore);
    				break;

        		case AccountingRequest.CODE:
        		{
        		    checkMissing(p, missing, requiredAccountingRequest, ignore);

        		    switch(((AccountingRequest)p).getAccountingStatusType())
        		    {
        		    	case AccountingRequest.ACCT_STATUS_START:
        		    	    // no additional requirements
        		    		break;
        		    	case AccountingRequest.ACCT_STATUS_STOP:
        		    	    checkMissing(p, missing, requiredAccountingStopRequest, ignore);
        		    		// fall through
        		    	case AccountingRequest.ACCT_STATUS_INTERIM:
        		    	    checkMissing(p, missing, requiredAccountingInterimRequest, ignore);
        		    		break;
        		    }
        		}
        		break;
        }
        
        if (!missing.isEmpty())
            throw new StandardViolatedException(this.getClass(), missing);
    }
    
    /**
     * @return Returns the iEEE8021XRequired.
     */
    public boolean isIEEE8021XRequired() 
    {
        return IEEE8021XRequired;
    }
    /**
     * @param required The iEEE8021XRequired to set.
     */
    public void setIEEE8021XRequired(boolean required) 
    {
        IEEE8021XRequired = required;
    }
}
