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

package net.jradius.server;

import java.io.StringWriter;
import java.io.PrintWriter;

import net.jradius.exception.RadiusException;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.server.config.Configuration;
import net.jradius.session.JRadiusSession;
import net.jradius.log.RadiusLog;

/**
 * An abstract class representing a JRadius Server Request.
 * 
 * @author David Bird
 * @author Gert Jan Verhoog
 */
public abstract class JRadiusRequest extends JRadiusEvent
{
    private JRadiusSession session;

    /**
     * @return Returns the return value of the JRadiusRequest
     */
    public abstract int getReturnValue();

    /**
     * @param returnValue The new return value to set
     */
    public abstract void setReturnValue(int returnValue);

    /**
     * @return Returns the RADIUS Server "Configuration Items" as AttributeList
     */
    public abstract AttributeList getConfigItems();
    
    /**
     * @return Returns an array of the RadiusPackets received
     */
    public abstract RadiusPacket[] getPackets();
    
    /**
     * @param configItems The new "Configuration Items" to set in the RADIUS Server
     */
    public abstract void setConfigItems(AttributeList configItems);
    
    /**
     * @param packets The RadiusPacket array to return to the RADIUS Server
     */
    public abstract void setPackets(RadiusPacket[] packets);
    
    /**
     * Get the RadiusSession assinged to this JRadiusRequest
     * @return Returns the session.
     */
    public JRadiusSession getSession()
    {
        return session;
    }
    
    /**
     * Assign a RadiusSession to a JRadiusRequest
     * @param session The session to set.
     */
    public void setSession(JRadiusSession session)
    {
        this.session = session;
    }

    /**
     * @return Returns the sessionKey, if one exists
     */
    public String getSessionKey()
    {
        if (session != null) return session.getSessionKey();
        return null;
    }
    
    /**
     * Convenience method, returns the Request RadiusPacket of a
     * JRadiusRequest. This is the first packet in the packet array.
     * @return a RadiusPacket containing the radius request
     * @throws RadiusException
     */
    public RadiusPacket getRequestPacket() throws RadiusException
    {
        RadiusPacket p[] = getPackets();
        if (p.length == 0)
        {
            throw new RuntimeException("No Request packet in JRadiusRequest");
        }
        return p[0];
    }
    
    /**
     * Convenience method, returns the Reply RadiusPacket of a
     * JRadiusRequest. This is the second packet in the packet array.
     * Use hasReplyPacket() to ensure there is a reply packet in the JRadiusRequest.
     * 
     * @return RadiusPacket containing the radius reply
     * @throws RadiusException
     */
    public RadiusPacket getReplyPacket() throws RadiusException
    {
        RadiusPacket p[] = getPackets();
        if (p.length < 2)
        {
            throw new RadiusException("No Reply packet in JRadiusRequest");
        }
        return p[1];
    }

    public void setReplyPacket(RadiusPacket np)
    {
        RadiusPacket p[] = getPackets();
        p[1] = np;
    }

    /**
     * @return True if the JRadiusRequest has a reply packet
     */
    public boolean hasReplyPacket()
    {
        return getPackets().length == 2;
    }
    
    /**
     * @return Returns true if the request is an Accounting-Request
     */
    public boolean isAccountingRequest()
    {
        try
        {
            return (getRequestPacket() instanceof AccountingRequest);
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    /**
     * Add an attribute to the reply by adding to the reply packet, if one,
     * or the configItems.
     * @param a The RadiusAttribute
     */
    public void addReplyAttribute(RadiusAttribute a)
    {
        if (hasReplyPacket()) try { getReplyPacket().addAttribute(a); } catch (RadiusException e) {}
        else getConfigItems().add(a);
    }
    
    public void printDebugInfo()
    {
        if (Configuration.isDebug())
        {
            RadiusPacket[] rp = this.getPackets();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            // debug info:
            pw.println(">>> packets in request from \"" + getSender() + "\":");

            for (int i=0; i < rp.length; i++)
                if (rp[i] != null)
                {
                    pw.println("--- packet " + (i+1) + " of " + rp.length);
                    pw.println(rp[i].toString());
                }

            pw.println("Configuration Items:");
            pw.println(getConfigItems().toString());

            pw.flush();
            RadiusLog.debug(sw.toString());
        }
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer("{");
        sb.append(" requester = ").append(getSender());
        sb.append(", type = ").append(getTypeString());
        sb.append(" }");
        return sb.toString();
    }
}
