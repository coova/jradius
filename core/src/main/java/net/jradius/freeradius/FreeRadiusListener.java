/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006 David Bird <david@coova.com>
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

package net.jradius.freeradius;

import java.io.InputStream;
import java.nio.ByteBuffer;

import net.jradius.exception.RadiusException;
import net.jradius.packet.PacketFactory;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.ListenerRequest;
import net.jradius.server.TCPListener;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;

/**
 * FreeRADIUS/rlm_jradius Listener
 *
 * @author David Bird
 * @author Gert Jan Verhoog
 */
public class FreeRadiusListener extends TCPListener
{
    private static final FreeRadiusFormat format = new FreeRadiusFormat();
    
    private ObjectPool requestObjectPool = new SoftReferenceObjectPool(new PoolableObjectFactory() 
    {
		public boolean validateObject(Object arg0) {
			return true;
		}
		
		public void passivateObject(Object arg0) throws Exception {
		}
		
		public Object makeObject() throws Exception {
			return new FreeRadiusRequest();
		}
		
		public void destroyObject(Object arg0) throws Exception {
		}
		
		public void activateObject(Object arg0) throws Exception {
		}
	});
    

    public JRadiusEvent parseRequest(ListenerRequest listenerRequest, ByteBuffer notUsed, InputStream in) throws Exception 
    {
    	FreeRadiusRequest request = (FreeRadiusRequest) requestObjectPool.borrowObject();
    	request.setBorrowedFromPool(requestObjectPool);

        int totalLength  = (int) (RadiusFormat.readUnsignedInt(in) - 4);
        int readOffset = 0;

        ByteBuffer buffer = request.buffer_in;
        
        if (totalLength < 0 || totalLength > buffer.capacity()) 
        {
        	return null;
        }
        
        buffer.clear();
        byte[] payload = buffer.array();
        
        while (readOffset < totalLength)
        {
        	int result = in.read(payload, readOffset, totalLength - readOffset);
        	if (result < 0) return null;
        	readOffset += result;
        }
        
        buffer.limit(totalLength);

        long nameLength = RadiusFormat.getUnsignedInt(buffer);

        if (nameLength < 0 || nameLength > 1024) 
        {
            throw new RadiusException("KeepAlive rlm_jradius connection has been closed");
        }
        
        byte[] nameBytes = new byte[(int) nameLength];
        buffer.get(nameBytes);
        
        int messageType = RadiusFormat.getUnsignedByte(buffer);
        int packetCount = RadiusFormat.getUnsignedByte(buffer);

        RadiusPacket rp[] = PacketFactory.parse(buffer, packetCount);
        
        long length  = RadiusFormat.getUnsignedInt(buffer);

        if (length > buffer.remaining())
        {
        	throw new RadiusException("bad length");
        }
        
        AttributeList configItems = new AttributeList();
        format.unpackAttributes(configItems, buffer, (int) length, true);
        
        request.setConfigItems(configItems);
        request.setSender(new String(nameBytes));
        request.setType(messageType);
        request.setPackets(rp);

        return request;
    }
}
