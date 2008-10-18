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
import net.jradius.log.JRadiusLogEntry;
import net.jradius.log.RadiusLog;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.JRadiusServer;
import net.jradius.server.event.SessionExpiredEvent;
import net.jradius.session.JRadiusSession;


/**
 * Handler to process the SessionExpiredEvent sent by the JRadius server. This 
 * handler should be placed in the default "event-handler" chain. 
 * @author David Bird
 */
public class SessionExpiredHandler extends EventHandlerBase
{
    /* (non-Javadoc)
     * @see net.jradius.handler.EventHandler#handle(net.jradius.server.JRadiusEvent)
     */
    public boolean handle(JRadiusEvent evt) throws Exception 
    {
        if (evt instanceof SessionExpiredEvent) 
        {
            SessionExpiredEvent event = (SessionExpiredEvent) evt;
            JRadiusSession session = event.getSession();
            if (session == null) return true;

            RadiusLog.debug("Processing Session Expired Event for Session: "
                    + session.getSessionKey());

            if (session.getSessionState() == JRadiusSession.RADIUS_ERROR) 
            {
                onRadiusError(event);
            } 
            else if ((session.getSessionState() & JRadiusSession.ACCT_STOPPED) != 0)
            {
                onAcctStopped(event);
            }
            else if ((session.getSessionState() & JRadiusSession.ACCT_STARTED) != 0)
            {
                onAcctStarted(event);
            }
            else if ((session.getSessionState() & JRadiusSession.AUTH_REJECTED) != 0)
            {
                onAuthRejected(event);
            }
            else if ((session.getSessionState() & JRadiusSession.AUTH_ACCEPTED) != 0)
            {
                Long serviceType = session.getServiceType();
                if (serviceType != null)
                {
                    int iServiceType = serviceType.intValue();
                    if (iServiceType != 6 &&  // Administrative-User
                        iServiceType != 8)    // Authenticate-Only
                    {
                        onAuthAccepted(event);
                    }
                }
            }
            else if ((session.getSessionState() & JRadiusSession.AUTH_REJECTED) != 0)
            {
                onAuthPending(event);
            }

            return true;
        }
        return false;
    }
    
    public void onRadiusError(SessionExpiredEvent event)
    {
        JRadiusSession session = event.getSession();
	    String error = "Session Expired in Error State";
	    RadiusLog.error(session.getSessionKey() + ": " + error);
	    JRadiusLogEntry logEntry = session.getLogEntry(event, "0");
	    logEntry.setType("error");
	    logEntry.addMessage(error);
	    session.commitLogEntries(JRadiusServer.RLM_MODULE_FAIL);
    }

    public void onAuthPending(SessionExpiredEvent event)
    {
	    RadiusLog.problem(null, event.getSession(), null,
	    		"Expired in Auth-Pending state (no response from service provider)");
    }

    public void onAuthAccepted(SessionExpiredEvent event)
    {
        JRadiusSession session = event.getSession();
	    String error = "Session Expired in Auth-Accepted state, Accounting never started";
	    RadiusLog.warn(session.getSessionKey() + ": " + error);
	    JRadiusLogEntry logEntry = session.getLogEntry(event, "0");
	    logEntry.setType("error");
	    logEntry.addMessage(error);
	    session.commitLogEntries(JRadiusServer.RLM_MODULE_FAIL);
    }

    public void onAuthRejected(SessionExpiredEvent event)
    {
        RadiusLog.debug("Rejected Session Expired: " + event);
    }

    public void onAcctStarted(SessionExpiredEvent event)
    {
        JRadiusSession session = event.getSession();
	    String error = "Session Expired in Accounting-Started state, Accounting never stopped";
	    RadiusLog.warn(session.getSessionKey() + ": " + error);
	    JRadiusLogEntry logEntry = session.getLogEntry(event, "0");
	    logEntry.setType("error");
	    logEntry.addMessage(error);
    }

    public void onAcctStopped(SessionExpiredEvent event)
    {
        RadiusLog.debug("Completed Session Expired: " + event);
    }
}
