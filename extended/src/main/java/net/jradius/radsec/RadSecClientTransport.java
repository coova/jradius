/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2009 Coova Technologies, LLC <support@coova.com>
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

package net.jradius.radsec;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import net.jradius.client.RadiusClientTransport;
import net.jradius.exception.RadiusException;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.PacketFactory;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;

public class RadSecClientTransport extends RadiusClientTransport
{
	private Socket socket;
	private Socket socketAcct;
	private KeyManager keyManagers[];
	private TrustManager trustManagers[];
	
    protected final ByteBuffer buffer_in;
    protected final ByteBuffer buffer_out;

	public RadSecClientTransport(KeyManager keyManager, TrustManager trustManager) 
	{
		this(new KeyManager[] { keyManager } , new TrustManager[] { trustManager });
	}

	public RadSecClientTransport(KeyManager keyManagers[], TrustManager trustManagers[]) 
	{
		this.keyManagers = keyManagers;
		this.trustManagers = trustManagers;
		
    	buffer_in = ByteBuffer.allocate(25000);
    	buffer_in.order(ByteOrder.BIG_ENDIAN);

    	buffer_out = ByteBuffer.allocate(25000);
    	buffer_out.order(ByteOrder.BIG_ENDIAN);
	}
	
	private void initialize()
	{
		try
		{
	        SSLContext sslContext = SSLContext.getInstance("SSLv3");
	        sslContext.init(keyManagers, trustManagers, null);
	        
	        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
	        socket = socketFactory.createSocket(getRemoteInetAddress(), getAuthPort());
	        socket.setReuseAddress(true);
	        socket.setSoTimeout(getSocketTimeout() * 1000);
	        
	        if (getAcctPort() != getAuthPort())
	        {
		        socketAcct = socketFactory.createSocket(getRemoteInetAddress(), getAcctPort());
		        socketAcct.setReuseAddress(true);
		        socketAcct.setSoTimeout(getSocketTimeout() * 1000);
	        }
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public RadiusResponse sendReceive(RadiusRequest p, int retries) throws RadiusException 
	{
		if (socket == null) initialize();
		return super.sendReceive(p, retries);
	}

	public void close() 
	{
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected RadiusResponse receive(RadiusRequest req) throws Exception {
        RadiusResponse res = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());

        synchronized (in) {
            if (statusListener != null)
        		statusListener.onBeforeReceive(this);
            
            int code = RadiusFormat.readUnsignedByte(in);
            int identifier = RadiusFormat.readUnsignedByte(in);
            int length = RadiusFormat.readUnsignedShort(in);

            buffer_in.clear();
            buffer_in.limit(in.read(buffer_in.array(), 0, length));
            
            res = (RadiusResponse) PacketFactory.parseUDP(code, identifier, length, buffer_in, false);

            if (statusListener != null)
        		statusListener.onAfterReceive(this, res);
		}
        
        return res;
	}

	@Override
	protected void send(RadiusRequest req, int attempt) throws Exception {
		Socket sock = socket;
		
		if (socketAcct != null && req instanceof AccountingRequest)
			sock = socketAcct;
		
        RadiusFormat format = RadiusFormat.getInstance();
        OutputStream out = sock.getOutputStream();

        buffer_out.clear();
        format.packPacket(req, "radsec", buffer_out, true);

        synchronized (out) {
            if (statusListener != null)
        		statusListener.onBeforeSend(this, req);

        	out.write(buffer_out.array(), 0, buffer_out.position());

        	if (statusListener != null)
        		statusListener.onAfterSend(this);
		}
	}
}
