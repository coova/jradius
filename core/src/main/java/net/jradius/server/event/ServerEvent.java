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

package net.jradius.server.event;

import net.jradius.server.JRadiusEvent;
import net.jradius.server.JRadiusRequest;

/**
 * Represents A Server Event and holds the associated JRadiusRequest.
 * @author David Bird
 */
public abstract class ServerEvent extends JRadiusEvent
{
    private JRadiusRequest request;
    private String sessionKey;

    public JRadiusRequest getRequest()
    {
        return request;
    }
    
    public void setRequest(JRadiusRequest request)
    {
        this.request = request;
    }
    
    /**
     * @return Returns the sessionKey.
     */
    public String getSessionKey()
    {
        return sessionKey;
    }

    /**
     * @param sessionKey The sessionKey to set.
     */
    public void setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
    }

    public int getType()
    {
        return 0;
    }
}
