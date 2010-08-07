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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import net.jradius.exception.RadiusException;
import net.jradius.handler.chain.JRCommand;
import net.jradius.log.RadiusLog;
import net.jradius.server.ListenerRequest;
import net.jradius.server.Processor;
import net.jradius.server.TCPListenerRequest;

/**
 * JRadius Relay Request Processor
 * 
 * @author David Bird
 */
public class WebServiceProcessor extends Processor
{
	protected static final byte[] newline = toHTTPBytes("\r\n");
	protected static final byte[] ctype = toHTTPBytes("Content-Type: text/xml\r\n");
	protected static final byte[] clength = toHTTPBytes("Content-Length: ");
	protected static final byte[] server = toHTTPBytes("Server: JRadius\r\n");  
	protected static final byte[] conclose = toHTTPBytes("Connection: close\r\n");
	protected static final byte[] ok = toHTTPBytes(" 200 OK\r\n");
	protected static final byte[] found = toHTTPBytes(" 302 Found\r\n");
	protected static final byte[] unauthorized = toHTTPBytes(" 401 Unauthorized\r\n");
	private boolean wantClientCertificates = true;
    
	protected void processRequest(ListenerRequest listenerRequest) throws Exception
	{
		TCPListenerRequest tcpListenerRequest = (TCPListenerRequest)listenerRequest;
		Socket socket = tcpListenerRequest.getSocket();
		socket.setSoTimeout(15000); // 15 second read timeout
        
		X509Certificate x509 = null;
        
        if (socket instanceof SSLSocket && wantClientCertificates)
        {
        	SSLSocket sslSocket = (SSLSocket) socket;
        	sslSocket.setWantClientAuth(true);
        	SSLSession sslSession = sslSocket.getSession();
        	try
        	{
	        	Certificate[] certs = sslSession.getPeerCertificates();
				if (certs != null)
				{
					Certificate cert = certs[0];
					if (cert instanceof X509Certificate)
						x509 = (X509Certificate) cert;
				}
			} 
			catch (Exception e) 
			{
			}
        }
        
        WebServiceRequest request = null;
        OutputStream os = null; 

        try
        {
            request = (WebServiceRequest) listenerRequest.getRequestEvent();
            request.setServerVariableMap(listenerRequest.getServerVariables());
            request.setCertificate(x509);
            request.setApplicationContext(getApplicationContext());
            processRequest(request);

            os = socket.getOutputStream();
            sendResponse(request, os);
        } 
        finally
        {
            if (os != null)
            {
                os.flush();
            }

            if (!tcpListenerRequest.isKeepAlive())
            {
	            if (os != null)
	            {
	                os.close();
	            }
	            socket.close();
            }
        }
    }
 
    protected void runHandlers(WebServiceRequest request)
    {
        RadiusLog.debug("Processing WebServiceRequest: " + request.toString());
        List<JRCommand> handlers = getRequestHandlers();
        if (handlers == null) return;

        for (JRCommand handler : handlers)
        {
            boolean stop = false;
            try
            {
                if (handler.doesHandle(request))
                {
                    stop = handler.execute(request);
                    if (stop) break;
                }
            }
            catch (WebServiceException e)
            {
                RadiusLog.error(e.getMessage(), e);
                break;
            }
            catch (RadiusException e)
            {
                RadiusLog.error(e.getMessage(), e);
                break;
            }
            catch (Throwable e)
            {
                RadiusLog.error(e.getMessage(), e);
                break;
            }
        }
    }
    
    protected void processRequest(WebServiceRequest request)
    {
    	runHandlers(request);
    }

    private void sendResponse(WebServiceRequest request, OutputStream out) throws IOException
    {
        WebServiceResponse response = request.getResponse();
        
        if (response == null)
        {
            RadiusLog.error("No response found for WebServiceRequest: " + request.toString());
            writeBadRequest(out, request.getHttpVersion());
            return;
        }

        writeResponse(out, request.getHttpVersion(), response.getHeaders(), response.getContent());
    }
    
    private void writeResponse(OutputStream writer, String httpVersion, Map headers, byte[] payload) throws IOException
    {
        boolean wroteCT = false;
        boolean wroteCL = false;
        
        writer.write(toHTTPBytes(httpVersion));

        if (headers.get("Location") != null)
            writer.write(found);
        else if (headers.get("WWW-Authenticate") != null)
        	writer.write(unauthorized);
        else
            writer.write(ok);
        
        writer.write(server);
        writer.write(conclose);
        for (Iterator i = headers.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry)i.next();
            String key = (String)entry.getKey();
            writer.write(toHTTPBytes(key));
            writer.write(toHTTPBytes(": "));
            writer.write(toHTTPBytes((String)entry.getValue()));
            writer.write(newline);
            if (key.equalsIgnoreCase("content-type")) wroteCT = true;
            else if (key.equalsIgnoreCase("content-length")) wroteCL = true;
        }
        if (!wroteCT) writer.write(ctype);
        if (!wroteCL)
        {
            writer.write(clength);
            writer.write(toHTTPBytes(Integer.toString(payload.length)));
            writer.write(newline);
        }
        writer.write(newline);
        writer.write(payload);
    }

    private void writeBadRequest(OutputStream writer, String httpVersion) throws IOException
    {
        writer.write(toHTTPBytes(httpVersion));
        writer.write(toHTTPBytes(" 400 Bad Request"));
        writer.write(newline);
        writer.write(server);
        writer.write(newline);
        writer.write(toHTTPBytes("Invalid request"));
    }
    
    protected static byte[] toHTTPBytes(String text)
    {
    	if (text == null) text = "";
    	
        try
        {
            return text.getBytes("US-ASCII");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new Error(e.getMessage());
        }
    }
}
