/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006 David Bird <david@coova.com>
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

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import net.jradius.server.config.Configuration;

import org.apache.commons.configuration.ConfigurationException;

/**
 * Main for JRadius server. Reads a configuration file and starts
 * the JRadius server.
 * 
 * @author Gert Jan Verhoog
 */
public final class Main
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            showUsage();
            System.exit(1);
        }
        
        String configFilePath = args[0];

        try
        {
            File file = new File(configFilePath);
            Configuration.initialize(file);
            JRadiusServer server = new JRadiusServer();
            server.start();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error: The configuration file '" + configFilePath + "' does not exist.");
        }
        catch (ConfigurationException e1)
        {
            System.err.println("Error: The configuration file could not be read,"
                    + " because the file contains an error: "
                    + e1.getMessage());
            showStackTrace(e1);
        }
        catch (SecurityException e2)
        {
            System.err.println("Error: The configuration file could not be read," +
            		" because a security error occurred: "
                    + e2.getMessage());
            showStackTrace(e2);
        }
        catch (IllegalArgumentException e3)
        {
            System.err.println("Error: The configuration file could not be read,"
                    + " because an illegal argument error occurred: "
                    + e3.getMessage());
            showStackTrace(e3);
        }
        catch (ClassNotFoundException e4)
        {
            System.err.println("Error: The configuration file could not be read,"
                    + " because a class specified in the configuration file could not be found: "
                    + e4.getMessage());
            showStackTrace(e4);
        }
        catch (NoSuchMethodException e5)
        {
            System.err.println("Error: The configuration file could not be read,"
                    + " because a method does not exist in a class specified in the configuration file: "
                    + e5.getMessage());
            showStackTrace(e5);
        }
        catch (InstantiationException e6)
        {
            System.err.println("Error: The configuration file could not be read,"
                    + " because an object specified in the configuration file could not be instantiated: "
                    + e6.getMessage());
            showStackTrace(e6);
        }
        catch (IllegalAccessException e7)
        {
            System.err.println("Error: The configuration file could not be read,"
                    + " because an illegal access error occurred: "
                    + e7.getMessage());
            showStackTrace(e7);
        }
        catch (InvocationTargetException e8)
        {
            System.err.println("Error: The configuration file could not be read,"
                    + " because an invocation target exception was thrown: "
                    + e8.getMessage());
            showStackTrace(e8);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return;
    }

    private static void showStackTrace(Exception e)
    {
        System.err.println("--- stack trace: ------------------------------");
        e.printStackTrace(System.err);
        System.err.println("--- end of stack trace. -----------------------");
    }

    
    private static void showUsage()
    {
        System.err.println("Usage: jradius <configfile>");
        System.err.println("    where <configfile> is the filename of the configuration file.");
    }
}
