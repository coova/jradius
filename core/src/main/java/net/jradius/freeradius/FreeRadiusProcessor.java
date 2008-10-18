/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.jradius.exception.RadiusException;
import net.jradius.handler.chain.JRCommand;
import net.jradius.log.RadiusLog;
import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;
import net.jradius.server.ListenerRequest;
import net.jradius.server.RadiusProcessor;
import net.jradius.server.config.Configuration;


/**
 * FreeRADIUS Request Processor
 * 
 * @author Gert Jan Verhoog
 * @author David Bird
 */
public class FreeRadiusProcessor extends RadiusProcessor
{
    private static final FreeRadiusFormat format = new FreeRadiusFormat();

    protected void processRequest(ListenerRequest listenerRequest) throws IOException, RadiusException
    {
        FreeRadiusRequest request = (FreeRadiusRequest)listenerRequest.getRequestEvent();
        try
        {
            request.setApplicationContext(getApplicationContext());
            request.setReturnValue(runPacketHandlers(request));
        }
        catch (Throwable th)
        {
            request.setReturnValue(JRadiusServer.RLM_MODULE_FAIL);
            th.printStackTrace();
        }
        writeResponse(request, listenerRequest.getOutputStream());
    }

    public void writeResponse(JRadiusRequest request, OutputStream outputStream) throws IOException, RadiusException 
    {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outBytes);

        if (Configuration.isDebug()) 
            request.printDebugInfo(System.out);

        RadiusPacket[] rp = request.getPackets();
        int packetCount = rp.length;
        
        RadiusFormat.writeUnsignedByte(out, request.getReturnValue());
        RadiusFormat.writeUnsignedByte(out, packetCount);
        
        for (int i=0; i < rp.length; i++)
        {
        	out.write(format.packPacket(rp[i], null));
        }
        
        byte[] bConfigItems = format.packAttributeList(request.getConfigItems());
        RadiusFormat.writeUnsignedInt(out, bConfigItems.length);
        out.write(bConfigItems);
        out.close();
        outputStream.write(outBytes.toByteArray());
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
