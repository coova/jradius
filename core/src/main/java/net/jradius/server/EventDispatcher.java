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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.jradius.handler.chain.JRCommand;
import net.jradius.log.RadiusLog;
import net.jradius.server.event.ServerEvent;
import net.jradius.session.JRadiusSession;
import net.jradius.session.JRadiusSessionManager;

/**
 * The JRadius Server Event (JRadiusEvent) Dispatcher.
 * 
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public class EventDispatcher extends JRadiusThread
{
    private BlockingQueue<JRadiusEvent> eventQueue;
    private List<JRCommand> eventHandlers;

    public EventDispatcher()
    {
        super();
        eventQueue = new LinkedBlockingQueue<JRadiusEvent>();
        eventHandlers = new LinkedList<JRCommand>();
    }

    public void post(JRadiusEvent event)
    {
        try
        {
            eventQueue.put(event);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        while (true)
        {
            try
            {
                Thread.yield();
                dispatchEvent();
            }
            catch (InterruptedException e)
            {
                return;
            }
            catch (Throwable e)
            {
                RadiusLog.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void dispatchEvent() throws InterruptedException
    {
        JRadiusEvent event = eventQueue.take();
        if (event != null)
        {
            JRadiusSessionManager sessionManager = JRadiusSessionManager.getManager(event.getSender());
            JRadiusSession session = null;

            if (event instanceof ServerEvent)
            {
                session = ((ServerEvent) event).getRequest().getSession();
            }

            if (session != null)
            {
                sessionManager.lock(session);
            }

            try
            {
                for (JRCommand command : eventHandlers)
                {
                    try
                    {
                        if (command.doesHandle(event)) 
                            if (command.execute(event)) 
                                break;
                    }
                    catch (Throwable e)
                    {
                        RadiusLog.error("Event handler " + command.getName() + " threw an exception:" + e);
                        e.printStackTrace();
                    }
                }
            }
            finally 
            {
                if (session != null)
                {
                    sessionManager.unlock(session);
                }
            }
        }
    }

    public List<JRCommand> getEventHandlers()
    {
        return eventHandlers;
    }

    public void setEventHandlers(List<JRCommand> eventHandlers)
    {
        this.eventHandlers = eventHandlers;
    }
}
