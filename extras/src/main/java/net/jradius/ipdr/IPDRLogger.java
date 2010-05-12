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

package net.jradius.ipdr;

import java.util.LinkedHashMap;

import net.jradius.log.RadiusLog;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.session.JRadiusSession;


/**
 * IPDR Logger (work in progress)
 * @author David Bird
 */
public class IPDRLogger
{
    private static final LinkedHashMap ipdrList = new LinkedHashMap();

    static
    {
        try
        {
            ipdrList.put("pwlan", new IPDR_PublicWLANAccess());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static String buildIPDR(String groupId, JRadiusSession radiusSession) throws Exception
    {
        RadiusPacket p = radiusSession.getLastRadiusRequest().getRequestPacket();

        if (!(p instanceof AccountingRequest)) 
        {
            RadiusLog.error("Can not build IPDR for session without accounting");
            return null;
        }
        
        if (((AccountingRequest)p).getAccountingStatusType() != AccountingRequest.ACCT_STATUS_STOP) 
        {
            RadiusLog.error("Can not build IPDR for session without STOP record");
            return null;
        }
        
        IPDR_Base ipdr = (IPDR_Base)ipdrList.get(groupId);
        if (ipdr == null) return null;
        
        return ipdr.toXML(radiusSession);
    }
}
