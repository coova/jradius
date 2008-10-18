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

package net.jradius.ipdr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.jradius.exception.UnknownAttributeException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.session.JRadiusSession;

import org.ipdr.common.BadCompositeException;
import org.ipdr.common.Descriptor;
import org.ipdr.common.DescriptorContentHandler;
import org.ipdr.common.FNFData;
import org.ipdr.common.FNFType;
import org.ipdr.common.IPDRDocWriter;
import org.ipdr.common.NameSpaceInfo;
import org.ipdr.common.OpenType;
import org.ipdr.common.Schema;
import org.ipdr.utils.IPDRException;
import org.ipdr.utils.UUIDUtil;
import org.xml.sax.SAXException;

/**
 * The base class of IPDR conversion classes (work in progress).
 * @author David Bird
 */
public abstract class IPDR_Base
{
    private class TypeIndex
    {
        public int index;
        public OpenType openType;
        public TypeIndex(int i, OpenType t) { index = i; openType = t; }
    }

    private static final LinkedHashMap typeIndexMap = new LinkedHashMap();
    
    protected static final String defaultNameSpaceBase = "http://www.ipdr.org/namespaces/"; 
    protected static final String defaultNameSpaceURI  = defaultNameSpaceBase + "ipdr"; 
    protected static final String defaultIPDRBaseURI   = "http://www.ipdr.org/public/"; 
    
    protected OpenType[] openType;
    protected String serviceType;
    protected String schemaURI;
    protected int openTypeLength;
   
    protected IPDR_Base(String schemaURI) throws IOException, IPDRException, SAXException
    {
        this.schemaURI = schemaURI;
        Descriptor descriptor = new Descriptor();
        ArrayList schemaArray = descriptor.parseSchema(schemaURI);
        DescriptorContentHandler dch = descriptor.getContentHandler();
        
        ArrayList location = dch.getSchemaLocation();
        serviceType = descriptor.getServType();

        for (int uriPos = 0; uriPos < location.size(); uriPos++) {
            String extn = dch.getExtensionBase();
            if (!(extn.equalsIgnoreCase("ipdr:IPDRType"))) {
                schemaArray.addAll(0, descriptor.parseSchema(location.get(uriPos).toString()));
            }
        }  

        ArrayList nameSpaceInfo = dch.getURIStorage();
		String nameSpaceURI = null;
        String nameSpaceID = null;

        ArrayList localNameSpaceInfo = new ArrayList();
		
        for (int count = 0; count < nameSpaceInfo.size(); count++) {
            nameSpaceURI = ((NameSpaceInfo)nameSpaceInfo.get(count)).getNameSpaceURI();
            nameSpaceID = ((NameSpaceInfo)nameSpaceInfo.get(count)).getNameSpaceID();
			if (!(nameSpaceID.equals("")) && !(nameSpaceID.equals("ipdr"))) { 
                NameSpaceInfo NSInfo = new NameSpaceInfo(nameSpaceURI, nameSpaceID);
				localNameSpaceInfo.add(NSInfo);
            }						
		}
        
		String name = null;
		for (int i = 0; i < schemaArray.size(); i += 3) {
            name = (String)schemaArray.get(i);  

			int colonPos = name.indexOf("$");
			String fnfURI = name.substring(0, colonPos);
			String fnfNSID = new String();
			for (int j = 0; j < localNameSpaceInfo.size(); j++) {
			    if ((((NameSpaceInfo)localNameSpaceInfo.get(j)).getNameSpaceURI()).equals(fnfURI)) {
			        fnfNSID = ((NameSpaceInfo)localNameSpaceInfo.get(j)).getNameSpaceID();
			    }
			}
		    if (!(fnfNSID.equals(""))) {
			    name = name.substring((colonPos + 1), name.length());
			    name = fnfNSID + ":" + name;
				schemaArray.add(i, name);
				schemaArray.remove(i + 1);
		    } else {
			    name = name.substring((colonPos + 1), name.length());
				schemaArray.add(i, name);
				schemaArray.remove(i + 1);
		    }
		}

		try
		{
		    openType = descriptor.createOpenTypes(schemaArray);
	        openTypeLength = openType.length;
	        for (int i=0; i<openTypeLength; i++)
	        {
	            typeIndexMap.put(openType[i].getName().toLowerCase(), new TypeIndex(i, openType[i]));
	        }
		}
		catch (BadCompositeException e)
		{
		    RadiusLog.error(e.getMessage());
		}
    }
    
