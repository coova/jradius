package net.jradius.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: bvujnovic
 * Date: 2008.11.12
 * Time: 10:48:52
 * To change this template use File | Settings | File Templates.
 */
public class Log4JRadiusLogger implements RadiusLogger
{
    private static final Log log = LogFactory.getLog(Log4JRadiusLogger.class);

    public boolean isLoggable(int logLevel)
    {
        switch(logLevel)
        {
            case RadiusLogger.LEVEL_OFF:
                return true;
            case RadiusLogger.LEVEL_ERROR:
                return this.log.isErrorEnabled();
            case RadiusLogger.LEVEL_WARNING:
                return this.log.isWarnEnabled();
            default:
            case RadiusLogger.LEVEL_INFO:
                return this.log.isInfoEnabled();
            case RadiusLogger.LEVEL_DEBUG:
                return this.log.isDebugEnabled();
        }
    }

    public void error(String message)
    {
    	if (message != null)
        {
            this.log.error(message);
        }
    }

    public void error(String message, Throwable e)
    {
        if (message != null)
        {
        	if (e != null)
            {
                this.log.error(message, e);
            }
            else
            {
                this.log.error(message);
            }
        }
        else if (e != null)
        {
            this.log.error("", e);
        }
    }

    public void warn(String message)
    {
    	if (message != null)
        {
            this.log.warn(message);
        }
    }

    public void warn(String message, Throwable e)
    {
    	if (message != null)
        {
    		if (e != null)
            {
                this.log.warn(message, e);
            }
            else
            {
                this.log.warn(message);
            }
        }
    	else if (e != null)
        {
            this.log.warn("", e);
        }
    }

    public void info(String message)
    {
        if (message != null)
        {
            this.log.info(message);
        }
    }

    public void info(String message, Throwable e)
    {
    	if (message != null)
        {
    		if (e != null)
            {
            	this.log.info(message, e);
            }
            else
            {
                this.log.info(message);
            }
        }
    	else if (e != null)
        {
            this.log.info("", e);
        }
    }

    public void debug(String message)
    {
    	if (this.log.isDebugEnabled() == false)
        {
            return;
        }

    	if (message != null)
        {
            this.log.debug(message);
        }
    }

    public void debug(String message, Throwable e)
    {
    	if (this.log.isDebugEnabled() == false)
        {
            return;
        }

    	if (message != null)
        {
    		if (e != null)
            {
                this.log.debug(message, e);
            }
            else
            {
                this.log.debug(message);
            }
        }
        else if (e != null)
        {
            this.log.debug("", e);
        }
    }
}
