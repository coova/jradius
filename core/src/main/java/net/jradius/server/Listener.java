package net.jradius.server;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import net.jradius.server.config.ListenerConfigurationItem;

public interface Listener {

	public void setConfiguration(ListenerConfigurationItem cfg) throws Exception;

	public void setRequestQueue(BlockingQueue<ListenerRequest> queue);
	
	//public void setRequestObjectPool(ObjectPool pool);

	public String getName();

	public JRadiusEvent parseRequest(ListenerRequest listenerRequest, ByteBuffer byteBuffer, InputStream inputStream) throws Exception;

	public void start();

	public void stop();

    public boolean getActive();

    public void setActive(boolean active);
}
