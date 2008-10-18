/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (C) 2007 David Bird <dbird@acm.org>
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

import net.jradius.dictionary.vsa_jradius.Attr_JRadiusRequestId;
import net.jradius.dictionary.vsa_jradius.Attr_JRadiusSessionId;
import net.jradius.exception.RadiusException;
import net.jradius.log.JRadiusLogEntry;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusRequest;
import net.jradius.session.JRadiusSession;

/**
 * Initializes and configures the JRadiusSession for the request.
 * @author David Bird
 */
public class InitSessionHandler extends RadiusSessionHandler
{
    /* (non-Javadoc)
     * @see net.jradius.handler.PacketHandler#handle(net.jradius.server.JRadiusRequest)
     */
    public boolean handle(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        AttributeList ci = request.getConfigItems();

        JRadiusSession session = request.getSession();
        JRadiusLogEntry logEntry = session.getLogEntry(request);
        
        // We set the type at every stage... the function, in fact,
        // only really sets the type when appropriate.
        logEntry.init(request, session);

        // Put some internal values into the ConfigItems for
        // easy processing of JRadius reuqests/sessions.
        
        if (ci.get(Attr_JRadiusSessionId.TYPE) == null)
            ci.add(new Attr_JRadiusSessionId(session.getSessionKey()));
        
        if (ci.get(Attr_JRadiusRequestId.TYPE) == null)
            ci.add(new Attr_JRadiusRequestId(Integer.toString(req.getIdentifier())));

        return session.onPreProcessing(request);
    }
}
