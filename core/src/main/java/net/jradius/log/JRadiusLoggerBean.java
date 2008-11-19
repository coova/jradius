package net.jradius.log;

/**
 * Created by IntelliJ IDEA.
 * User: bvujnovic
 * Date: 2008.11.12
 * Time: 11:23:30
 * To change this template use File | Settings | File Templates.
 */
public class JRadiusLoggerBean implements RadiusLoggerWrapper, RadiusLogger
{
    public RadiusLogger getRadiusLogger()
    {
        return this.logger;
    }

    public void setRadiusLogger(RadiusLogger logger)
    {
        if(logger == null)
        {
            throw new IllegalArgumentException("logger");
        }

        this.logger = logger;
    }

    public boolean isLoggable(int logLevel)
    {
        return this.logger.isLoggable(logLevel);
    }

    public void error(String message)
    {
        this.logger.error(message);
    }

    public void error(String message, Throwable e)
    {
        this.logger.error(message, e);
    }

    public void warn(String message)
    {
        this.logger.warn(message);
    }

    public void warn(String message, Throwable e)
    {
        this.logger.warn(message, e);
    }

    public void info(String message)
    {
        this.logger.info(message);
    }

    public void info(String message, Throwable e)
    {
        this.logger.info(message, e);
    }

    public void debug(String message)
    {
        this.logger.debug(message);
    }

    public void debug(String message, Throwable e)
    {
        this.logger.debug(message, e);
    }

    private RadiusLogger logger = new Log4JRadiusLogger();
}
