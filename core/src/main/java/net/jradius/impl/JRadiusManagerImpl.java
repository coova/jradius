package net.jradius.impl;

import net.jradius.JRadiusManager;
import net.jradius.log.RadiusLog;
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
import org.springframework.context.Lifecycle;

import java.io.InputStream;

public class JRadiusManagerImpl implements InitializingBean, BeanFactoryAware, DisposableBean, Lifecycle, JRadiusManager
{
    protected final Log log = LogFactory.getLog(getClass());
    private Boolean startOnLoad = Boolean.FALSE;
    private EventDispatcher eventDispatcher;
    private BeanFactory beanFactory;
    private JRadiusServer jRadiusServer;
    private String configFile;
    
    public JRadiusManagerImpl()
    {
    	System.err.println(this.getClass().toString());
    }
    
    public void start()
    {
    	jRadiusServer.start();
    }

    public void stop()
    {
    	jRadiusServer.stop();
    }

    public boolean isRunning()
    {
        return this.jRadiusServer.isRunning();
    }

    public void afterPropertiesSet() throws Exception
    {
        String filename = this.getConfigFile();

        if (filename == null || filename.trim().length() <= 0)
        {
            String message = "JRadiusManager: Missing settings filename ['configFile' property not specified correctly].";
            RadiusLog.error(message);
            throw new Exception(message);
        }

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        if (is == null)
        {
            String message = "File '" + filename + "' not found.";
            RadiusLog.error(message);
            throw new Exception(message);
        }

        Configuration.initialize(is, this.beanFactory);
        
        if (jRadiusServer == null)
        {
            jRadiusServer = new JRadiusServer(eventDispatcher);
            jRadiusServer.afterPropertiesSet();
        }
        
        if (startOnLoad.booleanValue())
        {
        	jRadiusServer.start();
        }
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

	public void setStartOnLoad(Boolean startOnLoad) {
		this.startOnLoad = startOnLoad;
	}
}
