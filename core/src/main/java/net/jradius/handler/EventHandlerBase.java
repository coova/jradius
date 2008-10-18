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

package net.jradius.handler;


import net.jradius.log.RadiusLog;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.config.ConfigurationItem;
import net.jradius.server.config.HandlerConfigurationItem;

import org.apache.commons.chain.Context;

/**
 * The base abstract class of all Event Handlers (the base of all handles)
 * 
 * @author David Bird
 * @author Gert Jan Verhoog
 */
public abstract class EventHandlerBase implements EventHandler 
{
    private String name;
    
    protected HandlerConfigurationItem config;

    public EventHandlerBase()
    {
        config = null;
    }
     
    public void setConfig(ConfigurationItem cfg)
    {
        name = cfg.getName();
        config = (HandlerConfigurationItem)cfg;
    }
    
    public boolean doesHandle(JRadiusEvent event)
    {
        return (config.handlesSender(event.getSender()) && 
                config.handlesType(event.getTypeString()));
    }

    public abstract boolean handle(JRadiusEvent event) throws Exception;

    public boolean execute(Context context) throws Exception
    {
        JRadiusEvent event = (JRadiusEvent)context;
        RadiusLog.debug("Executing command: " + getName());
        return handle(event);
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}

