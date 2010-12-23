/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2007 David Bird <david@coova.com>
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

package net.jradius.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import net.jradius.exception.RadiusException;
import net.jradius.log.JRadiusLogEntry;
import net.jradius.log.RadiusLog;
import net.jradius.server.EventDispatcher;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.event.SessionExpiredEvent;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.event.CacheEventListener;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JRadiusSessionManager implements InitializingBean, ApplicationContextAware, CacheEventListener
{
    private static JRadiusSessionManager defaultManager;

    private static HashMap<String, JRadiusSessionManager> managers = new HashMap<String, JRadiusSessionManager>();

    private HashMap<String, SessionKeyProvider> providers = new HashMap<String, SessionKeyProvider>();
    private HashMap<String, SessionFactory> factories = new HashMap<String, SessionFactory>();

    private ApplicationContext applicationContext;

    private int minInterimInterval  = 300;
    private int maxInactiveInterval = 2100;

    private String cacheName = "jradius-session";
    private String logCacheName = "jradius-log";
    

    private CacheManager cacheManager;
    private Cache sessionCache;
    private Cache logCache;

    private EventDispatcher eventDispatcher;

    /**
     * There is a single JRadiusSessionManager available that
     * is accessible through this method. Using the default
     * application-wide manager is sufficient for most uses.
     * For specific needs, it is possible to create a new
     * JRadiusSessionManager object.
     * @return the default JRadiusSessionManager instance
     */
    public static JRadiusSessionManager getManager(Object name)
    {
        JRadiusSessionManager manager = null;
     
        if (name != null)
        {
            manager = (JRadiusSessionManager) managers.get(name);
        }

        if (manager == null) 
        {
            if (defaultManager == null)
            {
                defaultManager = new JRadiusSessionManager();
                try
                {
                    defaultManager.afterPropertiesSet();
                }
                catch (Exception e)
                {
                    RadiusLog.error("Error in JRadius Session Manager", e);
                }
            }
         
            manager = defaultManager;
        }
        
        return manager;
    }

    public static JRadiusSessionManager setManager(String name, JRadiusSessionManager manager)
    {
        if (name != null)
        {
            managers.put(name, manager);
        }
        else
        {
            defaultManager = manager;
        }

        return manager;
    }

    public static void shutdownManagers() 
    {
        if (defaultManager != null)
        {
            defaultManager.shutdown();
        }
        
        for (Iterator<?> i = managers.values().iterator(); i.hasNext();)
        {
            JRadiusSessionManager manager = (JRadiusSessionManager)i.next();
            manager.shutdown();
        }
    }
    
    /**
     * Creates a new JRadiusSessionManager instance. This
     * sets the key provider and session factory to the
     * DefaultSessionKeyProvider and RadiusSessionFactory,
     * respectively
     */
    public JRadiusSessionManager()
    {
        initialize();
    }
    
    private void initialize()
    {
        try
        {
            // If we can find the extended JRadius classes, configure
            // the default RadiusSessionKeyProvider and RadiusSessionFactory
            Class<?> c;
            c = Class.forName("net.jradius.session.RadiusSessionKeyProvider");
            providers.put(null, (SessionKeyProvider) c.newInstance());
            c = Class.forName("net.jradius.session.RadiusSessionFactory");
            factories.put(null, (SessionFactory) c.newInstance());
        }
        catch (Exception e)
        {
            RadiusLog.error("Could not find extended JRadius classes - not running JRadiusSessionManager", e);
            throw new RuntimeException(e);
        }
    }

    public void shutdown()
    {
    	if (cacheManager != null && cacheManager.getStatus() == Status.STATUS_ALIVE)
        {
            cacheManager.shutdown();
        }
    }
    
    public void afterPropertiesSet() throws Exception
    {
    	if ((sessionCache == null || logCache == null) && cacheManager == null) 
        {
        	throw new RuntimeException("cacheManager required");
        }

        if (sessionCache == null) 
        {
            sessionCache = cacheManager.getCache(cacheName);

            if (sessionCache == null)
            {
            	sessionCache = new Cache(cacheName, 50000, false, false, maxInactiveInterval, maxInactiveInterval);
                cacheManager.addCache(sessionCache);
            }
        }

        if (logCache == null) 
        {
            logCache = cacheManager.getCache(logCacheName);

            if (logCache == null)
            {
            	logCache = new Cache(logCacheName, 20000, false, false, maxInactiveInterval, maxInactiveInterval);
                cacheManager.addCache(logCache);
            }
        }

        sessionCache.getCacheEventNotificationService().registerListener(this);
        logCache.getCacheEventNotificationService().registerListener(this);
    }

    /**
     * Sets the key provider for this session manager. The
     * key provider generates a key to store a session with.
     * The key is generated based on a JRadiusRequest that is
     * passed to the key provider's getSessionKey method.
     * Keys are used to retrieve stored sessions from the session
     * manager.
     * @param name The name of the SessionKeyProvider (null for default)
     * @param provider The SessionKeyProvider
     * @see SessionKeyProvider
     */
    public void setSessionKeyProvider(String name, SessionKeyProvider provider)
    {
        providers.put(name, provider);
    }
    
    /**
     * Sets the session factory for this session manager. The
     * session factory generates a new session object, possibly
     * initialized based on values of a JRadiusRequest.
     * @param name The name of the SessionFactory (null for default)
     * @param factory a SessionFactory
     * @see SessionFactory
     */
    public void setSessionFactory(String name, SessionFactory factory)
    {
        factories.put(name, factory);
    }
    
    /**
     * returns the session manager's key provider
     * @param name The name of the SessionKeyProvider (null for default)
     * @return the session manager's key provider
     */
    public SessionKeyProvider getSessionKeyProvider(Object name)
    {
        SessionKeyProvider provider = (SessionKeyProvider)providers.get(name);
        if (provider == null && name != null) provider = (SessionKeyProvider)providers.get(null);
        return provider;
    }
    
    /**
     * returns the session manager's session factory
     * @param name The name of the SessionFactory (null for default)
     * @return the session manager's session factory
     */
    public SessionFactory getSessionFactory(Object name)
    {
        SessionFactory factory = (SessionFactory)factories.get(name);
        if (factory == null && name != null) factory = (SessionFactory)factories.get(null);
        return factory;
    }
    
    /**
     * Returns a session object. First, a key is generated by
     * the session manager's key provider, based on the JRadiusRequest.
     * If there is a stored session based on the key, this session is
     * returned, otherwise a new session created by the session factory
     * is returned
     * @param request a JRadiusRequest used to retrieve or generate a session with
     * @return Returns a RadiusSession
     * @throws RadiusException
     */
    public JRadiusSession getSession(JRadiusRequest request) throws RadiusException
    {
        SessionKeyProvider skp = getSessionKeyProvider(request.getSender());
        Serializable key = skp.getAppSessionKey(request);
        JRadiusSession session = null;
        Serializable nkey = null;
        
        if (key != null) 
        {
            RadiusLog.debug("** Looking for session: " + key);
            
            session = getSession(request, key);
            if (session == null)
            {
                RadiusLog.error("Broken JRadius-Session-Id implementation for session: " + key);
                key = null;
            }
        }

        if (key == null)
        {
            key = skp.getClassKey(request);
            
            if (key != null) 
            {
                RadiusLog.debug("** Looking for session: " + key);
                
                session = getSession(request, key);
                if (session == null)
                {
                    RadiusLog.error("Broken Class implementation for session: " + key);
                    key = null;
                }
                else
                {
                    if (session.getJRadiusKey() != null && !session.getJRadiusKey().equals(session.getSessionKey()))
                    {
                        rehashSession(session, session.getJRadiusKey(), key);
                    }
                }
            }
        }

        if (key == null)
        {
            Serializable keys = skp.getRequestSessionKey(request);
        
            if (keys == null)
            {
                return null;
            }
        
            if (keys instanceof Serializable[])
            {
                key = ((Serializable[])(keys))[0];
                nkey = ((Serializable[])(keys))[1];
                RadiusLog.debug("Rehashing session with key " + key + " under new key " + nkey);
            }
            else
            {
                key = keys;
            }
            
            RadiusLog.debug("** Looking for session: " + key);
            session = getSession(request, key);

            if (session != null && nkey != null && !nkey.equals(key))
            {
                rehashSession(session, key, nkey);
            }
        }        

        if (session == null) 
        {
            session = newSession(request, nkey == null ? key : nkey);
        }
        
        session.setTimeStamp(System.currentTimeMillis());
        session.setLastRadiusRequest(request);
        
        return session;
    }

    public void rehashSession(JRadiusSession session, Serializable okey, Serializable nkey) throws RadiusException
    {
        remove(okey);
        session.setJRadiusKey((String)nkey);
        put(session.getJRadiusKey(), session);
    }

    public JRadiusSession newSession(JRadiusRequest request, Object key) throws RadiusException
    {
        JRadiusSession session = (JRadiusSession) getSessionFactory(request.getSender()).newSession(request);
        session.setJRadiusKey((String)key);
        put(session.getJRadiusKey(), session);
        put(session.getSessionKey(), session);
        return session;
    }

    public JRadiusSession getSession(JRadiusRequest request, Serializable key) throws RadiusException
    {
        Element element = sessionCache.get(key);
        JRadiusSession session = null;

        if (element != null)
        {
        	session = (JRadiusSession) element.getValue();
        }
        
        if (session == null && request != null)
        {
            SessionFactory sf = getSessionFactory(request.getSender());
            session = sf.getSession(request, key);
            if (session != null)
            {
                put(session.getJRadiusKey(), session);
                put(session.getSessionKey(), session);
            }
        }
        
        if (session == null) return null;
        
        return session;
    }

    public void lock(JRadiusSession session)
    {
        session.lock();
        //RadiusLog.error("Appropriate session locking must be implemented");
    }

    public void unlock(JRadiusSession session, boolean save)
    {
        session.unlock();
        //RadiusLog.error("Appropriate session locking must be implemented");
    }

    public JRadiusLogEntry newLogEntry(JRadiusEvent event, JRadiusSession session, String packetId) 
    {
        Object sender = null;
        
        if (event != null) 
            sender = event.getSender();
        
        else if (session != null && session.getLastRadiusRequest() != null) 
            sender = session.getLastRadiusRequest().getSender();
        
        return getSessionFactory(sender).newSessionLogEntry(event, session, packetId);
    }
    
    public void removeSession(JRadiusSession session) 
    {
        if (session != null)
        {
            remove(session.getJRadiusKey());
            remove(session.getSessionKey());
        }
    }
    
    private void remove(Serializable key)
    {
    	RadiusLog.debug("Removing session key: " + key);
        sessionCache.remove(key);
    }

    private void put(Object key, Object value)
    {
        RadiusLog.debug("Adding session key: " + key);
        sessionCache.put(new Element(key, value));
    }

    public int getMaxInactiveInterval()
    {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(int maxInactiveInterval)
    {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public int getMinInterimInterval()
    {
        return minInterimInterval;
    }

    public void setMinInterimInterval(int minInterimInterval)
    {
        this.minInterimInterval = minInterimInterval;
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

    public void dispose()
    {
    }

    public void notifyElementEvicted(Ehcache cache, Element element)
    {
    }

    public void notifyElementExpired(Ehcache cache, Element element)
    {
        Object value = element.getValue();
        if (value instanceof JRadiusSession)
        {
            JRadiusSession session = (JRadiusSession) value;
            RadiusLog.debug("Expired session: " + session.getSessionKey());
            if (eventDispatcher != null)
            {
                SessionExpiredEvent evt = new SessionExpiredEvent(session);
                evt.setApplicationContext(applicationContext);
                eventDispatcher.post(evt);
            }
        }
    }

    public void notifyElementPut(Ehcache cache, Element element) throws CacheException
    {
    }

    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException
    {
    }

    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException
    {
    }

    public void notifyRemoveAll(Ehcache cache)
    {
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public Ehcache getSessionCache()
    {
        return sessionCache;
    }

    public void setSessionCache(Cache sessionCache)
    {
        this.sessionCache = sessionCache;
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher)
    {
		this.eventDispatcher = eventDispatcher;
	}

	public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }
}
