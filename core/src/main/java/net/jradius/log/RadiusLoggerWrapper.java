package net.jradius.log;

/**
 * Created by IntelliJ IDEA.
 * User: bvujnovic
 * Date: 2008.11.12
 * Time: 12:48:18
 * To change this template use File | Settings | File Templates.
 */
public interface RadiusLoggerWrapper
{
    public abstract RadiusLogger getRadiusLogger();
    public abstract void setRadiusLogger(RadiusLogger logger);
}
