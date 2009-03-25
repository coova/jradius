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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;

import net.jradius.handler.chain.JRCommand;
import net.jradius.handler.chain.JRConfigParser;
import net.jradius.log.RadiusLog;
import net.jradius.log.RadiusLogger;
import net.jradius.realm.JRadiusRealmManager;
import net.jradius.realm.RealmFactory;
import net.jradius.session.JRadiusSessionManager;
import net.jradius.session.SessionFactory;
import net.jradius.session.SessionKeyProvider;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Command;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Reads JRadius configuration options and provides methods to access them
 * 
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public class Configuration
{
    private static XMLConfiguration xmlCfg;
    private static HierarchicalConfiguration.Node root;

    private static boolean debug;
    private static int timeoutSeconds;
    private static File configFile;
    private static Map<String, ListenerConfigurationItem> listeners = new LinkedHashMap<String, ListenerConfigurationItem>();
    private static Map packetHandlers = new LinkedHashMap();
    private static Map eventHandlers = new LinkedHashMap();
    private static Map dictionaries = new LinkedHashMap();

    private static BeanFactory beanFactory;
    private static JRConfigParser parser = new JRConfigParser();
    private static CatalogFactory factory = CatalogFactory.getInstance();
    private static LogConfigurationItem logConfig;
    
    public static void initialize(File file) throws FileNotFoundException, ConfigurationException
    {        
        configFile = file;
        initialize(new FileInputStream(file), null);
    }
 
    public static void initialize(InputStream input, BeanFactory factory) throws FileNotFoundException, ConfigurationException
    {
        //let new initalization configure it's own logger
        logConfig = null;

        beanFactory = factory;
        xmlCfg = new XMLConfiguration(new InputStreamReader(input));
        root = xmlCfg.getRoot();
        
        RadiusLog.info("Configuring JRadius Server....");

        setLogConfig();
        setGeneralOptions();
        setRealmManagerConfig();
        setSessionManagerConfig();
        setDictionaryConfigs();
        setPacketHandlersConfigs();   
        setEventHandlersConfigs();   
        setListenerConfigs();
    }
 
    /**
     * Corresponds to the &lt;debug&gt;true/false&lt;/debug&gt; configuration
     * option. If set to true, generate log messages for debugging.
     * @return true if debugging messages should be generated.
     */
    public static boolean isDebug()
    {
        return debug;
    }
    
    /**
     * @return configuration file directory
     */
    public static String getConfigFileDir()
    {
        if (configFile == null) return ".";
        String configFileDir = configFile.getParent();
        if (configFileDir == null) return ".";
        return configFileDir;
    }

    /**
     * A collection of PacketHandlerConfigurationItems, corresponding
     * to the &lt;packet-handler&gt; elements in the configuration file.
     * @return A collection of PacketHandlerConfigurationItems
     */
    public static Collection getPacketHandlers()
    {
        return packetHandlers.values();
    }

    /**
     * A collection of HandlerConfigurationItems, corresponding
     * to the &lt;event-handler&gt; elements in the configuration file.
     * @return A collection of HandlerConfigurationItems
     */
    public static Collection getEventHandlers()
    {
        return eventHandlers.values();
    }

    public static PacketHandlerConfigurationItem packetHandlerConfigurationForName(String name)
    {
        return (PacketHandlerConfigurationItem) packetHandlers.get(name);
    }
    
    public static HandlerConfigurationItem eventHandlerConfigurationForName(String name)
    {
        return (HandlerConfigurationItem) eventHandlers.get(name);
    }
    
    public static JRCommand packetHandlerForName(String name)
    {
        // XXX: our getCommand() will be replaced with factory.getCommand()
        return (JRCommand)getCommand(name);
    }
    
    public static JRCommand eventHandlerForName(String name)
    {
        // XXX: our getCommand() will be replaced with factory.getCommand()
        return (JRCommand)getCommand(name);
    }
    
    public static Command getCommand(String commandID) throws IllegalArgumentException 
    {
        // XXX: This function taken from CVS version of CatalogFactory
        String DELIMITER = ":";
        String commandName = commandID;
        String catalogName = null;
        Catalog catalog = null;

        if (commandID != null) 
        {
            int splitPos = commandID.indexOf(DELIMITER);
            if (splitPos != -1) 
            {
                catalogName = commandID.substring(0, splitPos);
                commandName = commandID.substring(splitPos + DELIMITER.length());
                if (commandName.indexOf(DELIMITER) != -1) 
                {
                    throw new IllegalArgumentException("commandID [" + commandID + "] has too many delimiters (reserved for future use)");
                }
            }
        }

        if (catalogName != null) 
        {
            catalog = factory.getCatalog(catalogName);
            if (catalog == null) 
            {
                RadiusLog.warn("No catalog found for name: " + catalogName + ".");
                return null;
            }
        } 
        else 
        {
            catalog = factory.getCatalog();
            if (catalog == null) 
            {
                RadiusLog.warn("No default catalog found.");
                return null;
            }
        }

        return catalog.getCommand(commandName);                    
    }

    /**
     * A collection of ListenerConfigurationItems, corresponding
     * to the &lt;listener&gt; elements in the configuration file.
     * @return A collection of ListenerConfigurationItems
     */
    public static Collection<ListenerConfigurationItem> getListenerConfigs()
    {
        return listeners.values();
    }
    
    public static ListenerConfigurationItem listenerConfigurationForName(String name)
    {
        return (ListenerConfigurationItem) listeners.get(name);
    }

    /**
     * A collection of DictionaryConfigurationItems, corresponding
     * to the &lt;load-dictionaries&gt; elements in the configuration file.
     * @return A collection of DictionaryConfigurationItems
     */
    public static Collection getDictionaryConfigs()
    {
        return dictionaries.values();
    }
    
    public static DictionaryConfigurationItem dictionaryConfigurationForName(String name)
    {
        return (DictionaryConfigurationItem) dictionaries.get(name);
    }

    /**
     * The number of seconds to wait for packets, corresponding to
     * the &lt;timeout&gt; option in the configuration file. If
     * this is 0 (zero), wait indefinately (i.e. waiting will
     * never time out).
     * @return The number of seconds to wait for packets
     */
    public static int getTimeoutSeconds()
    {
        return timeoutSeconds;
    }
    
    private static void setGeneralOptions()
    {
        debug = xmlCfg.getConfigBoolean("debug");
        timeoutSeconds = xmlCfg.getConfigInt("timeout");

        List children = root.getChildren("chain-catalog");
        
        HierarchicalConfiguration.Node node;
        for (Iterator i = children.iterator(); i.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)i.next();
            xmlCfg.setRoot(node);
            
            String catalogURL = xmlCfg.getConfigString("name");
            
            if (catalogURL != null)
            {
                try
                {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    RadiusLog.debug("Loading Chains URL: " + catalogURL);

                    URL url = classLoader.getResource(catalogURL);

                    if (url == null)
                    {
                        RadiusLog.error("File " + catalogURL + " not found.");
                    }
                    else
                    {
                        boolean failure;

                        try
                        {
                            url.openStream().close();
                            failure = false;
                        }
                        catch(Exception e)
                        {
                            failure = true;
                        }

                        if(failure)
                        {
                            RadiusLog.error("file " + url + " not found.");
                        }
                        else
                        {
                            parser.parse(url);
                        }
                    }
                }
                catch (Exception e)
                {
                	RadiusLog.error("Error loading catalog chain.", e);
                }
            }
            
            xmlCfg.setRoot(root);
        }
    }

    private static void setDictionaryConfigs()
    {
        List children = root.getChildren(DictionaryConfigurationItem.XML_KEY);
        HierarchicalConfiguration.Node node;
        for (Iterator i = children.iterator(); i.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)i.next();
            xmlCfg.setRoot(node);
            DictionaryConfigurationItem item = new DictionaryConfigurationItem(node, xmlCfg);
            dictionaries.put(item.getName(), item);
            xmlCfg.setRoot(root);
        }
    }

    private static void setLogConfig()
    {
        List children = root.getChildren(LogConfigurationItem.XML_KEY);
        HierarchicalConfiguration.Node node;
        for (Iterator i = children.iterator(); i.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)i.next();
            xmlCfg.setRoot(node);
            if (logConfig != null)
            {
                RadiusLog.warn("A RadiusLogger is already configured, skipping configuration");
                return;
            }
            
            logConfig = new LogConfigurationItem(node, xmlCfg);
            
            // Setup the new logger now so that the rest of the configuration
            // takes use of the new logger.
            try
            {
                RadiusLogger logger = (RadiusLogger)Configuration.getBean(logConfig.getClassName());
                RadiusLog.setRadiusLogger(logger);
                RadiusLog.debug("Configuring RadiusLogger " + logConfig.getName() + ": " + logger.getClass().getName());
            }
            catch (Exception e)
            {
                RadiusLog.error(e.getMessage(), e);
                logConfig = null;
            }

            xmlCfg.setRoot(root);
        }
    }

    private static void setPacketHandlersConfigs()
    {
        List list = root.getChildren(PacketHandlerConfigurationItem.XML_LIST_KEY);
        HierarchicalConfiguration.Node node;
        for (Iterator l = list.iterator(); l.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)l.next();
            List children = node.getChildren(PacketHandlerConfigurationItem.XML_KEY);
            for (Iterator i = children.iterator(); i.hasNext();)
            {
                node = (HierarchicalConfiguration.Node)i.next();
                xmlCfg.setRoot(node);
                PacketHandlerConfigurationItem item = new PacketHandlerConfigurationItem(node, xmlCfg);
                packetHandlers.put(item.getName(),item);
                xmlCfg.setRoot(root);
            }
        }
    }

    private static void setEventHandlersConfigs()
    {
        List list = root.getChildren(HandlerConfigurationItem.XML_LIST_KEY);
        HierarchicalConfiguration.Node node;
        for (Iterator l = list.iterator(); l.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)l.next();
            List children = node.getChildren(HandlerConfigurationItem.XML_KEY);
            for (Iterator i = children.iterator(); i.hasNext();)
            {
                node = (HierarchicalConfiguration.Node)i.next();
                xmlCfg.setRoot(node);
                HandlerConfigurationItem item = new HandlerConfigurationItem(node, xmlCfg);
                eventHandlers.put(item.getName(),item);
                xmlCfg.setRoot(root);
            }
        }
    }

    private static void setListenerConfigs()
    {
        List list = root.getChildren(ListenerConfigurationItem.XML_LIST_KEY);
        HierarchicalConfiguration.Node node;
        for (Iterator l = list.iterator(); l.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)l.next();
            List children = node.getChildren(ListenerConfigurationItem.XML_KEY);
            for (Iterator i = children.iterator(); i.hasNext();)
            {
                node = (HierarchicalConfiguration.Node)i.next();
                xmlCfg.setRoot(node);
                ListenerConfigurationItem item = new ListenerConfigurationItem(node, xmlCfg);
                listeners.put(item.getName(),item);
                xmlCfg.setRoot(root);
            }
        }
    }
    
    private static final String SESSION_MANAGER_KEY 	= "session-manager";
    private static final String REALM_MANAGER_KEY 		= "realm-manager";
    private static final String REQUESTER_KEY 			= "requester";
    private static final String KEY_PROVIDER_KEY 		= "key-provider";
    private static final String SESSION_FACTORY_KEY 	= "session-factory";
    private static final String REALM_FACTORY_KEY 		= "realm-factory";
    
    private static void setSessionManagerConfig()
    {
        List list = root.getChildren(SESSION_MANAGER_KEY);
        
        RadiusLog.info("Initializing session manager");

        HierarchicalConfiguration.Node node;
        for (Iterator l = list.iterator(); l.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)l.next();
            xmlCfg.setRoot(node);
            
            String clazz = xmlCfg.getConfigString("class");
            String requester = xmlCfg.getConfigString(REQUESTER_KEY);
            String keyProvider = xmlCfg.getConfigString(KEY_PROVIDER_KEY);
            String sessionFactory = xmlCfg.getConfigString(SESSION_FACTORY_KEY);
            
            if (clazz != null)
            {
                try
                {
                    RadiusLog.debug("Session Manager (" + requester + "): " + clazz);
                    JRadiusSessionManager manager = (JRadiusSessionManager) getBean(clazz);
                    JRadiusSessionManager.setManager(requester, manager);
                }
                catch (Exception e)
                {
                    RadiusLog.error(e.getMessage(), e);
                }
            }
            
            if (keyProvider != null)
            {
                try
                {
                    RadiusLog.debug("Session Key Provider (" + requester + "): " + keyProvider);
                    SessionKeyProvider provider = (SessionKeyProvider) getBean(keyProvider);
                    JRadiusSessionManager.getManager(requester).setSessionKeyProvider(requester, provider);
                }
                catch (Exception e)
                {
                    RadiusLog.error(e.getMessage(), e);
                }
            }
            
            if (sessionFactory != null)
            {
                try
                {
                    RadiusLog.debug("Session Factory (" + requester + "): " + sessionFactory);
                    SessionFactory factory = (SessionFactory) getBean(sessionFactory);
                    factory.setConfig(xmlCfg, node);
                    JRadiusSessionManager.getManager(requester).setSessionFactory(requester, factory);
                }
                catch (Exception e)
                {
                    RadiusLog.error(e.getMessage(), e);
                }
            }
        }
    }

    public static Object getBean(String name) throws IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        Object o = null;
        if (name.startsWith("bean:")) 
        {
            String s[] = name.split(":");
            o = beanFactory.getBean(s[1]);    
        }
        else
        {
            Class clazz = Class.forName(name);
            o = clazz.newInstance();
            if (o instanceof BeanFactoryAware)
            {
                try
                {
                    ((BeanFactoryAware)o).setBeanFactory(Configuration.beanFactory);
                }
                catch(Exception e)
                {
                    RadiusLog.warn("Error during bean initialization [BeanFactoryAware]", e);
                }
            }
            if (o instanceof InitializingBean)
            {
                try
                {
                    ((InitializingBean)o).afterPropertiesSet();
                }
                catch (Exception e)
                {
                    RadiusLog.warn("Error during bean initialization [InitializingBean]", e);
                }
            }
        }
        return o;
    }
    
    private static void setRealmManagerConfig()
    {
        List list = root.getChildren(REALM_MANAGER_KEY);
        
        RadiusLog.info("Initializing realm manager");

        HierarchicalConfiguration.Node node;
        for (Iterator l = list.iterator(); l.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)l.next();
            xmlCfg.setRoot(node);
            
            String requester = xmlCfg.getConfigString(REQUESTER_KEY);
            String realmFactory = xmlCfg.getConfigString(REALM_FACTORY_KEY);
            
            if (realmFactory != null)
            {
                try
                {
                    RadiusLog.debug("Realm Factory (" + requester + "): " + realmFactory);
                    RealmFactory factory = (RealmFactory) getBean(realmFactory);
                    factory.setConfig(xmlCfg, node);
                    JRadiusRealmManager.getManager().setRealmFactory(requester, factory);
                }
                catch (Exception e)
                {
                    RadiusLog.error(e.getMessage(), e);
                }
            }
        }
    }
}
