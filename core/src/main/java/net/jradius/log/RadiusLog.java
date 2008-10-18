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


/**
 * JRadius Server Logger Class.
 * @author David Bird
 */
public final class RadiusLog 
{
    private static RadiusLogger radiusLogger = new BaseRadiusLog();

    public static void debug(Object o)
    {
        radiusLogger.debug(o); 
    }
    
    public static void info(Object o)
    {
        radiusLogger.info(o); 
    }

    public static void warn(Object o)
    {
        radiusLogger.warn(o); 
    }

    public static void error(Object o)
    {
        radiusLogger.error(o);
    }
    
    public static void problem(JRadiusRequest request, JRadiusSession session, RadiusException exception, String mesg)
    {
        radiusLogger.problem(request, session, exception, mesg);
    }
    
    /**
     * @return Returns the radiusLogger.
     */
    public static RadiusLogger getRadiusLogger()
    {
        return radiusLogger;
    }
    
    /**
     * @param radiusLogger The radiusLogger to set.
     */
    public static void setRadiusLogger(RadiusLogger radiusLogger)
    {
        RadiusLog.radiusLogger = radiusLogger;
    }
}
