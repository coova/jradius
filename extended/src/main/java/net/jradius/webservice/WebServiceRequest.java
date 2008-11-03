/**
 * JRadius - A Radius Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import net.jradius.server.JRadiusEvent;


/**
 * A Web Service Server Request
 *
 * @author David Bird
 */
public class WebServiceRequest extends JRadiusEvent
{
    public static final long serialVersionUID = 0L;
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;
    
    private int type;
    private int method;
    private String httpVersion;
    private URI uri;
    private byte[] content;

    private Map<String, String> headerMap;

    private Map<String, String> serverVariableMap;
    
    private X509Certificate clientCertificate;
    
    private WebServiceResponse response;
    
    private Object sessionObject;
    
    /**
     * @return the request type
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * Set the type of the request
     * @param type
     */
    public void setType(int type)
    {
        this.type = type;
    }
        
    public String getTypeString()
    {
        return "ws";
    }

    public String getContentAsString() throws UnsupportedEncodingException
    {
    	return getContent() == null ? null : new String(getContent(), "UTF-8");
    }
    
    /**
     * @return Returns the content.
     */
    public byte[] getContent()
    {
        return content;
    }
    /**
     * @param content The content to set.
     */
    public void setContent(byte[] content)
    {
        this.content = content;
    }
    /**
     * @return Returns the headerMap.
     */
    public Map<String, String> getHeaderMap()
    {
        return headerMap;
    }
    /**
     * @param headerMap The headerMap to set.
     */
    public void setHeaderMap(Map<String, String> headerMap)
    {
        this.headerMap = headerMap;
    }
    /**
     * @return Returns the method.
     */
    public int getMethod()
    {
        return method;
    }
    /**
     * @param method The method to set.
     */
    public void setMethod(int method)
    {
        this.method = method;
    }
    /**
     * @return Returns the uri.
     */
    public URI getUri()
    {
        return uri;
    }
    /**
     * @param uri The uri to set.
     */
    public void setUri(URI uri)
    {
        this.uri = uri;
    }
    
    /**
     * @return Returns the httpVersion.
     */
    public String getHttpVersion()
    {
        return httpVersion;
    }
    /**
     * @param httpVersion The httpVersion to set.
     */
    public void setHttpVersion(String httpVersion)
    {
        this.httpVersion = httpVersion;
    }
    /**
     * @return Returns the response.
     */
    public WebServiceResponse getResponse()
    {
        return response;
    }
    /**
     * @param response The response to set.
     */
    public void setResponse(WebServiceResponse reply)
    {
        this.response = reply;
    }

	public Object getSessionObject() {
		return sessionObject;
	}

	public void setSessionObject(Object sessionObject) {
		this.sessionObject = sessionObject;
	}

	public Map<String, String> getParameterMap() {
        /**
         * Get the query string parameters
         */
		String qs = getUri().getQuery();
        HashMap<String, String> map = new HashMap<String, String>();
        if (qs != null)
        {
            StringTokenizer st = new StringTokenizer(qs, "&");
            while (st.hasMoreTokens()) 
            {
                String param = st.nextToken();
                int i = param.indexOf("=");
                if (i != -1) 
                {
                    String k = param.substring(0, i);
                    String v = param.substring(i+1, param.length());
                	try
                	{
	                    map.put(k, URLDecoder.decode(v));
                	}
                	catch (Exception e)
                	{
	                    map.put(k, v);
                	}
                }
            }
        }
        
        if (serverVariableMap != null)
        	map.putAll(serverVariableMap);
        
        return map;
	}
	
	public String toString()
	{
		return getHeaderMap().toString();
	}

	public void setCertificate(X509Certificate x509) 
	{
		clientCertificate = x509;
	}
	
	public X509Certificate getCertificate()
	{
		return clientCertificate;
	}

	public Map<String, String> getServerVariableMap() {
		return serverVariableMap;
	}

	public void setServerVariableMap(Map<String, String> serverVariableMap) {
		this.serverVariableMap = serverVariableMap;
	}
}
