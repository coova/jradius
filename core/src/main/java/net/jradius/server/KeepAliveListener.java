/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2006-2007 David Bird <david@coova.com>
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

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import net.jradius.log.RadiusLog;

/**
 * @author David Bird
 */
public class KeepAliveListener extends JRadiusThread
{
    private Socket socket;
    private TCPListener listener;
    private BlockingQueue<ListenerRequest> queue;
    
    public KeepAliveListener(Socket socket, TCPListener listener, BlockingQueue<ListenerRequest> queue)
    {
        this.socket = socket;
        this.listener = listener;
        this.queue = queue;
    }
    
    public void run()
    {
        RadiusLog.debug("JRadius/KeepAliveListener.run(): starting tcp socket listener");

        try
        {
            while (true)
            {
                //connects to input stream, try to parse input and if succeeded create new TCPListenerRequest
                ListenerRequest lr = new TCPListenerRequest(this.socket, this.listener, true);

                //enqueue item to list so one of processors can start processing
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
        catch (Exception e)
        {
            RadiusLog.debug("JRadius/KeepAliveListener.run(): shutting down tcp socket listener", e);
        }
        
        shutdown(false);

        listener.deadKeepAliveListener(this);
    }
    
    public void shutdown(boolean tryToInterrupt)
    {
        if (this.socket != null)
        {
            try
            {
                this.socket.shutdownInput();
            }
            catch (Exception e)
            {
            }

            try
            {
                this.socket.shutdownOutput();
            }
            catch (Exception e)
            {
            }

            try
            {
                this.socket.close();
            }
            catch (Exception e)
            {
            }

            this.socket = null;

            if(tryToInterrupt)
            {
                try
                {
                    this.interrupt();
                }
                catch(Exception e)
                {
                }
            }
        }
    }
}
