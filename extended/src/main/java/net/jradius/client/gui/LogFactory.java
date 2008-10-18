package net.jradius.client.gui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.impl.LogFactoryImpl;

public class LogFactory extends LogFactoryImpl
{
    public Log getInstance(Class c) throws LogConfigurationException
    {
        return new LogImpl();
    }

    public class LogImpl implements Log
    {
        private void append(String text)
        {
            if (LogConsole.getInstance() != null) 
                LogConsole.getInstance().append(LogConsole.CATEGORY_ERROR, text);
        }

        public void debug(Object o, Throwable arg1)
        {
        }

        public void debug(Object o)
        {
        }

        public void error(Object o, Throwable arg1)
        {
            append(o.toString());
        }

        public void error(Object o)
        {
            append(o.toString());
        }

        public void fatal(Object o, Throwable arg1)
        {
            append(o.toString());
        }

        public void fatal(Object o)
        {
            append(o.toString());
        }

        public void info(Object o, Throwable arg1)
        {
        }

        public void info(Object o)
        {
        }

        public boolean isDebugEnabled()
        {
            return true;
        }

        public boolean isErrorEnabled()
        {
            return true;
        }

        public boolean isFatalEnabled()
        {
            return true;
        }

        public boolean isInfoEnabled()
        {
            return true;
        }

        public boolean isTraceEnabled()
        {
            return true;
        }

        public boolean isWarnEnabled()
        {
            return true;
        }

        public void trace(Object o, Throwable arg1)
        {
            append(o.toString());
        }

        public void trace(Object o)
        {
            append(o.toString());
        }

        public void warn(Object o, Throwable arg1)
        {
            append(o.toString());
        }

        public void warn(Object o)
        {
            append(o.toString());
        }
    }
};
