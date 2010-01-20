/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
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

package net.jradius.packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import net.jradius.client.RadiusClient;
import net.jradius.exception.RadiusException;
import net.jradius.freeradius.FreeRadiusFormat;
import net.jradius.log.RadiusLog;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;


/**
 * RADIUS Packet Factor. Parses RADIUS packets and constructs
 * the appropriate Java class instance. 
 *
 * @author David Bird
 */
public class PacketFactory
{
    private static LinkedHashMap<Integer, Class<?>> codeMap = new LinkedHashMap<Integer, Class<?>>();
    
    static
    {
        codeMap.put(new Integer(0),       				   NullPacket.class);           // 0

        codeMap.put(new Integer(AccessRequest.CODE),       AccessRequest.class);        // 1
        codeMap.put(new Integer(AccessAccept.CODE),        AccessAccept.class);         // 2
        codeMap.put(new Integer(AccessReject.CODE),        AccessReject.class);         // 3
        codeMap.put(new Integer(AccountingRequest.CODE),   AccountingRequest.class);    // 4
        codeMap.put(new Integer(AccountingResponse.CODE),  AccountingResponse.class);   // 5
        codeMap.put(new Integer(AccountingStatus.CODE),    AccountingStatus.class);     // 6
        codeMap.put(new Integer(PasswordRequest.CODE),     PasswordRequest.class);      // 7
        codeMap.put(new Integer(PasswordAck.CODE),         PasswordAck.class);          // 8
        codeMap.put(new Integer(PasswordReject.CODE),      PasswordReject.class);       // 9
        codeMap.put(new Integer(AccessChallenge.CODE),     AccessChallenge.class);      // 11
        codeMap.put(new Integer(DisconnectRequest.CODE),   DisconnectRequest.class);    // 40
        codeMap.put(new Integer(DisconnectACK.CODE),       DisconnectACK.class);        // 41
        codeMap.put(new Integer(DisconnectNAK.CODE),       DisconnectNAK.class);        // 42
        codeMap.put(new Integer(CoARequest.CODE),          CoARequest.class);           // 43
        codeMap.put(new Integer(CoAACK.CODE),              CoAACK.class);               // 44
        codeMap.put(new Integer(CoANAK.CODE),              CoANAK.class);               // 45

        codeMap.put(new Integer(DHCPDiscover.CODE),        DHCPDiscover.class);         // 1025
        codeMap.put(new Integer(DHCPOffer.CODE),           DHCPOffer.class);            // 1026
        codeMap.put(new Integer(DHCPRequest.CODE),         DHCPRequest.class);          // 1027
        codeMap.put(new Integer(DHCPDecline.CODE),         DHCPDecline.class);          // 1028
        codeMap.put(new Integer(DHCPAck.CODE),             DHCPAck.class);              // 1029
        codeMap.put(new Integer(DHCPNack.CODE),            DHCPNack.class);             // 1030
        codeMap.put(new Integer(DHCPRelease.CODE),         DHCPRelease.class);          // 1031
        codeMap.put(new Integer(DHCPInform.CODE),          DHCPInform.class);           // 1032
        codeMap.put(new Integer(DHCPForceRenew.CODE),      DHCPForceRenew.class);       // 1033
    }

    private static KeyedObjectPool pktObjectPool = new GenericKeyedObjectPool(new KeyedPoolableObjectFactory() 
    {
		public boolean validateObject(Object arg0, Object arg1) 
		{
			return true;
		}
		
		public void passivateObject(Object arg0, Object arg1) throws Exception 
		{
			RadiusPacket p = (RadiusPacket) arg1;
			p.recycled = true;
		}
		
		public Object makeObject(Object arg0) throws Exception 
		{
			RadiusPacket p = createPacket((Integer) arg0);
			p.recyclable = true;
			p.recycled = false;
			return p;
		}
		
		public void destroyObject(Object arg0, Object arg1) throws Exception 
		{
		}
		
		public void activateObject(Object arg0, Object arg1) throws Exception 
		{
			RadiusPacket p = (RadiusPacket) arg1;
			p.setAuthenticator(null);
			p.recycled = false;
		}
		
	}, -1);

