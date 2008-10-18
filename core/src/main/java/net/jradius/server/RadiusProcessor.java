/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (C) 2006-2007 David Bird <david@coova.com>
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

import net.jradius.exception.RadiusException;
import net.jradius.exception.RadiusSecurityException;
import net.jradius.handler.chain.JRCommand;
import net.jradius.log.RadiusLog;
import net.jradius.server.event.HandlerLogEvent;
import net.jradius.session.JRadiusSession;
import net.jradius.session.JRadiusSessionManager;


/**
 * Base abstract class of all RadiusProcessors
 * 
 * @author David Bird
 */
public abstract class RadiusProcessor extends Processor
{
    public RadiusProcessor()
    {
        super();
    }
    
    abstract protected void logReturnCode(int result, JRCommand handler);
    
    protected int handleRadiusException(JRadiusRequest request, RadiusException e)
    {
        JRadiusSession session = request.getSession();
        String error = e.getMessage();
        RadiusLog.error(error);

        if (session != null)
        {
            try
            {
                session.getLogEntry(request).addMessage(error);
            }
            catch (RadiusException re)
            {
                RadiusLog.problem(request, session, re, re.getMessage());
            }
            // lets not remove the session and let it expire, or maybe
            // this was a RADIUS retransmission that should simply be forgotten
            //session.setSessionState(JRadiusSession.RADIUS_ERROR);
            //sessionManager.removeSession(session);
        }

        return (e instanceof RadiusSecurityException) ? JRadiusServer.RLM_MODULE_REJECT :  JRadiusServer.RLM_MODULE_FAIL;
    }

    protected int runPacketHandlers(JRadiusRequest request)
    {
        List<JRCommand> handlers = getRequestHandlers();

        int result = JRadiusServer.RLM_MODULE_NOOP;
        boolean exceptionThrown = false;
        
        RadiusLog.debug("Processing JRadiusRequest: " + request.toString());
        
        if (handlers == null) return result;

        JRadiusSessionManager sessionManager = JRadiusSessionManager.getManager(request.getSender());

        if (sessionManager == null) 
        {
            RadiusLog.error("No JRadiusSessionManager for request: " + request.toString());
            return result;
        }

        JRadiusSession session = request.getSession();
        
        if (session == null)
        {
            try
            {
                session = sessionManager.getSession(request);
            }
            catch (RadiusException e)
            {
                String error = e.getMessage();
                String mesg = "Rejecting request: "+request.getRequestPacket().toString();
                
                RadiusLog.warn(mesg + ": " + error);
                RadiusLog.problem(request, null, e, mesg);
                return JRadiusServer.RLM_MODULE_REJECT;
            }
        }

        if (session == null)
        {
            RadiusLog.error("Unable to create session");
            return JRadiusServer.RLM_MODULE_REJECT;
        }

        request.setSession(session);

        sessionManager.lock(session);
        try
        {
            for (JRCommand handler : handlers)
            {
                boolean stop = false;
                try
                {
                    if (handler.doesHandle(request))
                    {
                        stop = handler.execute(request);
                        result = request.getReturnValue();
                        logReturnCode(result, handler);
                        if (stop) break;
                    }
                }
                catch (RadiusException e)
                {
                    exceptionThrown = true;
                    result = handleRadiusException(request, e);
                    logReturnCode(result, handler);
                    break;
                }
                catch (Throwable e)
                {
                    exceptionThrown = true;
                    e.printStackTrace();
                    String error = e.getMessage();
                    RadiusLog.error("Handler " + handler.getName() + " threw throwable: " + error);
                    result = JRadiusServer.RLM_MODULE_FAIL;
                    logReturnCode(result, handler);
                    break;
                }
            }
            
            if (session != null && !exceptionThrown) 
            {
                try
                {
                    session.onPostProcessing(request);
                }
                catch (RadiusException e)
                {
                    result = handleRadiusException(request, e);
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                    RadiusLog.error("onPostProcessing threw throwable: " + e.getMessage());
                    result = JRadiusServer.RLM_MODULE_FAIL;
                }
            }
            
            if (result == JRadiusServer.RLM_MODULE_REJECT && request.isAccountingRequest())
            {
                RadiusLog.debug("Ack'ing AccountingRequest that was rejected");
                result = JRadiusServer.RLM_MODULE_OK;
            }
           
            // Send a log-event to the event-dispatcher
            HandlerLogEvent log = new HandlerLogEvent(request, request.getSessionKey(), result);
            getEventDispatcher().post(log);
        }
        finally
        {
        	sessionManager.unlock(session);
        }
        return result;
    }
}
