/**
 * JRadius - A Radius Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package net.jradius.webservice;

import java.util.LinkedHashMap;
import java.util.Map;

import net.jradius.server.JRadiusEvent;

/**
 * @author David Bird
 */
public class WebServiceResponse extends JRadiusEvent
{
    private static final long serialVersionUID = 0L;
    private byte[] content;
    private int type;

    private LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();

    public int getType()
    {
        return type;
    }
    
    public String getTypeString()
    {
        return "ws";
    }
    
    /**
     * @return Returns the content.
     */
    public byte[] getContent()
    {
        if (content == null) return "<NOOP/>".getBytes();
        return content;
    }
    
    /**
     * @param content The content to set.
     */
    public void setContent(byte[] content)
    {
        this.content = content;
    }
    
    public Map<String, String> getHeaders()
    {
        return this.headers;
    }
}
