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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * Base class for packet filter and listener configurations. This
 * abstract class provides common methods for both configurations,
 * since they are very similar in nature.
 * 
 * @author Gert Jan Verhoog
 * @author David Bird
 * @see PacketHandlerConfigurationItem
 * @see ListenerConfigurationItem
 */
public abstract class ConfigurationItem
{
    protected XMLConfiguration hcfg;
    protected HierarchicalConfiguration.Node root;
    protected String name;
    protected String description;
    protected String className;
    protected Map    properties;

    public ConfigurationItem(String name)
    {
        this.name = name;
    }
    
    public ConfigurationItem(String name, String className)
    {
        this.name = name;
        this.className = className;
    }
    
    /**
     * Creates a new configuration item based on the options in the
     * xml configuration file. The configuration item is created from
     * the xml element in HierarchicalConfiguration config at index
     * index.
     * @param node the current configuration node being examined
     * @param config the configuration
     */
    public ConfigurationItem(HierarchicalConfiguration.Node node, XMLConfiguration config)
    {
        HierarchicalConfiguration.Node pnode = config.getRoot();
        config.setRoot(node);
        hcfg          = config;
        root          = node;
        name          = config.getConfigString("name");
        description   = config.getConfigString("description");
        className     = config.getConfigString("class");
        setProperties(config);
        config.setRoot(pnode);
    }
    
    /**
     * Parse the &lt;properties&gt; element containing zero or more
     * &lt;key&
     * @param config
     */
    protected void setProperties(XMLConfiguration config)
    {
        properties = getPropertiesFromConfig(config, root);
    }
    
    public static HashMap getPropertiesFromConfig(XMLConfiguration config, HierarchicalConfiguration.Node root)
    {
        HashMap map = new HashMap();
        
        List list = root.getChildren("property");
        HierarchicalConfiguration.Node node;
        for (Iterator l = list.iterator(); l.hasNext();)
        {
            node = (HierarchicalConfiguration.Node)l.next();
            config.setRoot(node);
            
            String name = config.getConfigString("name");
            String value = config.getConfigString("value");
            map.put(name, value);
        }
        
        return map;
    }
    
    public String getClassName()
    {
        return className;
    }
    
    public String getDescription()
    {
        return description;
    }

    public String getName()
    {
        return name;
    }

    public Map getProperties()
    {
        return properties;
    }
    
    public XMLConfiguration getXMLConfig()
    {
        return hcfg;
    }
    
    public HierarchicalConfiguration.Node getRoot()
    {
        return root;
    }
    
    /**
     * @param className The className to set.
     */
    public void setClassName(String className)
    {
        this.className = className;
    }
    
    /**
     * @param description The description to set.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @param properties The properties to set.
     */
    public void setProperties(Map properties)
    {
        this.properties = properties;
    }
    
    public String xmlKey() {return "no such key";}

    public String toString()
    {
        return name + " [" + className + "]: " + description + " -- " + properties;
    }
}
