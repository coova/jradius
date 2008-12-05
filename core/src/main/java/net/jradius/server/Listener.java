package net.jradius.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import net.jradius.exception.RadiusException;
import net.jradius.server.config.ListenerConfigurationItem;

public interface Listener {

	public void setConfiguration(ListenerConfigurationItem cfg) throws Exception;

	public void setRequestQueue(BlockingQueue<ListenerRequest> queue);

	public String getName();

	public JRadiusEvent parseRequest(ListenerRequest listenerRequest, InputStream inputStream) throws IOException, RadiusException;

	public void start();

	public void stop();

    public boolean getActive();

    public void setActive(boolean active);
}
