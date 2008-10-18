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

package net.jradius.handler.tlstunnel;

import java.util.HashMap;
import java.util.StringTokenizer;

import net.jradius.dictionary.Attr_Realm;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.vsa_freeradius.Attr_FreeRADIUSProxiedTo;
import net.jradius.exception.RadiusException;
import net.jradius.handler.RadiusSessionHandler;
import net.jradius.packet.RadiusPacket;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;
import net.jradius.session.JRadiusSession;
import net.jradius.session.JRadiusSessionManager;
import net.jradius.session.RadiusSessionKeyProvider;
import net.jradius.session.RadiusSessionSupport;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * Tunnel Session Initialization Handler.
 * @author David Bird
 */
public class InitTunnelSessionHandler extends RadiusSessionHandler
{
    private Cache tlsTunnels;
    private HashMap realms = new HashMap();
    
    /**
     * This handler is to be chained before the actual InitSessionHandler. 
     * In the event the request is the inner request of a TLS tunnel, the associated
     * session if found and configured. 
     * @see net.jradius.handler.PacketHandler#handle(net.jradius.server.JRadiusRequest)
     */
    public boolean handle(JRadiusRequest request) throws RadiusException
    {
        int type = request.getType();
        RadiusPacket req = request.getRequestPacket();

        String fullUserName  	= (String) req.getAttributeValue(Attr_UserName.TYPE);
        String stripUserName 	= null;
        String realm     		= null;

        JRadiusSession session = request.getSession();

        if (fullUserName == null) return false;
        
        stripUserName = fullUserName;
        
        String[] s = RadiusSessionSupport.splitUserName(stripUserName);
        
        if (s != null && s.length == 2)
        {
            stripUserName = s[0];
            realm = s[1];
        }

        if (type == JRadiusServer.JRADIUS_authorize &&
	        req.findAttribute(Attr_FreeRADIUSProxiedTo.TYPE) != null)
	    {
            // If we are proxy-ing the request to ourselves, 
	        // this is an inner-tunnel authentication.
	        RadiusSessionKeyProvider skp = (RadiusSessionKeyProvider)JRadiusSessionManager.getManager(request.getSender()).getSessionKeyProvider(request.getSender());
	        Element element = tlsTunnels.get(skp.getTunneledRequestKey(request));
            if (element == null) return false;
            String sessionKey = (String)element.getValue();
	        if (sessionKey == null) 
	        {
	            request.setReturnValue(JRadiusServer.RLM_MODULE_REJECT);
	            return true;
	        }

            session = JRadiusSessionManager.getManager(request.getSender()).getSession(request, sessionKey);
            if (session == null) throw new RadiusException("Could not find on-going tunneled session: " + sessionKey);
            
            session.setSecured(true);
            session.setUsername(stripUserName);
            session.setRealm(realm);

            request.setSession(session);

            String r = (String)req.getAttributeValue(Attr_Realm.TYPE);
	        if (r != null)
	        {
	            if ("DEFAULT".equals(r))
	            {
	                r = realm;
	            }
	            if (!isLocalRealm(r)) 
	            {
	                session.setProxyToRealm(r);
	            }
	        }
 	    }

        return false;
    }
    
    public void setLocalRealms(String localRealms)
    {
        StringTokenizer st = new StringTokenizer(localRealms, ",");
        while (st.hasMoreTokens()) 
        {
            String realm = st.nextToken();
            realms.put(realm.trim().toLowerCase(), "local");
        }
    }

    public void setSecureRealms(String localRealms)
    {
        StringTokenizer st = new StringTokenizer(localRealms, ",");
        while (st.hasMoreTokens()) 
        {
            String realm = st.nextToken();
            realms.put(realm.trim().toLowerCase(), "secure");
        }
    }

    public boolean isLocalRealm(String realm)
    {
        String s = (String)realms.get(realm.trim().toLowerCase());
        if (s == null) return false;
        return "local".equals(s);
    }

    public boolean isSecureRealm(String realm)
    {
        String s = (String)realms.get(realm.trim().toLowerCase());
        if (s == null) return false;
        return "secure".equals(s);
    }

    public void setTlsTunnels(Cache tlsTunnels)
    {
        this.tlsTunnels = tlsTunnels;
    }
}
