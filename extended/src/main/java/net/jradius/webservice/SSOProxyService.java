/**
 * JRadius - A Radius Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
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

package net.jradius.webservice;

import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import net.jradius.handler.EventHandlerBase;
import net.jradius.log.RadiusLog;
import net.jradius.server.JRadiusEvent;
import net.jradius.session.JRadiusSession;
import net.jradius.session.JRadiusSessionManager;
import net.jradius.util.Base64;


public class SSOProxyService extends EventHandlerBase
{   
    private String defaultURL = "http://jradius.net/";
    private String cipherType = "Blowfish";
    private String cipherKey = "changeme";
    
    public boolean doesHandle(JRadiusEvent event)
    {
        WebServiceRequest request = (WebServiceRequest) event;
        String path = request.getUri().getPath();
        return path.startsWith("/sso/");
    }
    
    public boolean handle(JRadiusEvent event) throws Exception
    {
        WebServiceRequest request = (WebServiceRequest) event;
        
        String path = request.getUri().getRawPath();
        RadiusLog.debug("SSO Proxy Request: " + path);
        
        String[] parts = path.split("/", 3);
        if (parts.length != 3) throw new WebServiceException("invalid request");

        String command = parts[1];
        String payload = parts[2];
        
        if (!"sso".equals(command)) throw new WebServiceException("invalid command");
        if (payload == null) throw new WebServiceException("invalid security");

        byte[] KeyData = cipherKey.getBytes();
        SecretKeySpec KS = new SecretKeySpec(KeyData, cipherType);
        Cipher cipher = Cipher.getInstance(cipherType);
        cipher.init(Cipher.DECRYPT_MODE, KS);
        
        byte[] data = Base64.decode(payload);
        byte[] plaintext = cipher.doFinal(data);

        String scommand = URLDecoder.decode(new String(plaintext).trim(), "US-ASCII");
        RadiusLog.debug("Secure command: " + scommand);
        String session = scommand.substring("session=".length());

        JRadiusSession radiusSession = (JRadiusSession)JRadiusSessionManager.getManager(request.getSender()).getSession(null, session);

        WebServiceResponse response = new WebServiceResponse();
        Map headers = response.getHeaders();

        String url = radiusSession.getRedirectURL();
        if (url == null) url = defaultURL;
        headers.put("Location", url);
        
        request.setResponse(response);

        return false;
    }

	public String getCipherKey() 
	{
		return cipherKey;
	}

	public void setCipherKey(String cipherKey) 
	{
		this.cipherKey = cipherKey;
	}

	public String getCipherType() 
	{
		return cipherType;
	}

	public void setCipherType(String cipherType) 
	{
		this.cipherType = cipherType;
	}

	public String getDefaultURL() 
	{
		return defaultURL;
	}

	public void setDefaultURL(String defaultURL) 
	{
		this.defaultURL = defaultURL;
	}
}