    private static RadiusPacket createPacket(Integer code) throws Exception
    {
		Class<?> c = (Class<?>) codeMap.get(code);
        if (c == null)
        {
            throw new RadiusException("bad radius code");
        }
        RadiusPacket p = (RadiusPacket) c.newInstance();
        // System.err.println("Created packet " + p.toString());
        return p;
    }
    
    public static RadiusPacket newPacket(Integer code)
    {
    	try 
    	{
            if (pktObjectPool != null)
            {
            	RadiusPacket p = (RadiusPacket) pktObjectPool.borrowObject(code);
            	// System.err.println("Borrowed packet " + p.toString());
            	return p;
            }
          
            return createPacket(code);
		} 
    	catch (Exception e)
    	{
			throw new RuntimeException(e);
		}
    }

    public static RadiusPacket newPacket(byte b)
    {
    	return newPacket(new Integer(b));
    }

    public static RadiusPacket newPacket(byte b, int identifier)
    {
    	RadiusPacket p = newPacket(new Integer(b));
    	p.setIdentifier(identifier);
    	return p;
    }

    public static RadiusPacket newPacket(byte b, int identifier, AttributeList list)
    {
    	RadiusPacket p = newPacket(new Integer(b));
    	p.setIdentifier(identifier);
    	p.getAttributes().add(list);
    	return p;
    }

    public static RadiusPacket newPacket(byte b, AttributeList list)
    {
    	RadiusPacket p = newPacket(new Integer(b));
    	p.getAttributes().add(list);
    	return p;
    }

    public static RadiusRequest newPacket(byte b, RadiusClient client, AttributeList list)
    {
    	RadiusRequest p = (RadiusRequest) newPacket(new Integer(b));
    	p.setRadiusClient(client);
    	p.getAttributes().add(list);
    	return p;
    }


    /**
     * Parse a UDP RADIUS message
     * @param dp The Datagram to be parsed
     * @return Returns the RadiusPacket
     * @throws RadiusException
     */
    public static RadiusPacket parse(DatagramPacket dp) throws RadiusException
    {
    	ByteBuffer buffer = ByteBuffer.wrap(dp.getData(), dp.getOffset(), dp.getLength());
        RadiusPacket rp = null;

        try
        {
            rp = parseUDP(buffer);
        }
        catch (IOException e)
        {
            RadiusLog.error(e.getMessage(), e);
        }
        
        return rp;
    }

    public static RadiusPacket parseUDP(ByteBuffer buffer) throws RadiusException, IOException
    {
        int code = RadiusFormat.getUnsignedByte(buffer);
        int identifier = RadiusFormat.getUnsignedByte(buffer);
        int length = RadiusFormat.getUnsignedShort(buffer);

        return parseUDP(code, identifier, length, buffer);
    }
    
