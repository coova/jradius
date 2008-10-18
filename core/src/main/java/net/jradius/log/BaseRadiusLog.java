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

package net.jradius.log;


import net.jradius.exception.RadiusException;
import net.jradius.server.JRadiusRequest;
import net.jradius.session.JRadiusSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The minimal default RadiusLogger.
 * @author David Bird
 */
public class BaseRadiusLog implements RadiusLogger
{
    private Log log = LogFactory.getLog(getClass());

    public void debug(Object o)
    {
        log.debug(o);
    }
    
    public void info(Object o)
    {
        log.info(o);
    }

    public void warn(Object o)
    {
        log.warn(o);
    }

    public void error(Object o)
    {
        log.error(o);
    }
    
    public String problemReport(JRadiusRequest request, JRadiusSession session, RadiusException exception, String mesg)
    {
        StringBuffer sb = new StringBuffer();
        if (mesg != null)
        {
            sb.append("Problem: ").append(mesg).append("\n\n");
        }
        if (request != null)
        {
            sb.append("-----------------------------------------------------------\n")
              .append("JRadiusRequest: ").append(request.toString()).append("\n")
              .append("-----------------------------------------------------------\n");
            try
            {
                sb.append("RADIUS Request:\n")
                  .append("-----------------------------------------------------------\n")
                  .append(request.getRequestPacket().toString())
                  .append("-----------------------------------------------------------\n")
                  .append("RADIUS Reply:\n")
                  .append("-----------------------------------------------------------\n")
                  .append(request.getReplyPacket().toString())
                  .append("-----------------------------------------------------------\n")
                  .append("\n\n");
            }
            catch (RadiusException e) {}
        }
        if (session != null)
        {
            sb.append("-----------------------------------------------------------\n")
              .append("RadiusSession:\n")
              .append("-----------------------------------------------------------\n")
              .append(session.toString())
              .append("\n\n");
        }
        if (exception != null)
        {
            sb.append("-----------------------------------------------------------\n")
              .append("RadiusException:\n")
              .append("-----------------------------------------------------------\n")
              .append(exception.toString())
              .append("\n\n");
        }
        return sb.toString();
    }


    public void problem(JRadiusRequest request, JRadiusSession session, RadiusException exception, String mesg)
    {
        log.error(problemReport(request, session, exception, mesg));
    }
}
