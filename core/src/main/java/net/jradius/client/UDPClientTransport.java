package net.jradius.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import net.jradius.exception.RadiusException;
import net.jradius.exception.RadiusSecurityException;
import net.jradius.exception.TimeoutException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.PacketFactory;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.util.MessageAuthenticator;

public class UDPClientTransport extends RadiusClientTransport
{
    private static final RadiusFormat format = RadiusFormat.getInstance();
    
    public static final int defaultAuthPort = 1812;
    public static final int defaultAcctPort = 1813;

    protected DatagramSocket socket;

	public UDPClientTransport(DatagramSocket socket) {
		this.socket = socket;
		this.remoteInetAddress = socket.getInetAddress();
	}

	public UDPClientTransport() throws SocketException {
		this(new DatagramSocket());
	}

	public void close() {
		socket.close();
	}
	
    protected void send(RadiusRequest req, int attempt) throws Exception
    {
    	int port = req instanceof AccountingRequest ? acctPort : authPort;
    	
    	if (statusListener != null)
    		statusListener.onBeforeSend(this, req);
    	
        if (attempt > 1)
        {
            RadiusLog.warn("RadiusClient retrying request (attempt " + attempt + ")...");
        }
        byte[] b = format.packPacket(req, sharedSecret, true);
        DatagramPacket request = new DatagramPacket(b, b.length, getRemoteInetAddress(), port);
        socket.send(request);

        if (statusListener != null)
    		statusListener.onAfterSend(this);
    }
    
    protected RadiusResponse receive(RadiusRequest req) throws Exception
    {
        if (statusListener != null)
        	statusListener.onBeforeReceive(this);

        byte replyBytes[] = new byte[RadiusPacket.MAX_PACKET_LENGTH];
        DatagramPacket reply = new DatagramPacket(replyBytes, replyBytes.length);
        
        socket.receive(reply);
        
        RadiusPacket replyPacket = PacketFactory.parse(reply);
        
        if (!(replyPacket instanceof RadiusResponse))
        {
            throw new RadiusException("Received something other than a RADIUS Response to a Request");
        }

        if (statusListener != null)
        	statusListener.onAfterReceive(this, replyPacket);

        return (RadiusResponse)replyPacket;
    }
    
}
