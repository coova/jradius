/**
 * JRadius.EWT Embedded Web Toolkit for JRadius/FreeRADIUS
 * Copyright (C) 2008 David Bird <david@coova.com>
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

package net.jradius.ewt.handler;

import java.net.URI;
import java.util.Map;

import net.jradius.log.RadiusLog;
import net.jradius.server.JRadiusEvent;
import net.jradius.webservice.WebServiceRequest;
import net.jradius.webservice.WebServiceResponse;

public class UIHandler extends CoreHandler
{   
    public UIHandler()
    {
    }
    
    public boolean doesHandle(JRadiusEvent event)
    {
        WebServiceRequest request = (WebServiceRequest) event;
        String path = request.getUri().getPath();
        return path.startsWith("/ewt/");
    }
    
    public boolean handle(JRadiusEvent event) throws Exception
    {
        WebServiceRequest request = (WebServiceRequest) event;
        
        URI uri = request.getUri();
        
        String path = uri.getRawPath().replace("/ewt/", "");
        RadiusLog.debug("EWT Request: " + path);

        byte[] content = ewtServerSupport.getResourceFile(path);
    
        WebServiceResponse response = new WebServiceResponse();
        response.setContent(content);
            
        Map<String, String> headers = response.getHeaders();
            
        if (path.endsWith(".gif"))
            headers.put("Content-Type", "image/gif");
        else if (path.endsWith(".jpg"))
            headers.put("Content-Type", "image/jpeg");
        else if (path.endsWith(".png"))
            headers.put("Content-Type", "image/png");
        else if (path.endsWith(".js"))
            headers.put("Content-Type", "application/javascript");
        else if (path.endsWith(".css"))
            headers.put("Content-Type", "text/css");
        else if (path.endsWith(".html"))
            headers.put("Content-Type", "text/html");
        else 
            return true;
        
        headers.put("Content-Length", ""+content.length);
            
        request.setResponse(response);
        return true;
    }
}
