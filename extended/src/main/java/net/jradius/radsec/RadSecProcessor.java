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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.jradius.dictionary.Attr_SharedSecret;
import net.jradius.exception.RadiusException;
import net.jradius.handler.chain.JRCommand;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;
import net.jradius.server.ListenerRequest;
import net.jradius.server.RadiusProcessor;
import net.jradius.server.config.Configuration;
import net.jradius.util.MessageAuthenticator;


/**
 * RadSec Request Processor
 * 
 * @author David Bird
 */
public class RadSecProcessor extends RadiusProcessor
{
    protected void processRequest(ListenerRequest listenerRequest) throws Exception
    {
        RadSecRequest request = (RadSecRequest) listenerRequest.getRequestEvent();
        
        try
        {
            request.setApplicationContext(getApplicationContext());
            request.setReturnValue(runPacketHandlers(request));
        }
        catch (Throwable th)
        {
            request.setReturnValue(JRadiusServer.RLM_MODULE_FAIL);
            RadiusLog.error(">>> processRequest(): Error during processing RunPacketHandlers block", th);
        }

        try
        {
        	OutputStream out = listenerRequest.getOutputStream();
        	synchronized (out)
        	{
                this.writeResponse(request, request.buffer_out, out);
			}
        }
        catch(Throwable e)
        {
            RadiusLog.error(">>> processRequest(): Error during writing response", e);
        }
    }

    public void writeResponse(JRadiusRequest request, ByteBuffer buffer, OutputStream outputStream) throws IOException, RadiusException 
    {
        if (Configuration.isDebug()) 
            request.printDebugInfo();

        RadiusPacket[] rp = request.getPackets();

        RadiusRequest req = (RadiusRequest) rp[0];
        RadiusResponse res = (RadiusResponse) rp[1];
        
        String sharedSecret = (String) req.getAttributeValue(Attr_SharedSecret.TYPE);
        
        RadiusFormat format = RadiusFormat.getInstance();

		MessageAuthenticator.generateResponseMessageAuthenticator(req, res, sharedSecret);
		res.generateAuthenticator(req.getAuthenticator(), sharedSecret);

		buffer.clear();
		format.packPacket(res, sharedSecret, buffer, true);
        
        outputStream.write(buffer.array(), 0, buffer.position());
        outputStream.flush();
    }

    protected void logReturnCode(int result, JRCommand handler)
    {
        switch (result)
        {
            case JRadiusServer.RLM_MODULE_INVALID:
            case JRadiusServer.RLM_MODULE_NOTFOUND:
            case JRadiusServer.RLM_MODULE_FAIL:
                RadiusLog.error("Error: Packet handler returned " + JRadiusServer.resultCodeToString(result)
                        + ". Stopped handling this packet.");
                break;
            case JRadiusServer.RLM_MODULE_HANDLED:
            case JRadiusServer.RLM_MODULE_REJECT:
                RadiusLog.info("Packet handler returned " + JRadiusServer.resultCodeToString(result)
                        + ". Stopped handling this packet.");
                break;
            case JRadiusServer.RLM_MODULE_OK:
            case JRadiusServer.RLM_MODULE_NOOP:
            case JRadiusServer.RLM_MODULE_UPDATED:
            case JRadiusServer.RLM_MODULE_NUMCODES:
            case JRadiusServer.RLM_MODULE_USERLOCK:
            default:
                RadiusLog.debug("Packet handler " + handler.getName() + " returned "
                        + JRadiusServer.resultCodeToString(result) + ". Continue handling this packet.");
        }
    }
}
