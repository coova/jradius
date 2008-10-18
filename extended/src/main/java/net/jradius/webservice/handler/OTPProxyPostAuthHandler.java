/**
 * JRadius - A Radius Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2007 David Bird <david@coova.com>
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package net.jradius.webservice.handler;

import net.jradius.dictionary.Attr_AuthType;
import net.jradius.dictionary.Attr_Class;
import net.jradius.dictionary.Attr_EAPMessage;
import net.jradius.dictionary.Attr_MessageAuthenticator;
import net.jradius.dictionary.Attr_State;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.vsa_jradius.Attr_JRadiusSessionId;
import net.jradius.handler.RadiusSessionHandler;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;
import net.jradius.session.JRadiusSession;
import net.jradius.webservice.OTPProxyRequest;
import net.jradius.webservice.WebServiceListener;

/**
 * @author David Bird
 */
public class OTPProxyPostAuthHandler extends RadiusSessionHandler
{
    private String listenerBean = "otpListener";

    public boolean handle(JRadiusRequest request) throws Exception
    {
        JRadiusSession session = (JRadiusSession) request.getSession();
        if (session == null) return noSessionFound(request);

        RadiusPacket req = request.getRequestPacket();
        RadiusPacket rep = request.getReplyPacket();
        AttributeList ci = request.getConfigItems();
        
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

        req.addAttribute(new Attr_JRadiusSessionId(session.getSessionKey()));
        
        otpRequest.setAccessRequest((RadiusRequest)req);

        RadiusResponse resp = otpRequest.getAccessResponse();
        
        if (resp == null)
        {
            ci.add(new Attr_AuthType(Attr_AuthType.Reject));
            request.setReturnValue(JRadiusServer.RLM_MODULE_REJECT);
            return true;
        }
        
        RadiusLog.debug(
                "------------------------------------------------\n"+
                "OTP Proxy Response:\n" + resp.toString()+
                "------------------------------------------------\n");
        
        if (resp instanceof AccessAccept)
        {
            AttributeList attrs = resp.getAttributes();
            attrs.remove(Attr_Class.TYPE);
            attrs.remove(Attr_State.TYPE);
            attrs.remove(Attr_EAPMessage.TYPE);
            attrs.remove(Attr_MessageAuthenticator.TYPE);
            rep.addAttributes(attrs);
            return false;
        }
        
        ci.add(new Attr_AuthType(Attr_AuthType.Reject));
        request.setReturnValue(JRadiusServer.RLM_MODULE_REJECT);
        return true;
    }
}
