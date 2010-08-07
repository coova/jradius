/**
 * JRadius - A Radius Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (C) 2006-2007 David Bird <david@coova.com>
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package net.jradius.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Bird
 */
public class TCPListenerRequest extends ListenerRequest
{
    private Socket socket;
    private InputStream bin;
    private OutputStream bout;
    boolean keepAlive;

    public TCPListenerRequest()
    {
    }
    
    public TCPListenerRequest(Socket socket, Listener listener, boolean getEvent, boolean keepAlive) throws Exception
    {
    	accept(socket, listener, getEvent, keepAlive);
    }
    
    public TCPListenerRequest(Socket socket, InputStream bin, OutputStream bout, Listener listener, boolean getEvent, boolean keepAlive) throws Exception
    {
    	accept(socket, bin, bout, listener, getEvent, keepAlive);
    }
    
    public void accept(Socket socket, Listener listener, boolean getEvent, boolean keepAlive) throws Exception
    {
    	accept(socket, new BufferedInputStream(socket.getInputStream(), 4096), new BufferedOutputStream(socket.getOutputStream(), 4096), listener, getEvent, keepAlive);
    }
    
    public void accept(Socket socket, InputStream bin, OutputStream bout, Listener listener, boolean getEvent, boolean keepAlive) throws Exception
    {
    	this.listener = listener;
    	this.socket = socket;
    	this.bin = bin;
    	this.bout = bout;
    	this.keepAlive = keepAlive;
        
        if (getEvent)
        {
            this.event = getEventFromListener();
        }
	}

	public InputStream getInputStream() throws IOException
    {
    	return bin;
    }

    public OutputStream getOutputStream() throws IOException
    {
    	return bout;
    }

	public Map<String, String> getServerVariables() 
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put("REMOTE_ADDR", socket.getInetAddress().getHostAddress());
		return result;
	}
	
    public Socket getSocket() 
    {
        return socket;
    }

	public boolean isKeepAlive() 
	{
		return this.keepAlive;
	}
}
