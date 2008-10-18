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

import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * Configuration Item for Packet Handlers.
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public class PacketHandlerConfigurationItem extends HandlerConfigurationItem
{
    public static final String XML_LIST_KEY 	= "packet-handlers";
    public static final String XML_KEY 			= "packet-handler";
    public static final String XML_KEY_ALT          = "request-handler";
    
    public PacketHandlerConfigurationItem(String name)
    {
        super(name);
    }

    public PacketHandlerConfigurationItem(String name, String className)
    {
        super(name, className);
    }

    public PacketHandlerConfigurationItem(HierarchicalConfiguration.Node node, XMLConfiguration config)
    {
        super(node, config);
    }
    
    public String xmlKey()
    {
        return XML_KEY;
    }
}
