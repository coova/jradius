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

package net.jradius.server.config;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * Configuration Item for Event Handlers.
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public class HandlerConfigurationItem extends ConfigurationItem
{
    public static final String XML_LIST_KEY 	= "event-handlers";
    public static final String XML_KEY 			= "event-handler";

    public static final String TYPE_KEY			= "type";
    public static final String SENDER_KEY		= "sender";
    public static final String HANDLER_KEY		= "handler";
    public static final String CATALOG_KEY		= "catalog";
    
    private List possibleTypes;
    private List<String> handleTypes;
    private List<String> senders;

    String handlerName;
    String catalogName;

    public HandlerConfigurationItem(String name)
    {
        super(name);
    }

    public HandlerConfigurationItem(String name, String className)
    {
        super(name, className);
    }

    public HandlerConfigurationItem(HierarchicalConfiguration.Node node, XMLConfiguration config)
    {
        super(node, config);
        possibleTypes   = config.getList(".handle.type");
        String type 	= config.getConfigString(HandlerConfigurationItem.TYPE_KEY);
        String sender 	= config.getConfigString(HandlerConfigurationItem.SENDER_KEY);
        handlerName 	= config.getConfigString(HandlerConfigurationItem.HANDLER_KEY);
        catalogName 	= config.getConfigString(HandlerConfigurationItem.CATALOG_KEY);
        setSenders(sender);
        setHandleTypes(type);
    }
    
    /**
     * @return Returns the handled types
     */
    public List<String> getHandleTypes()
    {
        return handleTypes;
    }

    /**
     * @param handleTypes The handled types to set.
     */
    public void setHandleTypes(List<String> handleTypes)
    {
        this.handleTypes = handleTypes;
    }
    
    /**
     * @param handleTypes The handled types to set.
     */
    public void setHandleTypes(String handleTypes)
    {
        LinkedList<String> list = new LinkedList<String>();
        if (handleTypes == null) handleTypes = "";
        String[] types = handleTypes.split("[ \\t]*,[ \\t]*");
        
        if (types != null) 
            for (int i=0; i < types.length; i++) 
                if (types[i].length() > 0)
                    if (possibleTypes == null || 
                            possibleTypes.isEmpty() ||
                            possibleTypes.contains(types[i]))
                        list.add(types[i]);

        this.handleTypes = list;
    }

    /**
     * @param sender The requester name(s) allowed for this handler.
     */
    public void setSenders(String sender)
    {
        LinkedList<String> list = new LinkedList<String>();
        if (sender == null) sender = "";
        String[] types = sender.split("[ \\t]*,[ \\t]*");
        
        if (types != null) 
            for (int i=0; i < types.length; i++) 
                if (types[i].length() > 0)
                    list.add(types[i]);
                
        this.senders = list;
    }
    
    /**
     * @return Returns the list of requesters serviced by this handler
     */
    public List<String> getSenders()
    {
        return senders;
    }

    /**
     * @return Returns the possible types to handle.
     */
    public List getPossibleTypes()
    {
        return possibleTypes;
    }
    
    /**
     * @param possibleTypes The possible types to set.
     */
    public void setPossibleTypes(List<String> possibleTypes)
    {
        this.possibleTypes = possibleTypes;
    }
    
    public boolean handlesType(String type)
    {
        if (handleTypes.isEmpty()) return true;
        if (handleTypes.contains(type)) return true;
        return false;
    }

    public boolean handlesSender(Object sender)
    {
        if (senders.isEmpty()) return true;
        if (senders.contains(sender)) return true;
        return false;
    }
    
    /**
     * @return Returns the catalogName.
     */
    public String getCatalogName()
    {
        return catalogName;
    }
    /**
     * @param catalogName The catalogName to set.
     */
    public void setCatalogName(String catalogName)
    {
        this.catalogName = catalogName;
    }
    /**
     * @return Returns the handlerName.
     */
    public String getHandlerName()
    {
        return handlerName;
    }
    /**
     * @param handlerName The handlerName to set.
     */
    public void setHandlerName(String handlerName)
    {
        this.handlerName = handlerName;
    }
    /**
     * @param senders The senders to set.
     */
    public void setSenders(List<String> senders)
    {
        this.senders = senders;
    }
}
