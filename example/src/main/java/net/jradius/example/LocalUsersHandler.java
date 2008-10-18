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

package net.jradius.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import net.jradius.dictionary.Attr_AuthType;
import net.jradius.dictionary.Attr_CleartextPassword;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.exception.RadiusException;
import net.jradius.handler.PacketHandlerBase;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;
import net.jradius.server.config.ConfigurationItem;
import net.jradius.session.JRadiusSession;

import org.apache.commons.configuration.HierarchicalConfiguration;


/**
 * A simple Local Users implementation where users and attributes
 * are defined in the JRadius XML configuration.
 * 
 * @author David Bird
 */
public class LocalUsersHandler extends PacketHandlerBase
{
    /**
     * A "Local Users" class used by this Handler
     */
    private class LocalUser 
    {
        public String username;
        public String realm;
        public String password;
        public String attributes;
        public AttributeList attrList;
        
        /**
         * @return Returns the formatted username (with realm, if provided)
         */
        public String getUserName()
        {
            if (realm != null) return username + "@" + realm;
            return username;
        }
        
        /**
         * @return Returns the AttributeList for this user generated, if not already,
         * based on the attribute list provided in the configuration.
         */
        public AttributeList getAttributeList()
        {
            if (attrList == null)
            {
                if (attributes != null)
                {
                    BufferedReader in = new BufferedReader(new StringReader(attributes));
                    String line;
                    
                    attrList = new AttributeList();
                    
                    try
                    {
                        while ((line = in.readLine()) != null)
                        {
                            line = line.trim();
                            if (line.equals("")) continue;
                            String parts[] = line.split("[^a-zA-Z-]", 2);
                            if (parts.length == 2)
                            {
                                String attribute = parts[0];
                                line = parts[1].trim();
                                parts = line.split("[^\\+=:-]", 2);
                                if (parts.length == 2)
                                {
                                    String op = parts[0];
                                    String value = parts[1];
                                    try
                                    {
                                        RadiusAttribute attr = AttributeFactory.newAttribute(attribute, value, op);
                                        if (attr != null)
                                        {
                                            attrList.add(attr, false);
                                        }
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                
            }
            return attrList;
        }
    };
    
    /*
     * Hash map of local users
     */
    private final LinkedHashMap users = new LinkedHashMap();
    
    public void setConfig(ConfigurationItem cfg)
    {
        super.setConfig(cfg);
        HierarchicalConfiguration.Node root = cfg.getRoot();
        HierarchicalConfiguration xmlCfg = cfg.getXMLConfig();
        
        /*
         * Look for <users> ... </users> in the configuration
         */
        List usersList = root.getChildren("users");
        HierarchicalConfiguration.Node node;

        for (Iterator l = usersList.iterator(); l.hasNext();)
        {
            /*
             * Iterate the <user> ... </user> blocks
             */
            node = (HierarchicalConfiguration.Node)l.next();
            List children = node.getChildren("user");
            for (Iterator i = children.iterator(); i.hasNext();)
            {
                node = (HierarchicalConfiguration.Node)i.next();
                root = xmlCfg.getRoot();
                xmlCfg.setRoot(node);
            
                LocalUser user = new LocalUser();

                /*
                 * A user is defined in the configuration with the following XML syntax example:
                 * 
                 * <users>
                 *   <user username="test" password="test">
                 * 		Reply-Message = Hello test user!
                 *   </user>
                 * </users>
                 * 
                 * The contents of the <user>...</user> block are the attributes to use in the
                 * AccessAccept reply.
                 */
                user.username = xmlCfg.getString("[@username]");
                user.realm = xmlCfg.getString("[@realm]");
                user.password = xmlCfg.getString("[@password]");
                Object v = node.getValue();
                
                if (v != null)
                {
                    user.attributes = v.toString();
                }

                RadiusLog.debug("        -> Configured local user: " + user.getUserName());
                users.put(user.getUserName(), user);
                xmlCfg.setRoot(root);
            }
        }
    }
    
    public boolean handle(JRadiusRequest jRequest)
    {
        try
        {
            /*
             * Gather some information about the JRadius request
             */
            int type = jRequest.getType();
            AttributeList ci = jRequest.getConfigItems();
            RadiusPacket req = jRequest.getRequestPacket();
            RadiusPacket rep = jRequest.getReplyPacket();

            JRadiusSession session = jRequest.getSession();
            
            /*
             * Find the username in the request packet
             */
            String username = (String)req.getAttributeValue(Attr_UserName.TYPE);

            /*
             * See if this is a local user, otherwise we will reject (though, you may
             * want to return "ok" if you have modules configured after jradius in FreeRADIUS)
             */
    	    LocalUser u = (LocalUser)users.get(username);
    	    
    	    if (u == null)
    	    {
    	        // Unknown username, so let the RADIUS server sort it out.
    	        RadiusLog.info("Ignoring unknown username: " + username);
    	        return false;
            }

            switch (type)
            {
            	case JRadiusServer.JRADIUS_authorize:
            	{
            	    /*
            	     * We know the user, lets inform FreeRADIUS of the user's
            	     * password so that FreeRADIUS may perform the required
            	     * authentication checks.
            	     */
            		//ci.add(new Attr_AuthType(Attr_AuthType.Local)); // Auth locally
                    ci.add(new Attr_UserPassword(u.password));      // FreeRADIUS 1.0
                    ci.add(new Attr_CleartextPassword(u.password)); // FreeRADIUS 2.0
            	}
            	break;
                    
            	case JRadiusServer.JRADIUS_post_auth:
            	{
            	    if (rep instanceof AccessAccept)
            	    {
            	        /*
            	         * FreeRADIUS has returned after the authentication checks and the
            	         * user's credentials worked. Since we are now returning an AccessAccept,
            	         * we will the packet with the attributes configured for the user.
            	         */
            	        rep.addAttributes(u.getAttributeList());
            	        RadiusLog.info("Authentication successful for username: " + username);
            	    }
            	    else
            	    {
            	        RadiusLog.info("Authentication failed for username: " + username);
            	    }
            	}
            	break;
            }
        }
        catch (RadiusException e)
        {
            e.printStackTrace();
        }
        
        /*
         * Everything worked out well, from the perspective of this module.
         */
        jRequest.setReturnValue(JRadiusServer.RLM_MODULE_UPDATED);
        return false;
    }
}
