/**
 * JRadius.EWT Embedded Web Toolkit for JRadius/FreeRADIUS
 * Copyright (C) 2008 David Bird <david@coova.com>
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package net.jradius.ewt.handler;

import java.io.FileNotFoundException;
import java.io.InputStream;

import net.jradius.ewt.EWTServerSupport;
import net.jradius.handler.EventHandlerBase;

import org.springframework.beans.factory.InitializingBean;

public abstract class CoreHandler extends EventHandlerBase implements InitializingBean
{   
	protected EWTServerSupport ewtServerSupport;

    public InputStream resourceStream(String resourceName) throws FileNotFoundException
    {
        return ewtServerSupport.resourceStream(resourceName);
    }

	public void setEwtServerSupport(EWTServerSupport ewtServerSupport) 
	{
		this.ewtServerSupport = ewtServerSupport;
	}

	public void afterPropertiesSet() throws Exception
	{
		if (ewtServerSupport == null) throw new RuntimeException("ewtServerSupport is null");
	}
}
