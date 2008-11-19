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


import net.jradius.log.RadiusLog;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.config.ConfigurationItem;
import net.jradius.server.config.HandlerConfigurationItem;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.Command;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.BeansException;

/**
 * The JRadius ChainBase for Jakarta Commons Chain
 * @author David Bird
 */
public class JRChainBase extends ChainBase implements JRCommand, BeanFactoryAware, InitializingBean
{
    private BeanFactory beanFactory;
    private String name;
  
    protected HandlerConfigurationItem config;

    public JRChainBase()
    {
    }
    
    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setConfig(ConfigurationItem cfg)
    {
        config = (HandlerConfigurationItem)cfg;
    }
    
    public boolean doesHandle(JRadiusEvent event)
    {
        if (config == null) return true;
        return (config.handlesSender(event.getSender()) && 
                config.handlesType(event.getTypeString()));
    }
    
    public boolean execute(Context context) throws Exception
    {
        RadiusLog.debug("Executing command: " + getName());
        return super.execute(context);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    public void afterPropertiesSet() throws Exception
    {
        for(Command c : this.commands)
        {
            if(c instanceof BeanFactoryAware)
            {
                ((BeanFactoryAware)c).setBeanFactory(beanFactory);
            }
        }

        for(Command c : this.commands)
        {
            if(c instanceof InitializingBean)
            {
                ((InitializingBean)c).afterPropertiesSet();
            }
        }
    }
}