    abstract protected String getServiceType();
    
    abstract protected String getNameSpaceID();

    protected String getNameSpacePrefix()
    {
        String ns = getNameSpaceID();
        if (ns == null) return null;
        return ns + ":";
    }
    
    abstract protected Object[] getIPDRData(JRadiusSession radiusSession, RadiusPacket p) throws Exception;

    public String toXML(JRadiusSession radiusSession) throws Exception
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
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
		String[] serviceDefinitionURIs = { schemaURI };

		NameSpaceInfo[] otherNameSpaces;
		String ns = getNameSpaceID();
		if (ns != null)
		{
		    otherNameSpaces = new NameSpaceInfo[1];
			otherNameSpaces[0] = new NameSpaceInfo(defaultNameSpaceBase + ns, ns);
		}
		else
		{
		    otherNameSpaces = new NameSpaceInfo[0];
		}
		
		String[] schemaNameSpaces = { "http://www.ipdr.org/namespaces/ipdr" };

		Schema s = new Schema();
        s.setSchemaData(otherNameSpaces, serviceDefinitionURIs, defaultNameSpaceURI);
	
		IPDRDocWriter w = new IPDRDocWriter(out, IPDRDocWriter.XML);
		w.setSchema(s);
		
		w.writeHeader("JRadius", System.currentTimeMillis(),
                s.getDefaultNameSpaceURI(), s.getOtherNameSpaces(),
                schemaNameSpaces, s.getServiceDefinitionURIs(),
                UUIDUtil.getNewUUID());

		Object[] obj = getIPDRData(radiusSession, p);

		// Trim down null values
		int nonNull = 0;
		for (int i=0; i < obj.length; i++) if (obj[i] != null) nonNull++;
		Object finalObj[] = new Object[nonNull];
		OpenType finalType[] = new OpenType[nonNull];
		for (int i=0, c=0; i < obj.length; i++) 
		{
		    if (obj[i] != null)
		    {
		        finalObj[c] = obj[i];
		        finalType[c] = openType[i];
		        c++;
		    }
		}
		
		FNFType type = new FNFType(getNameSpacePrefix() + getServiceType(), finalType, "");
		FNFData data = new FNFData(finalObj, type);

		w.writeIPDR(data);
		
		w.writeEnd(System.currentTimeMillis());

		out.close();
		return out.toString();
    }

    protected void attributeToField (RadiusPacket p, Object[] obj, long attribute, String element) 
    throws BadCompositeException, UnknownAttributeException
    {
        RadiusAttribute attr = p.findAttribute(attribute);
        TypeIndex ti = getTypeIndex(element);
        if (ti == null) return;
        if (attr != null)
        {
            Object o = attr.getValue().getValueObject();
            String s = attr.getValue().toString();
            switch (ti.openType.getTypeCode())
            {
            	case OpenType.BYTE_TYPE_CODE:
            	    obj[ti.index] = attr.getValue().getBytes();
        			break;
            	case OpenType.STRING_TYPE_CODE:
            	    obj[ti.index] = s;
            		break;
            	case OpenType.INTEGER_TYPE_CODE:
            	    if (o instanceof Number) 
                	    obj[ti.index] = new Integer(((Number)o).intValue());
            	    else 
                	    obj[ti.index] = Integer.valueOf(s);
            		break;
            	default:
            	    throw new BadCompositeException("unsupported field type");
            }
        }
    }

    protected void addData(Object[] obj, String element, Object val, Object defVal)
    {
        TypeIndex ti = getTypeIndex(element);
        if (ti == null) return;
        if (val == null) val = defVal;
        if (val == null) return;
        obj[ti.index] = val;
    }
    
    private TypeIndex getTypeIndex(String element)
    {
        String key = getNameSpacePrefix() + element;
        TypeIndex ti = (TypeIndex)typeIndexMap.get(key.toLowerCase());
        if (ti == null)
        {
            RadiusLog.error("IPDR: Could not find element " + element + " in service definition");
            return null;
        }
        return ti;
    }
}
