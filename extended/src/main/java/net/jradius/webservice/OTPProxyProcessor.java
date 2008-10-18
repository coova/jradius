/**
 * JRadius - A Radius Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import net.jradius.exception.RadiusException;
import net.jradius.realm.JRadiusRealm;
import net.jradius.realm.JRadiusRealmManager;
import net.jradius.server.ListenerRequest;
import net.jradius.server.Processor;
import net.jradius.server.TCPListenerRequest;

/**
 * @author David Bird
 */
public class OTPProxyProcessor extends Processor 
{
    protected void processRequest(ListenerRequest listenerRequest) throws IOException, RadiusException
    {
        Socket socket = ((TCPListenerRequest)listenerRequest).getSocket();
        try
        {
            WebServiceListener wsListener = (WebServiceListener)listenerRequest.getListener();
            socket.setSoTimeout(7000); // 7 second read timeout
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String userName = reader.readLine();

            String realmName = realmFromUserName(userName);
            if (realmName == null) error(writer,"No realm given");

            JRadiusRealm realm = JRadiusRealmManager.get(realmName);
            if (realm == null) error(writer, "Unrecognized realm: " + realmName);

            OTPProxyRequest request = new OTPProxyRequest(wsListener, userName, realm, socket, reader, writer);
          
            request.start();
            wsListener.put(request);
        }
        catch (Exception e)
        {
            socket.close();
            throw new RadiusException(e);
        }
    }

    protected void error(BufferedWriter writer, String e) throws IOException, RadiusException
    {
        writer.write("error:"+e+"\n");
        writer.flush();

        throw new OTPProxyException(e);
    }
    
    protected String realmFromUserName(String username) throws OTPProxyException
    {
        int idx;
        
        if ((idx = username.indexOf("/")) > 0 ||
            (idx = username.indexOf("\\")) > 0)
        {
            // Prefix Realm - takes priority over Postfix
            return username.substring(0, idx);
        }
        
        if ((idx = username.indexOf("@")) > 0)
        {
            // Postfix Realm
            return username.substring(idx + 1);
        }
        
        return null;
    }
}
