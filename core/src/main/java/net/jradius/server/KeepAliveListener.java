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
        try
        {
            while (true)
            {
                queue.put(new TCPListenerRequest(socket, listener, true));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        shutdown();

        listener.deadKeepAliveListener(this);
    }
    
    public void shutdown()
    {
        if (socket == null) return;
        try { socket.shutdownInput(); }
        catch (Exception e) { }
        try { socket.shutdownOutput(); }
        catch (Exception e) { }
        try { socket.close(); }
        catch (Exception e) { }
        socket = null;
    }
}

