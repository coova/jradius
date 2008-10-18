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

package net.jradius.session;

import java.util.Map;

import net.jradius.exception.RadiusException;
import net.jradius.log.JRadiusLogEntry;
import net.jradius.log.RadiusLogEntry;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.config.ConfigurationItem;
import net.jradius.server.config.XMLConfiguration;
import net.jradius.util.RadiusRandom;

import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * The Default SessionFactory.
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public class RadiusSessionFactory implements SessionFactory
{
    private Map configMap = null;
    
    public JRadiusSession getSession(JRadiusRequest request, Object key) throws RadiusException
    {
        return null;
    }

    public JRadiusSession newSession(JRadiusRequest request) throws RadiusException
    {
        return new RadiusSession(createNewSessionID());
    }

    public JRadiusLogEntry newSessionLogEntry(JRadiusEvent event, JRadiusSession session, String packetId)
    {
        return new RadiusLogEntry(session, packetId);
    }

    protected String createNewSessionID()
    {
        return RadiusRandom.getRandomString(16);
    }

    public String getConfigValue(String name)
    {
        if (configMap == null) return null;
        return (String)configMap.get(name);
    }

    public void setConfig(XMLConfiguration config, HierarchicalConfiguration.Node root)
    {
        this.configMap = ConfigurationItem.getPropertiesFromConfig(config, root);
    }
}
