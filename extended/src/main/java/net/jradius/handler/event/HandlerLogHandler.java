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

package net.jradius.handler.event;

import net.jradius.handler.EventHandlerBase;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.event.HandlerLogEvent;
import net.jradius.session.JRadiusSession;


/**
 * The default (as defined in net.jradius.handler.catalog.xml) Event Handler. This 
 * handler commits all the log entries stored up in the session and is launched at the 
 * end of every pass through the JRadius server.
 * @author David Bird
 */
public class HandlerLogHandler extends EventHandlerBase
{
    /* (non-Javadoc)
     * @see net.jradius.handler.EventHandler#handle(net.jradius.server.JRadiusEvent)
     */
    public boolean handle(JRadiusEvent event) throws Exception
    {
        if (event instanceof HandlerLogEvent)
        {
            HandlerLogEvent evt = (HandlerLogEvent)event;
            
            JRadiusSession session = evt.getRequest().getSession();

            if (session != null) 
            {
                session.commitLogEntries(evt.getResult());
            }

            return true;
        }
        return false;
    }
}
