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

package net.jradius.server.config;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.jradius.handler.EventHandler;
import net.jradius.handler.EventHandlerChain;
import net.jradius.handler.PacketHandlerChain;
import net.jradius.handler.chain.JRCommand;
import net.jradius.log.RadiusLog;

import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * The configuration context of a JRadius Listener
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public class ListenerConfigurationItem extends ConfigurationItem
{
    public static String XML_LIST_KEY 	= "listeners";
    public static String XML_KEY 		= "listener";

    private List<JRCommand> requestHandlers;
    private List<JRCommand> eventHandlers;
    private String processorClassName;
    private int numberOfThreads;
    
    private static final String PROC_CLASS_KEY		= "processor-class";
    private static final String PROC_THREADS_KEY	= "processor-threads";
    
    /**
     * Creates a Listener Configuration Context
     * @param node The node within the XML configuration where &lt;listner ...&gt; us found
     * @param config The XML configuration context
     */
    public ListenerConfigurationItem(HierarchicalConfiguration.Node node, XMLConfiguration config)
    {
        super(node, config);

        processorClassName = config.getConfigString(PROC_CLASS_KEY);
        numberOfThreads    = config.getConfigInt(PROC_THREADS_KEY, 1);
        
        // TODO: The following repetitive code could use a re-write
        // Indeed, the entire configuration section should really
        // use the Digester isntead. 
        
        HierarchicalConfiguration.Node lnode = node;
        HierarchicalConfiguration.Node lroot = config.getRoot();
        
        List children = lnode.getChildren(PacketHandlerConfigurationItem.XML_KEY);
        
        for (Iterator l = children.iterator(); l.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)l.next();
            config.setRoot(node);

            PacketHandlerConfigurationItem cfg = new PacketHandlerConfigurationItem(node, config);
            PacketHandlerConfigurationItem preCfg = null;

            // First, look to see if the named handler is a known chain
            JRCommand command = Configuration.packetHandlerForName(cfg.getHandlerName());

            if (command == null)
            {
                // If not a chain, it must either be a generic "catalog"
                // configuration or a pre-configured packet-handler in the 
                // jradius configuration file.
                if (cfg.getCatalogName() != null)
                {
                    cfg.setName(cfg.getCatalogName());
                    cfg.setClassName(PacketHandlerChain.class.getName());
                }
                else
                {
                    preCfg = Configuration.packetHandlerConfigurationForName(cfg.getHandlerName());
                    if (preCfg != null)
                    {
                        cfg.setName(preCfg.getName());
                        cfg.setHandlerName(preCfg.getHandlerName());
                        cfg.setClassName(preCfg.getClassName());
                        cfg.getSenders().addAll(preCfg.getSenders());
                        cfg.getHandleTypes().addAll(preCfg.getHandleTypes());
                        cfg.getProperties().putAll(preCfg.getProperties());
                    }
                }
                
                try
                {
                    // Instantiate the PacketHandler
                    command = (EventHandler) Configuration.getBean(cfg.getClassName());
                }
                catch (Exception e)
                {
                    RadiusLog.error(e.getMessage());
                    continue;
                }
            }
            
            if (preCfg != null) command.setConfig(preCfg);
            command.setConfig(cfg);
            
            if (requestHandlers == null)
                requestHandlers = new LinkedList();

            requestHandlers.add(command);
        }

        if (requestHandlers == null)
        {
            // No packet-handlers defined? Lets check for request-handlers
            config.setRoot(lroot);
            children = lnode.getChildren(PacketHandlerConfigurationItem.XML_KEY_ALT);
            
            for (Iterator l = children.iterator(); l.hasNext();)
            {
                node = (HierarchicalConfiguration.Node)l.next();
                config.setRoot(node);

                HandlerConfigurationItem cfg = new HandlerConfigurationItem(node, config);
                HandlerConfigurationItem preCfg = null;

                // First, look to see if the named handler is a known chain
                JRCommand command = Configuration.eventHandlerForName(cfg.getHandlerName());

                if (command == null)
                {
                    // If not a chain, it must either be a generic "catalog"
                    // configuration or a pre-configured packet-handler in the 
                    // jradius configuration file.
                    if (cfg.getCatalogName() != null)
                    {
                        cfg.setName(cfg.getCatalogName());
                        cfg.setClassName(EventHandlerChain.class.getName());
                    }
                    else
                    {
                        preCfg = Configuration.eventHandlerConfigurationForName(cfg.getHandlerName());
                        if (preCfg != null)
                        {
                            cfg.setName(preCfg.getName());
                            cfg.setHandlerName(preCfg.getHandlerName());
                            cfg.setClassName(preCfg.getClassName());
                            cfg.getSenders().addAll(preCfg.getSenders());
                            cfg.getHandleTypes().addAll(preCfg.getHandleTypes());
                            cfg.getProperties().putAll(preCfg.getProperties());
                        }
                    }
                    
                    try
                    {
                        command = (EventHandler) Configuration.getBean(cfg.getClassName());
                    }
                    catch (Exception e)
                    {
                        RadiusLog.error(e.getMessage());
                        continue;
                    }
                }
                
                if (preCfg != null) command.setConfig(preCfg);
                command.setConfig(cfg);
                
                if (requestHandlers == null)
                    requestHandlers = new LinkedList();

                requestHandlers.add(command);
            }
        }
        
        config.setRoot(lroot);
        children = lnode.getChildren(HandlerConfigurationItem.XML_KEY);
        
        for (Iterator l = children.iterator(); l.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)l.next();
            config.setRoot(node);

            HandlerConfigurationItem cfg = new HandlerConfigurationItem(node, config);
            HandlerConfigurationItem preCfg = null;

            // First, look to see if the named handler is a known chain
            JRCommand command = Configuration.eventHandlerForName(cfg.getHandlerName());

            if (command == null)
            {
                // If not a chain, it must either be a generic "catalog"
                // configuration or a pre-configured packet-handler in the 
                // jradius configuration file.
                if (cfg.getCatalogName() != null)
                {
                    cfg.setName(cfg.getCatalogName());
                    cfg.setClassName(EventHandlerChain.class.getName());
                }
                else
                {
                    preCfg = Configuration.eventHandlerConfigurationForName(cfg.getHandlerName());
                    if (preCfg != null)
                    {
                        cfg.setName(preCfg.getName());
                        cfg.setHandlerName(preCfg.getHandlerName());
                        cfg.setClassName(preCfg.getClassName());
                        cfg.getSenders().addAll(preCfg.getSenders());
                        cfg.getHandleTypes().addAll(preCfg.getHandleTypes());
                        cfg.getProperties().putAll(preCfg.getProperties());
                    }
                }
                
                try
                {
                    command = (EventHandler) Configuration.getBean(cfg.getClassName());
                }
                catch (Exception e)
                {
                    RadiusLog.error(e.getMessage());
                    continue;
                }
            }
            
            if (preCfg != null) command.setConfig(preCfg);
            command.setConfig(cfg);
            
            if (eventHandlers == null)
                eventHandlers = new LinkedList();

            eventHandlers.add(command);
        }
    }
    
    /**
     * @return Returns the active handlers for this Listener Configuration
     */
    public List<JRCommand> getRequestHandlers()
    {
        return requestHandlers;
    }
    
    /**
     * @return Returns the eventHandlers.
     */
    public List<JRCommand> getEventHandlers()
    {
        return eventHandlers;
    }
    
    /**
     * @return Returns the configured number of threads
     */
    public int getNumberOfThreads()
    {
        return numberOfThreads;
    }
    
    /**
     * @return Returns the configured class name
     */
    public String getProcessorClassName()
    {
        return processorClassName;
    }

    public String xmlKey()
    {
        return XML_KEY;
    }
}
