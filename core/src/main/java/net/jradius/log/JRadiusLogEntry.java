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

import java.util.Date;

import net.jradius.exception.RadiusException;
import net.jradius.server.JRadiusRequest;
import net.jradius.session.JRadiusSession;



/**
 * The JRadius Log Entry Object (bean).
 * @author David Bird
 */
public interface JRadiusLogEntry
{
	public void init(JRadiusRequest request, JRadiusSession session) throws RadiusException;
	
	public void setType(String type);
	public void setCode(Integer code);
	public void setLastUpdate(Date lastUpdate);
	public Date getLastUpdate();
	public void addMessage(String message);

	public boolean isFinished();
	public boolean isCommitted();
	public void setCommitted(boolean committed);

	public String getInboundRequest();
	public void setInboundRequest(String string);

	public String getInboundReply();
	public void setInboundReply(String string);

	public String getOutboundRequest();
	public void setOutboundRequest(String string);

	public String getOutboundReply();
	public void setOutboundReply(String string);
}
