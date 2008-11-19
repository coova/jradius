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


import net.jradius.handler.chain.JRCommand;
import net.jradius.log.RadiusLog;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.config.ConfigurationItem;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Command;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.BeansException;

import java.util.Iterator;

/**
 * The EventHandlerChain is a simple EventHandler that delegates
 * the request to a type specific command within a chain catalog, 
 * if one is found. In the case of accounting, if the 'accounting'
 * chain is not found, accounting status specific chains are tried
 * (e.g. acct-start, acct-interim, and acct-stop).
 * @author David Bird
 */
public class EventHandlerChain extends EventHandlerBase implements BeanFactoryAware
{
    private Catalog catalog;
    private String catalogName;

    private BeanFactory beanFactory;

    private String onTrue;
    private String onFalse;
    
    public void setConfig(ConfigurationItem cfg)
    {
        super.setConfig(cfg);
        setCatalogName(cfg.getName());

        Catalog c = this.getCatalog();

        {
            Iterator i = c.getNames();

            while(i.hasNext())
            {
                String name = (String)i.next();
                Command cmd = c.getCommand(name);

                if(cmd instanceof BeanFactoryAware)
                {
                    ((BeanFactoryAware)cmd).setBeanFactory(beanFactory);
                }
            }
        }

        {
            Iterator i = c.getNames();

            while(i.hasNext())
            {
                try
                {
                    String name = (String)i.next();
                    Command cmd = c.getCommand(name);

                    if(cmd instanceof InitializingBean)
                    {
                        ((InitializingBean)cmd).afterPropertiesSet();
                    }
                }
                catch(Exception e)
                {
                    RadiusLog.warn("Error during bean initialization [InitializingBean]", e);
                }
            }
        }
    }
    
    public boolean handle(JRadiusEvent event) throws Exception
    {
        return execute(event.getTypeString(), event);
    }

    protected boolean execute(String commandName, JRadiusEvent event) throws Exception
    {
        Catalog catalog = getCatalog();
        if (catalog == null) return true;
        if ("true".equalsIgnoreCase(commandName)) return true;
        if ("false".equalsIgnoreCase(commandName)) return false;
        JRCommand command = (JRCommand)catalog.getCommand(commandName);
        return execute(command, event);
    }

    protected boolean execute(JRCommand command, JRadiusEvent event) throws Exception
    {
        if (command == null) return false;
        boolean result = command.execute(event);
        String onTrue = getOnTrue();
        String onFalse = getOnFalse();
        if (result) { if (onTrue != null) return execute(onTrue, event); }
        else { if (onFalse != null) return execute(onFalse, event); }
        return result;
    }

    public void setOnFalse(String onFalse)
    {
        this.onFalse = onFalse;
    }

    public void setOnTrue(String onTrue)
    {
        this.onTrue = onTrue;
    }

    public String getOnFalse()
    {
        return onFalse;
    }

    public String getOnTrue()
    {
        return onTrue;
    }

    public String getCatalogName()
    {
        return catalogName;
    }

    public void setCatalogName(String catalogName)
    {
        this.catalogName = catalogName;
    }
    
    protected Catalog getCatalog()
    {
        if (this.catalog == null)
        {
            CatalogFactory factory = CatalogFactory.getInstance();
            this.catalog = factory.getCatalog(getCatalogName());
            if (this.catalog == null)
            {
                RadiusLog.error("Unknown catalog named: " + getCatalogName());
            }
        }
        return this.catalog;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }
}
