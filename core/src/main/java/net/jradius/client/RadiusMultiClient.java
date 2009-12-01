package net.jradius.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class RadiusMultiClient extends RadiusClient 
{
	CacheManager cacheManager;
	String requestCacheName;
	Cache requestCache;
	
	public RadiusMultiClient() throws SocketException 
	{
		super();
	}

	public RadiusMultiClient(DatagramSocket socket, InetAddress address, String secret, int authPort, int acctPort, int timeout) throws SocketException 
	{
		super(socket, address, secret, authPort, acctPort, timeout);
	}

	public RadiusMultiClient(DatagramSocket socket, InetAddress address, String secret) 
	{
		super(socket, address, secret);
	}

	public RadiusMultiClient(InetAddress address, String secret, int authPort, int acctPort, int timeout) throws SocketException 
	{
		super(address, secret, authPort, acctPort, timeout);
	}

	public RadiusMultiClient(InetAddress address, String secret) throws SocketException 
	{
		super(address, secret);
	}

	public RadiusMultiClient(RadiusClientTransport transport) 
	{
		super(transport);
	}
	
	
}
