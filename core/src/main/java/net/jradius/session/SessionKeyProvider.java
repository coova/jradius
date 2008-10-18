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

package net.jradius.session;

import java.io.Serializable;

import net.jradius.exception.RadiusException;
import net.jradius.server.JRadiusRequest;


/**
 * Session Key Provider generates keys in which to store RadiusSessions
 * into the Session Manager. 
 * @author Gert Jan Verhoog
 */
public interface SessionKeyProvider
{
    public Serializable getClassKey(JRadiusRequest request) throws RadiusException;
    public Serializable getAppSessionKey(JRadiusRequest request) throws RadiusException;
    public Serializable getRequestSessionKey(JRadiusRequest request) throws RadiusException;
}
