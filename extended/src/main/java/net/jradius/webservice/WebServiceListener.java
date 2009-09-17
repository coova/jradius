/**
 * JRadius - A Radius Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2006 David Bird <david@coova.com>
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.jradius.server.JRadiusEvent;
import net.jradius.server.ListenerRequest;
import net.jradius.server.TCPListener;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.springframework.beans.factory.InitializingBean;


/**
 * JRadius TCP/SSL Proxy Listen
 *
 * @author David Bird
 */
public class WebServiceListener extends TCPListener implements InitializingBean, CacheEventListener
{
    protected String cacheName = "ws-requests";
    protected Map requestMap;
    protected CacheManager cacheManager;
    protected Ehcache requestCache;
    protected Integer timeToLive;
    protected Integer idleTime;
    
    public JRadiusEvent parseRequest(ListenerRequest listenerRequest, InputStream inputStream) throws IOException, WebServiceException
    {
        DataInputStream reader = new DataInputStream(inputStream);
        WebServiceRequest request = new WebServiceRequest();
        
        String line = null;
        
        try
        {
	        line = reader.readLine();
        }
        catch (SocketException e)
        {
        	return null;
        }
        
        if (line == null) throw new WebServiceException("Invalid relay request");
        
        StringTokenizer tokens = new StringTokenizer(line);
        String method = tokens.nextToken();
        String uri = tokens.nextToken();
        String httpVersion = tokens.nextToken();
        
        if ("GET".equals(method)) request.setMethod(WebServiceRequest.GET);
        else if ("POST".equals(method)) request.setMethod(WebServiceRequest.POST);
        else if ("PUT".equals(method)) request.setMethod(WebServiceRequest.PUT);
        else throw new WebServiceException("Does not handle HTTP request method: " + method);
        
        request.setHttpVersion(httpVersion);
        
        try
        {
            request.setUri(new URI(uri));
        }
        catch (URISyntaxException e)
        {
            throw new WebServiceException(e.getMessage());
        }
        
        Map<String, String> headers = getHeaders(reader);
        request.setHeaderMap(headers);
        
        String clen = headers.get("content-length");
        if (clen != null)
        {
            request.setContent(getContent(reader, Integer.parseInt(clen)));
        }
        
        return request;
    }
    
    private Map<String, String> getHeaders(DataInputStream reader) throws IOException
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        String line;
        do
        {
            line = reader.readLine().trim();
            if (line != null && line.length() > 0)
            {
                String[] parts = line.split(":", 2);
                if (parts != null && parts.length == 2)
                {
                    map.put(parts[0].toLowerCase().trim(), parts[1].trim());
                }
                else break;
            }
            else break;
        }
        while (true);
        
        return map;
    }
    
    private byte[] getContent(DataInputStream reader, int clen) throws IOException
    {
        byte[] buf = new byte[clen];
        reader.readFully(buf);
        return buf;
    }
    
    public void remove(OTPProxyRequest request)
    {
        request.interrupt();
        if (requestMap != null)
            requestMap.remove(request.getOtpName());
        else
            requestCache.remove(request.getOtpName());
    }

    public void put(WebServiceRequestObject obj)
    {
        if (requestMap != null)
            requestMap.put(obj.getKey(), obj);
        else
            requestCache.put(new Element(obj.getKey(), obj));
    }
    
    public WebServiceRequestObject get(String username)
    {
        if (requestMap != null)
            return (WebServiceRequestObject)requestMap.get(username);
        Element e = requestCache.get(username);
        return e == null ? null : (WebServiceRequestObject)e.getValue();
    }

    private void deleteElement(Element e)
    {
        if (e==null) return;
        WebServiceRequestObject o = (WebServiceRequestObject)e.getValue();
        if (o==null) return;
        o.delete();
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public void dispose()
    {
    }

    public void notifyElementEvicted(Ehcache cache, Element element)
    {
        deleteElement(element);
    }

    public void notifyElementExpired(Ehcache cache, Element element)
    {
        deleteElement(element);
    }

    public void notifyElementPut(Ehcache cache, Element element) throws CacheException
    {
    }

    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException
    {
        deleteElement(element);
    }

    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException
    {
    }

    public void notifyRemoveAll(Ehcache cache)
    {
        List keys = cache.getKeys();
        for (Iterator i=keys.iterator(); i.hasNext();)
            deleteElement(cache.get(i.next()));
    }

    public void afterPropertiesSet() throws Exception
    {
        if (idleTime == null) idleTime = new Integer(120);
        if (timeToLive == null) timeToLive = new Integer(180);
        if (requestMap != null) return;
        if (requestCache == null) {
            if (cacheManager == null) 
                cacheManager = CacheManager.create();
            requestCache = cacheManager.getCache(cacheName);
            if (requestCache == null)
            {
                requestCache = new Cache(cacheName, 1000000, true, true, timeToLive.intValue(), idleTime.intValue());
                cacheManager.addCache(requestCache);
            }
        }
        requestCache.getCacheEventNotificationService().registerListener(this);
    }

    public CacheManager getCacheManager()
    {
        return cacheManager;
    }
    public void setCacheManager(CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }
    public String getCacheName()
    {
        return cacheName;
    }
    public void setCacheName(String cacheName)
    {
        this.cacheName = cacheName;
    }
    public Integer getIdleTime()
    {
        return idleTime;
    }
    public void setIdleTime(Integer idleTime)
    {
        this.idleTime = idleTime;
    }
    public Ehcache getRequestCache()
    {
        return requestCache;
    }
    public void setRequestCache(Ehcache requestCache)
    {
        this.requestCache = requestCache;
    }
    public Integer getTimeToLive()
    {
        return timeToLive;
    }
    public void setTimeToLive(Integer timeToLive)
    {
        this.timeToLive = timeToLive;
    }

    public Map getRequestMap()
    {
        return requestMap;
    }

    public void setRequestMap(Map requestMap)
    {
        this.requestMap = requestMap;
    }
}
