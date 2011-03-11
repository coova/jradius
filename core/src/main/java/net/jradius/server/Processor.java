/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
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

package net.jradius.server;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.jradius.handler.chain.JRCommand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Base abstract class of all Processors
 * 
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public abstract class Processor extends JRadiusThread implements ApplicationContextAware
{
	protected Log log = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;

    private EventDispatcher eventDispatcher;
    
    private List<JRCommand> requestHandlers;
    
    private BlockingQueue<ListenerRequest> queue;
    
    private boolean active = true;

    public Processor()
    {
        super();
    }
    
    /**
     * Sets the request queue for this listener
     * 
     * @param q the RequestQueue;
     */
    public void setRequestQueue(BlockingQueue<ListenerRequest> q)
    {
        queue = q;
    }

    public BlockingQueue<ListenerRequest> getRequestQueue()
    {
        return queue;
    }

    public EventDispatcher getEventDispatcher()
    {
        return eventDispatcher;
    }
    
    public void setEventDispatcher(EventDispatcher eventDispatcher)
    {
        this.eventDispatcher = eventDispatcher;
    }
    
    public void setRequestHandlers(List<JRCommand> handlers)
    {
        requestHandlers = handlers;
    }
    
    public List<JRCommand> getRequestHandlers()
    {
        return requestHandlers;
    }

    protected abstract void processRequest(ListenerRequest listenerRequest) throws Exception;

    public void run()
    {
        while (getActive())
        {
            try
            {
                Thread.yield();
                process();
            }
            catch (InterruptedException e)
            {
            	e.printStackTrace();
            }
            catch (Throwable e)
            {
            	e.printStackTrace();
                log.error("Error in radius task Processor", e);
            }
        }
    }

    public void process() throws Exception
    {
        Object queueElement = this.queue.take();

		if (!(queueElement instanceof ListenerRequest))
        {
            throw new IllegalArgumentException("Expected ListenerRequest but found " + queueElement.getClass().getName());
        }

		ListenerRequest request = (ListenerRequest) queueElement;

		try
		{
			processRequest(request);
		}
		finally
		{
			ObjectPool pool = request.getBorrowedFromPool();
			
	        if (pool != null)
	        {
	        	pool.returnObject(request);
	        }
		}
    }

    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    public boolean getActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
        if (!active)
        {
            try
            {
                interrupt();
            }
            catch(Exception e)
            {
            }
        }
    }
}
