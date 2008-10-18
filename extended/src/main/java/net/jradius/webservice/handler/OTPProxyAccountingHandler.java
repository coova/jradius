/**
 * JRadius - Java RADIUS client and server framework
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
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

package net.jradius.webservice.handler;

import net.jradius.dictionary.Attr_UserName;
import net.jradius.handler.RadiusSessionHandler;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.server.JRadiusRequest;
import net.jradius.session.JRadiusSession;
import net.jradius.webservice.OTPProxyRequest;
import net.jradius.webservice.WebServiceListener;

/**
 * @author David Bird
 */
public class OTPProxyAccountingHandler extends RadiusSessionHandler
{
    private String listenerBean = "otpListener";

    public boolean handle(JRadiusRequest request) throws Exception
    {
        JRadiusSession session = (JRadiusSession) request.getSession();
        if (session == null) return noSessionFound(request);

        RadiusPacket req = request.getRequestPacket();
        
        String username = (String)req.getAttributeValue(Attr_UserName.TYPE);
        
        if (request.getApplicationContext() == null)
        {
            RadiusLog.error(this.getClass().getName()+" can only run when configured with Spring");
            return false;
        }
        
        WebServiceListener wsListener = (WebServiceListener)request.getApplicationContext().getBean(listenerBean);
        if (wsListener == null) return false;
        OTPProxyRequest otpRequest = (OTPProxyRequest)wsListener.get(username);
        if (otpRequest == null) return false;

        otpRequest.updateAccounting((AccountingRequest)req);

        return false;
    }
}
