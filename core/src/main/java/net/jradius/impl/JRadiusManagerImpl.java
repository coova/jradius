package net.jradius.impl;

import net.jradius.JRadiusManager;
import net.jradius.server.EventDispatcher;
import net.jradius.server.JRadiusServer;
import net.jradius.server.config.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class JRadiusManagerImpl implements InitializingBean, BeanFactoryAware, DisposableBean, JRadiusManager
{
    protected final Log log = LogFactory.getLog(getClass());
    private EventDispatcher eventDispatcher;
    private BeanFactory beanFactory;
    private JRadiusServer jRadiusServer;
    private String configFile;
    
    public void start()
    {
    	jRadiusServer.start();
    }

    public void stop()
    {
    	jRadiusServer.stop();
    }

    public void afterPropertiesSet() throws Exception
    {
        Configuration.initialize(Thread.currentThread().getContextClassLoader().getResourceAsStream(getConfigFile()), beanFactory);
        
        if (jRadiusServer == null)
        {
            jRadiusServer = new JRadiusServer(eventDispatcher);
            jRadiusServer.afterPropertiesSet();
        }

        jRadiusServer.start();
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    public String getConfigFile()
    {
        return configFile;
    }

    public void setConfigFile(String configFile)
    {
        this.configFile = configFile;
    }

    public void destroy() throws Exception
    {
        stop();
    }

	public void setJRadiusServer(JRadiusServer radiusServer) 
	{
		jRadiusServer = radiusServer;
	}

	public void setEventDispatcher(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}
    
}
