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

import net.jradius.dictionary.Attr_EAPType;
import net.jradius.dictionary.Attr_ProxyToRealm;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.handler.RadiusSessionHandler;
import net.jradius.handler.chain.JRCommand;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.value.NamedValue;
import net.jradius.server.JRadiusRequest;
import net.jradius.session.JRadiusSession;
import net.jradius.session.JRadiusSessionManager;
import net.jradius.session.RadiusSessionKeyProvider;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.chain.Catalog;

/**
 * TLS Tunnel Termination Authorization Handler
 * @author David Bird
 */
public class AuthorizeHandler extends RadiusSessionHandler
{
    private HashMap terminatedRealms = new HashMap();
    private Cache tlsTunnels;
    private String anonUserName;
    private String chainName;
    
    public boolean handle(JRadiusRequest request) throws Exception
    {
        RadiusPacket req = request.getRequestPacket();
        AttributeList ci = request.getConfigItems();
        
        JRadiusSession session = request.getSession();

        String username = (String) req.getAttributeValue(Attr_UserName.TYPE);
        String realm = session.getRealm();

        Attr_EAPType eap = (Attr_EAPType) req.findAttribute(Attr_EAPType.TYPE);

        if (eap != null)
        {
            NamedValue eapv = (NamedValue)eap.getValue();
            String eapType = eapv.getValueString();
            
            if (session.isSecured())
            {
                Catalog catalog = getCatalog();
                if (catalog != null && chainName != null)
                {
                    JRCommand c = (JRCommand)catalog.getCommand(chainName);
                    if (c == null)
                    {
                        RadiusLog.error("There is no command '" + chainName + "' in catalog " + getCatalogName());
                        return false;
                    }
                    return execute(c, request);
                }
            }
            else if ((Attr_EAPType.Identity.equals(eapType) ||
                 Attr_EAPType.NAK.equals(eapType) ||
                 Attr_EAPType.EAPTTLS.equals(eapType) ||
                 Attr_EAPType.PEAP.equals(eapType)) &&
                    ((anonUserName != null && anonUserName.equals(username)) ||
                            terminatedRealms.containsKey(realm)))
            {
                // Here we are returning NOOP so that TTLS or PEAP tunnels
                // can terminate at this radius server and we can proxy the tunneled credentials.
                RadiusSessionKeyProvider skp = (RadiusSessionKeyProvider)JRadiusSessionManager.getManager(request.getSender()).getSessionKeyProvider(request.getSender());

                // Rewrite the log type (not an authorization, but a tunnel
                // termination)
                session.getLogEntry(request).setType("tls-tunnel");

                // Force the local handling of the tunnel (do not proxy)
                ci.remove(Attr_ProxyToRealm.TYPE);
                
                // Record the session as a tls tunnel
                tlsTunnels.put(new Element(skp.getTunneledRequestKey(request), session.getSessionKey()));

                RadiusLog.info("EAP-TTLS Termination: username = " + username + ", session = " + session.getSessionKey());

                return true;
            }
        }
    
        return false;
    }
    
    /**
     * @return Returns the anonUserName.
     */
    public String getAnonUserName()
    {
        return anonUserName;
    }
    
    /**
     * @param anonUserName The anonUserName to set.
     */
    public void setAnonUserName(String anonUserName)
    {
        this.anonUserName = anonUserName;
    }

    public void setTerminatedRealms(String realms)
    {
        StringTokenizer st = new StringTokenizer(realms, ",");
        while (st.hasMoreTokens()) 
        {
            String realm = st.nextToken();
            terminatedRealms.put(realm.trim().toLowerCase(), realm);
        }
    }

    public String getChainName()
    {
        return chainName;
    }

    public void setChainName(String chainName)
    {
        this.chainName = chainName;
    }

    public void setTlsTunnels(Cache tlsTunnels)
    {
        this.tlsTunnels = tlsTunnels;
    }
}
