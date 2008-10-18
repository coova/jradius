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
 * JRadius RadiusLogger Interface.
 * @author David Bird
 */
public interface RadiusLogger
{
    public void debug(Object o);
    
    public void info(Object o);

    public void warn(Object o);

    public void error(Object o);

    public void problem(JRadiusRequest request, JRadiusSession session, RadiusException ex, String mesg);
}
