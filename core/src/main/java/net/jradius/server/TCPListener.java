/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
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

package net.jradius.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.server.config.Configuration;
import net.jradius.server.config.ListenerConfigurationItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;

/**
 * The base abstract class of all Listeners
 * 
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public abstract class TCPListener extends JRadiusThread implements Listener
{
	protected Log log = LogFactory.getLog(getClass());

	protected boolean active = false;
    protected ListenerConfigurationItem config;
    
    protected BlockingQueue<ListenerRequest> queue;
    
    protected int port = 1814;
    protected int backlog = 1024;
    protected boolean requiresSSL = false;
    protected boolean usingSSL = false;
    protected boolean keepAlive;
 
    protected ServerSocket serverSocket;
    
    protected final List<KeepAliveListener> keepAliveListeners = new LinkedList<KeepAliveListener>();

    protected boolean sslWantClientAuth;
    protected boolean sslNeedClientAuth;
    protected String[] sslEnabledProtocols;
    protected String[] sslEnabledCiphers;

    protected ObjectPool requestObjectPool = new SoftReferenceObjectPool(new PoolableObjectFactory() 
    {
		public boolean validateObject(Object arg0) {
			return true;
		}
		
		public void passivateObject(Object arg0) throws Exception {
		}
		
		public Object makeObject() throws Exception {
			return new TCPListenerRequest();
		}
		
		public void destroyObject(Object arg0) throws Exception {
		}
		
		public void activateObject(Object arg0) throws Exception {
			TCPListenerRequest req = (TCPListenerRequest) arg0;
			req.clear();
		}
	});
    

    public void setConfiguration(ListenerConfigurationItem cfg) 
    {
    	try {
    		setConfiguration(cfg, false);
    	} catch (Exception e) {
    		e.printStackTrace();
    		RadiusLog.error("Invalid JRadius configuration.", e);
    	}
    }

    public void setConfiguration(ListenerConfigurationItem cfg, boolean noKeepAlive) 
        throws  KeyStoreException, NoSuchAlgorithmException, CertificateException, 
                UnrecoverableKeyException, KeyManagementException, IOException
    {
        keepAlive = !noKeepAlive;
        config = cfg;
        
        Map props = config.getProperties();
        
        String s = (String) props.get("port");
        if (s != null) port = new Integer(s).intValue();
        
        s = (String) props.get("backlog");
        if (s != null) backlog = new Integer(s).intValue();
        
        if (keepAlive) 
        {
            s = (String) props.get("keepAlive");
            if (s != null) keepAlive = new Boolean(s).booleanValue();
        }

        String useSSL = (String) props.get("useSSL");
        String trustAll = (String) props.get("trustAll");

        if (requiresSSL || "true".equalsIgnoreCase(useSSL))
        {
            KeyManager[] keyManagers = null;
            TrustManager[] trustManagers = null;
            
            String keyManager = (String) props.get("keyManager");
            
            if (keyManager != null && keyManager.length() > 0)
            {
    			try {
    				KeyManager manager = (KeyManager) Configuration.getBean(keyManager);
    	        	keyManagers = new KeyManager[] { manager };
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            }
            else
            {
	            String keystore         = (String) props.get("keyStore");
	            String keystoreType     = (String) props.get("keyStoreType");
	            String keystorePassword = (String) props.get("keyStorePassword");
	            String keyPassword      = (String) props.get("keyPassword");
	            
	            if (keystore != null)
	            {
	                if (keystoreType == null) keystoreType = "pkcs12";
	
	                KeyStore ks = KeyStore.getInstance(keystoreType);
	                ks.load(new FileInputStream(keystore), keystorePassword == null ? null : keystorePassword.toCharArray());
	
	                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	                kmf.init(ks, keyPassword == null ? null : keyPassword.toCharArray());
	                keyManagers = kmf.getKeyManagers();
	            }
            }
            
            String trustManager = (String) props.get("trustManager");

            if (trustManager != null && trustManager.length() > 0)
            {
    			try {
    	        	TrustManager manager = (TrustManager) Configuration.getBean(trustManager);
    	            trustManagers = new TrustManager[] { manager };
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            }
            else if ("true".equalsIgnoreCase(trustAll))
            {
                trustManagers = new TrustManager[]{ new X509TrustManager()
                {
                	public void checkClientTrusted(X509Certificate[] chain, String authType) 
                	{
                		
                	}
                	public void checkServerTrusted(X509Certificate[] chain, String authType) 
                	{
                		
                	}
                	public X509Certificate[] getAcceptedIssuers() 
                	{ 
                		return new X509Certificate[0]; 
                	}
                }};
            }
            else
            {
            	String keystore         = (String) props.get("caStore");
            	String keystoreType     = (String) props.get("caStoreType");
            	String keystorePassword = (String) props.get("caStorePassword");

                if (keystore != null)
                {
                    if (keystoreType == null) keystoreType = "pkcs12";

                    KeyStore caKeys = KeyStore.getInstance(keystoreType);
                    caKeys.load(new FileInputStream(keystore), keystorePassword == null ? null : keystorePassword.toCharArray());
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                    tmf.init(caKeys);
                    trustManagers = tmf.getTrustManagers();
                }
            }

            SSLContext sslContext = SSLContext.getInstance("SSLv3");
            sslContext.init(keyManagers, trustManagers, null);
            
            ServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) socketFactory.createServerSocket(port, backlog);
            serverSocket = sslServerSocket;

            if (sslWantClientAuth)
            	sslServerSocket.setWantClientAuth(true);
            
            if (sslNeedClientAuth)
            	sslServerSocket.setNeedClientAuth(true);
            
            if (sslEnabledProtocols != null)
            	sslServerSocket.setEnabledProtocols(sslEnabledProtocols);
            
            if (sslEnabledCiphers != null)
	            sslServerSocket.setEnabledCipherSuites(sslEnabledCiphers);

            usingSSL = true;
        }
        else
        {
        	serverSocket = new ServerSocket(port, backlog);
        }
        
        serverSocket.setReuseAddress(true);
        setActive(true);
    }
    
    /**
     * Sets the request queue for this listener
     * 
     * @param q the RequestQueue;
     */
    public void setRequestQueue(BlockingQueue<ListenerRequest> q)
    {
        queue = q;
    }

    /**
     * Sets the listeners ConfigurationItem
     * @param cfg a configuration item
     */
    public void setListenerConfigurationItem(ListenerConfigurationItem cfg)
    {
        config = cfg;
        this.setName(config.getName());
    }
    
    /**
     * Listen for one object and place it on the request queue
     * @throws IOException
     * @throws InterruptedException
     * @throws RadiusException
     */
    public void listen() throws Exception
    {
        RadiusLog.debug("Listening on socket...");
        Socket socket = serverSocket.accept();

    	socket.setTcpNoDelay(false);

        if (keepAlive)
        {
        	KeepAliveListener keepAliveListener = new KeepAliveListener(socket, this, queue);
            keepAliveListener.start();

            synchronized (keepAliveListeners)
            {
                keepAliveListeners.add(keepAliveListener);
            }
        }
        else
        {
        	TCPListenerRequest lr = (TCPListenerRequest) requestObjectPool.borrowObject();
        	lr.setBorrowedFromPool(requestObjectPool);
        	lr.accept(socket, this, false, false);

            while(true)
            {
                try
                {
                    this.queue.put(lr);
                    break;
                }
                catch(InterruptedException e)
                {
                }
            }
        }
    }
    
    public void deadKeepAliveListener(KeepAliveListener keepAliveListener)
    {
    }

    public boolean getActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
        if (!active)
        {
            for (KeepAliveListener listener : keepAliveListeners)
            {
                try { listener.shutdown(true); }
                catch (Throwable e) { }
            }

            this.keepAliveListeners.clear();

            try { this.serverSocket.close(); }
            catch (Throwable e) { }

            try { this.interrupt(); }
            catch(Exception e) { }
        }
    }
    
    /**
     * The thread's run method repeatedly calls listen()
     */
    public void run()
    {
        while (getActive())
        {
            try
            {
                Thread.yield();
                listen();
            }
            catch(SocketException e)
            {
            	if (getActive() == false)
                {
                    break;
                }
                else
                {
                    RadiusLog.error("Socket exception", e);
                }
            }
            catch (InterruptedException e)
            {
            }
            catch (SSLException e)
            {
                RadiusLog.error("Error occured in TCPListener.", e);
                active = false;
            }
            catch (Throwable e)
            {
                RadiusLog.error("Error occured in TCPListener.", e);
            }
        }

        RadiusLog.debug("Listener: " + this.getClass().getName() + " exiting (not active)");
    }

    public boolean isUsingSSL() 
    {
        return usingSSL;
    }

    public boolean isKeepAlive()
    {
        return keepAlive;
    }

    public void setBacklog(int backlog)
    {
        this.backlog = backlog;
    }

    public void setKeepAlive(boolean keepAlive)
    {
        this.keepAlive = keepAlive;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setUsingSSL(boolean usingSSL)
    {
        this.usingSSL = usingSSL;
    }
}