    public static RadiusPacket parseUDP(int code, int identifier, int length, ByteBuffer buffer) throws RadiusException, IOException
    {
    	RadiusPacket rp = null;
        Integer key = new Integer(code);

        if (pktObjectPool != null)
        {
        	try
        	{
        		rp = (RadiusPacket) pktObjectPool.borrowObject(key);
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }
        
        if (rp == null)
        {
	        Class<?> c = (Class<?>) codeMap.get(key);
	     
	        if (c == null)
	        {
	            throw new RadiusException("bad radius code");
	        }

	        try
	        {
	            rp = (RadiusPacket)c.newInstance();
	        }
	        catch (Exception e)
	        {
	            RadiusLog.error(e.getMessage(), e);
	            return null;
	        }
        }
        
        byte[] bAuthenticator = new byte[16];
        buffer.get(bAuthenticator);

        rp.setIdentifier(identifier);
        rp.setAuthenticator(bAuthenticator);
        
        length -= RadiusPacket.RADIUS_HEADER_LENGTH;
        if (length > 0)
        {
        	RadiusFormat.setAttributeBytes(rp, buffer, length);
        }
        
        return rp;
    }

    /*
    public static RadiusPacket parsePacket(InputStream in) throws RadiusException, IOException
    {
        RadiusPacket rp = null;
        int code = (int) RadiusFormat.readUnsignedInt(in);
        int identifier = (int) RadiusFormat.readUnsignedInt(in);

        Integer key = new Integer(code);
        
        if (pktObjectPool != null)
        {
        	try
        	{
        		rp = (RadiusPacket) pktObjectPool.borrowObject(key);
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }
        
        if (rp == null)
        {
	        Class<?> c = (Class<?>) codeMap.get(key);
	        if (c == null)
	        {
	            throw new RadiusException("bad radius packet type: " + code);
	        }
	        try
	        {
	            rp = (RadiusPacket) c.newInstance();
	        }
	        catch (Exception e)
	        {
	            RadiusLog.error(e.getMessage(), e);
	        }
        } 

        long length = Format.readUnsignedInt(in);
        byte[] bAttributes = new byte[(int) length];
        in.read(bAttributes);
        
        try
        {
            rp.setIdentifier(identifier);
            FreeRadiusFormat.setAttributeBytes(rp, bAttributes);
        }
        catch (Exception e)
        {
            RadiusLog.error(e.getMessage(), e);
        }
        return rp;
    }
    */
    
    public static RadiusPacket parsePacket(ByteBuffer buffer) throws RadiusException
    {
        RadiusPacket rp = null;
        int code = (int) Format.getUnsignedInt(buffer);
        int identifier = (int) Format.getUnsignedInt(buffer);
        long length = Format.getUnsignedInt(buffer);

        Integer key = new Integer(code);
        
        if (pktObjectPool != null)
        {
        	try
        	{
        		rp = (RadiusPacket) pktObjectPool.borrowObject(key);
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }
        
        if (rp == null)
        {
	        Class<?> c = (Class<?>) codeMap.get(key);
	        if (c == null)
	        {
	            throw new RadiusException("bad radius packet type: " + code);
	        }
	        try
	        {
	            rp = (RadiusPacket) c.newInstance();
	        }
	        catch (Exception e)
	        {
	            RadiusLog.error(e.getMessage(), e);
	        }
        } 

        try
        {
            rp.setIdentifier(identifier);
            FreeRadiusFormat.setAttributeBytes(rp, buffer, (int) length);
            //buffer.position(buffer.position() + (int) length);
        }
        catch (Exception e)
        {
            RadiusLog.error(e.getMessage(), e);
        }
        return rp;
    }
    
    /**
     * Parse multiple RadiusPackets from a data stream
     * @param in The input data stream
     * @param packetCount Number of packets to expect
     * @return Returns an array of RadiusPackets
     * @throws RadiusException
    public static RadiusPacket[] parse(InputStream in, int packetCount) throws RadiusException
    {
        RadiusPacket rp[] = new RadiusPacket[packetCount];
        try
        {
            for (int i=0; i < packetCount; i++)
            {
            	rp[i] = parsePacket(in);
            }
        }
        catch (IOException e)
        {
            RadiusLog.error(e.getMessage(), e);
        }
        return rp;
    }
     */


	public static RadiusPacket[] parse(ByteBuffer buffer, int packetCount)
	{
        RadiusPacket rp[] = new RadiusPacket[packetCount];
        try
        {
            for (int i=0; i < packetCount; i++)
            {
            	rp[i] = parsePacket(buffer);
            }
        }
        catch (RadiusException e)
        {
            RadiusLog.error(e.getMessage(), e);
        }
        return rp;
	}

    public static void poolStatus()
    {
		if (pktObjectPool == null) return;
		System.err.println("PacketPool: active="+pktObjectPool.getNumActive()+" idle="+pktObjectPool.getNumIdle());
    }
    
	public static void recycle(RadiusPacket p) 
	{
		AttributeList list = p.getAttributes();
		list.clear();
		
		if (pktObjectPool != null && p.recyclable)
		{
			try
			{
				pktObjectPool.returnObject(new Integer(p.getCode()), p);
				// System.err.print("Recycled packet "+p.toString());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		// poolStatus();
		// AttributeFactory.poolStatus();
	}

	public static void recycle(RadiusPacket[] rp) 
	{
		if (rp != null)
		{
			for (RadiusPacket p : rp)
			{
				recycle(p);
			}
		}
	}
}
