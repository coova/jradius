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

package net.jradius.handler.chain;


import net.jradius.server.JRadiusEvent;
import net.jradius.server.config.ConfigurationItem;

import org.apache.commons.chain.Command;

/**
 * The JRadius Command Interface for Jakarta Commons Chain. This 
 * class is the foundation of all handlers within JRadius - which 
 * can be single command, or chains of commands.
 * @author David Bird
 */
public interface JRCommand extends Command
{
    /**
     * Set the ConfigurationItem of this handler. All JRadius handlers
     * have an associated HandlerConfigurationItem associated with it.
     * @param cfg The HandlerConfigurationItem to be set
     */
    public void setConfig(ConfigurationItem cfg);

    /**
     * Tests whether or not this handler handles the given JRadiusEvent.
     * @param event The JRadiusEvent (or JRadiusRequest) to be checked
     * @return Returns true if this handler should handle the given event
     */
    public boolean doesHandle(JRadiusEvent event);

    /**
     * @return Returns the name of the handler (as defined in the configuration)
     */
    public String getName();
}
