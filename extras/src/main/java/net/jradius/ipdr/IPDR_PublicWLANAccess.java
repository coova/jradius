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

import java.io.IOException;
import java.util.Date;

import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_AcctSessionTime;
import net.jradius.dictionary.Attr_CalledStationId;
import net.jradius.dictionary.Attr_CallingStationId;
import net.jradius.dictionary.Attr_FramedIPAddress;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_NASPort;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.exception.UnknownAttributeException;
import net.jradius.packet.RadiusPacket;
import net.jradius.session.JRadiusSession;

import org.ipdr.common.BadCompositeException;
import org.ipdr.utils.IPDRException;
import org.xml.sax.SAXException;

/**
 * IPDR PublicWLANAccess record conversion (work in progress).
 * @author David Bird
 */
public class IPDR_PublicWLANAccess extends IPDR_Base
{
    public IPDR_PublicWLANAccess() throws SAXException, IOException, IPDRException
    {
        super(defaultIPDRBaseURI + "PublicWLANAccess3.5-A.0.1.xsd");
    }
    
    protected Object[] getIPDRData(JRadiusSession radiusSession, RadiusPacket p) 
    throws BadCompositeException, UnknownAttributeException
    {
        Object[] dataObj = new Object[openTypeLength]; 

        Date startTime = radiusSession.getStartTime();
        Date stopTime = radiusSession.getStopTime();
        
        if (stopTime == null)
        {
            stopTime = new Date();
        }
        
		attributeToField(p, dataObj, Attr_UserName.TYPE, 			"userName");
		attributeToField(p, dataObj, Attr_AcctSessionId.TYPE,		"sessionID");
		attributeToField(p, dataObj, Attr_NASIdentifier.TYPE,		"NASID");
		attributeToField(p, dataObj, Attr_NASIPAddress.TYPE, 		"NASIPaddress");
		attributeToField(p, dataObj, Attr_NASPortType.TYPE, 		"NASPortType");
		attributeToField(p, dataObj, Attr_NASPort.TYPE, 			"locationPort");
		attributeToField(p, dataObj, Attr_FramedIPAddress.TYPE,		"userIPAddr");
		attributeToField(p, dataObj, Attr_CallingStationId.TYPE,	"callingStationID");
		attributeToField(p, dataObj, Attr_CalledStationId.TYPE,		"calledStationID");
		attributeToField(p, dataObj, Attr_AcctSessionTime.TYPE,		"sessionDuration");

		addData(dataObj, "locationID", 				radiusSession.getAttribute(JRadiusSession.SESSION_ATTRIBUTE_LOCATION_ID), 				null);
		addData(dataObj, "locationType", 			radiusSession.getAttribute(JRadiusSession.SESSION_ATTRIBUTE_LOCATION_TYPE), 			null);
		addData(dataObj, "locationName", 			radiusSession.getAttribute(JRadiusSession.SESSION_ATTRIBUTE_LOCATION_NAME), 			null);
		addData(dataObj, "locationCity", 			radiusSession.getAttribute(JRadiusSession.SESSION_ATTRIBUTE_LOCATION_CITY), 			null);
		addData(dataObj, "locationStateProvince",	radiusSession.getAttribute(JRadiusSession.SESSION_ATTRIBUTE_LOCATION_STATE_PROVINCE), 	null);
		addData(dataObj, "locationCountryCode", 	radiusSession.getAttribute(JRadiusSession.SESSION_ATTRIBUTE_LOCATION_COUNTRY_CODE), 	null);

		addData(dataObj, "timeZoneOffset", 	new Integer(stopTime.getTimezoneOffset()), 					null);
		addData(dataObj, "inputOctets", 	new Integer(radiusSession.getTotalOctetsIn().intValue()), 	null);
		addData(dataObj, "outputOctets",	new Integer(radiusSession.getTotalOctetsOut().intValue()), 	null);

        return dataObj;
    }
    
    protected String getServiceType()
    {
        return "PublicWLANAccessUsageEntry";
    }
    
    protected String getNameSpaceID()
    {
        return "PWA";
    }
}
