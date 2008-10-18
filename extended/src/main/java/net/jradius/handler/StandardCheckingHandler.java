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

import net.jradius.exception.RadiusException;
import net.jradius.exception.StandardViolatedException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.server.JRadiusRequest;
import net.jradius.standard.RadiusStandard;


/**
 * A handler to verify a request complies with a standard (requires a
 * standardName configuration)
 * @author David Bird
 */
public class StandardCheckingHandler extends PacketHandlerBase
{
    private RadiusStandard standard;
    
    /**
     * This handler checks the request against the configured standard. If
     * the StandardViolationException is thrown, a problem report is genereate
     * and sent to the RadiusLog.
     * @see net.jradius.handler.PacketHandler#handle(net.jradius.server.JRadiusRequest)
     */
    public boolean handle(JRadiusRequest request) throws Exception
    {
        if (standard == null) return false;
        RadiusPacket req = request.getRequestPacket();
        RadiusPacket rep = request.getReplyPacket();
        String errMessage = "";
        RadiusException ex = null;

        if (req instanceof RadiusRequest)
        {
            try
            {
                standard.checkPacket(req);
            }
            catch (StandardViolatedException e)
            {
                errMessage += ": Request Missing: " + e.listAttributes();
                ex = e;
            }
        }
        if (rep instanceof RadiusResponse)
        {
            try
            {
                standard.checkPacket(rep);
            }
            catch (StandardViolatedException e)
            {
                errMessage += ": Response Missing: " + e.listAttributes();
                ex = e;
            }
        }
        if (ex != null)
        {
            RadiusLog.problem(request, request.getSession(), ex, errMessage.substring(2));
        }
        
        return false;
    }
    
    /**
     * This bean method is to support the chain configuration "standardName"
     * @param name The Class Name of the standard to check against.
     */
    public void setStandardName(String name)
    {
        try
        {
            Class clazz = Class.forName(name);
            setStandard((RadiusStandard)clazz.newInstance());
        }
        catch (Exception e)
        {
            RadiusLog.error("Could not initialize RadiusStandard " + name + ": " + e.getMessage());
        }
    }
    /**
     * @return Returns the standard.
     */
    public RadiusStandard getStandard() {
        return standard;
    }
    /**
     * @param standard The standard to set.
     */
    public void setStandard(RadiusStandard standard) {
        this.standard = standard;
    }
}
