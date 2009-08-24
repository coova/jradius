package net.jradius.client;

import net.jradius.packet.RadiusPacket;

public interface TransportStatusListener {
	public void onBeforeReceive(RadiusClientTransport transport);
	public void onAfterReceive(RadiusClientTransport transport, RadiusPacket packet);
	public void onBeforeSend(RadiusClientTransport transport, RadiusPacket packet);
	public void onAfterSend(RadiusClientTransport transport);
}
