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

package net.jradius.handler.accounting;

import net.jradius.dictionary.Attr_Class;
import net.jradius.exception.RadiusException;
import net.jradius.handler.RadiusSessionHandler;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.server.JRadiusRequest;
import net.jradius.session.JRadiusSession;

/**
 * Check for the Class Attribute set by PostAuthorizeClassHandler. If not found,
 * then add a log message stating the NAS did not support this attribute. After
 * this handler, the attribute is no longer needed and is deleted.
 *
 * @author David Bird
 * @see net.jradius.handler.authorize.PostAuthorizeClassHandler
 */
public class AccountingClassHandler extends RadiusSessionHandler
{
    public boolean handle(JRadiusRequest request) throws RadiusException
    {
        JRadiusSession session = request.getSession();
        if (session == null) return noSessionFound(request);

        RadiusPacket req = request.getRequestPacket();
        
        byte[] bClass = (byte[]) req.getAttributeValue(Attr_Class.TYPE);
        if (bClass != null)
        {
            String sClass = new String(bClass);
            if (sClass.startsWith(ClassPrefix))
            {
                if (session.getRadiusClass() != null)
                {
                	RadiusAttribute cattr = AttributeFactory.newAttribute(Attr_Class.TYPE, session.getRadiusClass());
                    req.overwriteAttribute(cattr);
                }
                else
                {
                    req.removeAttribute(Attr_Class.TYPE);
                }
                return false;
            }
        }

        session.addLogMessage(request, "Accounting without Class Attribute");

        return false;
    }
}
