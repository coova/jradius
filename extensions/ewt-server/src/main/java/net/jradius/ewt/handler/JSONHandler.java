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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.jradius.dal.dao.UIHelpDAO;
import net.jradius.dal.model.UIHelp;
import net.jradius.dal.model.UIHelpExample;
import net.jradius.server.JRadiusEvent;
import net.jradius.webservice.WebServiceRequest;
import net.jradius.webservice.WebServiceResponse;

import com.coova.ewt.server.EWTServerSupport.EWTResponse;

public class JSONHandler extends CoreHandler 
{   
    private UIHelpDAO uiHelpDAO;

    public JSONHandler()
    {
    }
    
    public boolean doesHandle(JRadiusEvent event)
    {
        WebServiceRequest request = (WebServiceRequest) event;
        String path = request.getUri().getPath();
        return path.startsWith("/ewt/json");
    }

    protected EWTResponse handleEWT(Map map, WebServiceRequest request) throws Exception
    {
    	return ewtServerSupport.handle(map, request.getContentAsString());
    }
    
    public boolean handle(JRadiusEvent event) throws Exception
    {
    	WebServiceRequest request = (WebServiceRequest) event;
        
        String error = "no response";

        Map<String, String> map = request.getParameterMap();
        
        EWTResponse ewtResponse = handleEWT(map, request);

        if (ewtResponse == null)
        {
        	try
        	{
                String res = map.get("res");
                /**
                 * ***  Switch based on "res" == "service"
                 */
                if ("service".equals(res))
                {
                    String s = map.get("s");
                    
                    Runtime rtime = Runtime.getRuntime();
                    Process child = rtime.exec("ewt/"+s);
                    
                    if (request.getContent() != null)
                    {
    	                BufferedWriter outCommand = new BufferedWriter(new OutputStreamWriter(child.getOutputStream()));
    	                outCommand.write(request.getContentAsString());
    	                outCommand.flush();
                    }
                    
    	            BufferedReader rd = new BufferedReader(new InputStreamReader(child.getInputStream()));
    	            StringBuffer sb = new StringBuffer();
    	            String line;
    	            
    	            while ((line = rd.readLine()) != null) 
    	            	sb.append(line).append("\n");

                    ewtResponse = ewtServerSupport.new EWTResponse(ewtServerSupport.htmlType, sb.toString().getBytes());
                }
                /**
                 * ***  Switch based on "res" == "help"
                 */
                else if ("help".equals(res) && uiHelpDAO != null)
                {
                    String s = map.get("s");
                    
                    UIHelpExample example = new UIHelpExample();
                    example.createCriteria().andKeyidEqualTo(s);
                    
                    List<UIHelp> list = uiHelpDAO.selectByExample(example);

                    byte[] reply = "Help not available".getBytes();
                    if (list != null && list.size() > 0)
                    {
                        reply = list.get(0).getHelptext().getBytes();
                    }

                    ewtResponse = ewtServerSupport.new EWTResponse(ewtServerSupport.htmlType, reply);
                }
        	}
        	catch (Exception e)
        	{
        		error = e.getMessage();
        		e.printStackTrace();
        	}
        }

        if (ewtResponse == null) {
        	ewtResponse = ewtServerSupport.new EWTResponse(ewtServerSupport.jsonType, 
        			("{error: '"+error+"'}").getBytes());
        }
        
        WebServiceResponse response = new WebServiceResponse();

        if (response == null) return true;
        
        response.setContent(ewtResponse.getContent());
                    
        Map<String, String> headers = response.getHeaders();

        headers.put("Content-Type", ewtResponse.getType());
        headers.put("Content-Length", ""+ewtResponse.getContentLength());
        headers.put("Cache-Control", "no-cache, must-revalidate");
        headers.put("Pragma", "no-cache" );
        request.setResponse(response);
        
        return true;
    }
    

    public UIHelpDAO getUiHelpDAO()
    {
        return uiHelpDAO;
    }

    public void setUiHelpDAO(UIHelpDAO uiHelpDAO)
    {
        this.uiHelpDAO = uiHelpDAO;
    }
}
