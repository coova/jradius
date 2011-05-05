/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2007-2008 David Bird <david@coova.com>
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class WISPrClient
{
	private String DEFAULT_USER_AGENT = "WISPrClient [jradius.net] ("+System.getProperty("java.verrsion")+")";
	private String userAgent;
	
    private String prefixRealm = null;
    private String username = null;
    private String realm = null;
    private String password = null;
    
    private String WISPrLogin = null;
    private String WISPrAbortLogin = null;
    private String WISPrLogoff = null;
    private String WISPrLocationName = null;
    private String[] WISPrXML = new String[2];
    private String cookie = null;
    private String loginResult;
    private String loginResultsUrl;
    private String replyMessage;
    
    private boolean secureRoaming = false;
    private String otpProxyServer = "ap.coova.org";
    private String otpProxyPort = "1810";
    private boolean otpUseSSL = true;
    private boolean otpTrustAnyCert = true;
    private String otpCertChain = null;
    
    private boolean isJavaReady = true;
    private boolean isReady = false;
    private boolean isOnline = false;
    
    public String publicURL = "http://www.microsoft.com/en/us/default.aspx";
    public String titleMatch = "Microsoft Corporation";
    
    public String welcomeURL  =  "Success!";
    public String onlineURL   =  "Online";
    public String loginURL    =  "";
    public String noWISPrURL  =  "";
    public String badWISPrURL =  "";

    private StringBuffer debugString = new StringBuffer();
    private Boolean isCommunicator;
    private Boolean haveJavaSecurity = null;
    private SecurityManager securityManager;
    private String ENCODING = "utf-8";

    private String status = "Loading...";
    
    private Thread messageThread;
    MulticastSocket socket;
    InetAddress group;
    
    private String copy = 
        " Running JRadius WiFi Client\n" + 
        " Copyright (c) 2007-2008 David Bird <david@coova.com>\n"+
        " Copyright (c) 2005-2006 PicoPoint B.V.\n"+
        " All Rights Reserved.\n";

    public void login(String username, String password)
    {
        if (username != null) setUsername(username);
        if (password != null) setPassword(password);
        doWISPrLogin();
    }

    public void logoff()
    {
        doWISPrLogoff();
    }

    public void init()
    {
        securityManager = System.getSecurityManager();

        if (isCommunicator())
        {
            try
            {
            	@SuppressWarnings("rawtypes")
				Class privilegeManager = Class.forName("netscape.security.PrivilegeManager");
            	@SuppressWarnings("unchecked")
				Method enablePrivilege = privilegeManager.getMethod("enablePrivilege", new Class[] {String.class});
            	enablePrivilege.invoke(null, new Object[] { "UniversalXPConnect" });
            	enablePrivilege.invoke(null, new Object[] { "UniversalConnect" });
            }
            catch (Throwable e)
            { }
        }
        else
        {
            try
            {
            	@SuppressWarnings("rawtypes")
				Class policyEngine = Class.forName("com.ms.security.PolicyEngine");
            	@SuppressWarnings("rawtypes")
            	Class permissionID = Class.forName("com.ms.security.PermissionID");
				@SuppressWarnings("unchecked")
				Method assertPermission = policyEngine.getMethod("assertPermission", new Class[] {permissionID});
				Field permId = permissionID.getField("NETIO");
				assertPermission.invoke(null, new Object[] { permId.get(null) });
            }
            catch (Throwable e)
            { }
        }

        debugWrite("Version Info:\n"
                + System.getProperty("java.vendor") + " "
                + System.getProperty("java.version") + " running on "
                + System.getProperty("os.name") + " " 
                + System.getProperty("os.version") + " " 
                + System.getProperty("os.arch"));

        try
        {
            group = InetAddress.getByName("224.0.0.1");
            socket = new MulticastSocket(40401);
            socket.joinGroup(group);

            messageThread = new Thread(new Runnable() 
            {
                boolean running = true;
                public void run()
                {
                    DatagramPacket packet;
                    while(running) 
                    {
                        byte[] buf = new byte[256];
                        packet = new DatagramPacket(buf, buf.length);
                        try
                        {
                            socket.receive(packet);
                            String received = new String(packet.getData());
                            debugWrite("recieved: "+received);
                        }
                        catch(Exception e)
                        {
                            debugWrite("recieved: "+e.getMessage());
                            running = false;
                        }
                    }
                }
            });
            messageThread.start();
        }
        catch (Exception e)
        {
            debugWrite(e.getMessage());
        }
        
        findWISPrLogin(publicURL);
   }

    public void shutdown()
    {
        try
        {
            socket.leaveGroup(group);
            socket.close();
            messageThread.interrupt();
        }
        catch (Exception e)
        {
            debugWrite(e.getMessage());
        }
    }
    
    public void broadcastMessage(String message)
    {
        try
        {
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName("224.0.0.1");
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, 40401);
            socket.send(packet);
            socket.close();
        }
        catch (Exception e)
        {
            debugWrite(e.getMessage());
        }
    }
    
    private StringBuffer sb;
    private void debugWrite(String msg)
    {
        if (sb == null) sb = new StringBuffer();
        sb.append(msg);
    }

    public String getDebug()
    {
        String s = sb.toString();
        sb = null;
        return s;
    }
    
    private void redirect(String page)
    {
        status = page;
    }

    private void setStatus(String s)
    {
        status = s;
    }

    public void checkStatus()
    {
        findWISPrLogin(publicURL);
    }
    
    public void findWISPrLogin(String url)
    {
        PageResult result = getPage(url, cookie);

        if (result == null)
            return;

        if (result.getIsXML()==0)
        {
            String title = getXMLParam(result.getContent(), null, "title");
            if (title != null && titleMatch.equals(title))
            {
                isOnline = true;
                redirect(onlineURL);
            }
            else
            {
                //redirect(noWISPrURL);
            }
        }
        else
        {
            String xml = WISPrXML[0] = result.getContent();
            debugWrite("Extracted WISPr XML:\n" + xml);

            String xmll = xml.toLowerCase();

            String responseCode = getXMLParam(xml, xmll, "responsecode");
            if (responseCode == null) { redirect(badWISPrURL); return; }
            
            if ("200".equals(responseCode)) // Proxy Detection
            {
                String nextUrl = getXMLParam(xml, xmll, "nexturl");
                if (nextUrl != null && !"".equals(nextUrl))
                {
                    findWISPrLogin(nextUrl);
                }
                else
                {
                    redirect(badWISPrURL);
                }
            }
            else if ("201".equals(responseCode)) // Authentication Pending
            {
                String loginResultsURL = getXMLParam(xml, xmll, "loginresultsurl");
                if (loginResultsURL != null && !"".equals(loginResultsURL))
                {
                    findWISPrLogin(loginResultsURL);
                }
                else
                {
                    redirect(badWISPrURL);
                }
            }
            else
            {
                WISPrLogin = getXMLParam(xml, xmll, "loginurl");
                WISPrAbortLogin = getXMLParam(xml, xmll, "abortloginurl");
                WISPrLocationName = getXMLParam(xml, xmll, "locationname");
                debugWrite("Extracted login URL from WISPr XML:\n" + WISPrLogin);
                debugWrite("Extracted abort URL from WISPr XML:\n" + WISPrAbortLogin);
                String msg = "Found WiFi Network";
                if (WISPrLogin != null)
                {
                    isReady = true;
                }
                if (WISPrLocationName != null)
                {
                    status = WISPrLocationName.replaceAll("_", " ");
                }
                else
                {
                    status = msg;
                }
            }
        }
    }
    
    private void doWISPrLogin()
    {
        StringBuffer responseHtml;

        try
        {
            if (WISPrLogin == null)
            {
                return;
            }

            if (username.length() < 1)
            {
                throw new RuntimeException("Please enter username");
            }

            if (password.length() < 1)
            {
                throw new RuntimeException("Please enter username");
            }
            
            String wisprUsername = username;
            String wisprPassword = password;

            if (secureRoaming)
            {
                try
                {
                    ControlThread control = new ControlThread(username);
                    wisprUsername = control.getOtpUsername();
                    wisprPassword = control.getOtpPassword();
                    control.start();
                    
                    System.out.println("Using OTP " + wisprUsername + "/" + wisprPassword);
                }
                catch(IOException ioe)
                {
                    System.err.println("Unable to secure username and password!");
                }
            }

            StringBuffer loginUrl = new StringBuffer(WISPrLogin);

            if (WISPrLogin.indexOf("?") > 0)
            {
                loginUrl.append("&");
            }
            else
            {
                loginUrl.append("?");
            }

            if (prefixRealm != null && prefixRealm.length() > 0)
            {
                wisprUsername = prefixRealm + "/" + wisprUsername;
            }
            
            loginUrl.append("UserName=").append(urlEncode(wisprUsername));
            loginUrl.append("&Password=").append(urlEncode(wisprPassword));

            String providerUrl = loginUrl.toString();

            debugWrite("Login using url " + providerUrl);

            redirect(processWISPrResponse(providerUrl));
        }
        catch (Exception rte)
        {
            debugWrite("Runtime Exception: " + rte.getMessage());
        }
    }

    private String processWISPrResponse(String url)
    {
        PageResult result = getPage(url, cookie);

        if (result.getIsXML()==0)
        {
            debugWrite("Did not get WISPr XML in: " + result.getContent());
            return badWISPrURL;
        }

        String xml = WISPrXML[1] = result.getContent();
        
        debugWrite("Extracted WISPr XML:\n" + xml);
        
        String xmll = xml.toLowerCase();
        loginResult = getXMLParam(xml, xmll, "responsecode");
        loginResultsUrl = getXMLParam(xml, xmll, "loginresultsurl");
        replyMessage = getXMLParam(xml, xmll, "replymessage");
        WISPrLogoff = getXMLParam(xml, xmll, "logoffurl");

        if (loginResult == null)
        {
            return badWISPrURL;
        }

        if ("50".equals(loginResult))
        {
            isOnline = true;
            if (replyMessage == null)
            {
                status = "Logged in";
            }
            else
            {
                status = replyMessage;
            }
            debugWrite("Successful login: Redirecting user to " + welcomeURL);
            return welcomeURL;
        }

        if ("201".equals(loginResult))
        {
            if (replyMessage == null)
            {
                status = "Logging in...";
            }
            else
            {
                status = replyMessage;
            }
            debugWrite("Login pending: resultsUrl " + loginResultsUrl);
            return processWISPrResponse(loginResultsUrl);
        }
        
        if (replyMessage == null)
        {
            status = "Login failed";
        }
        else
        {
            status = replyMessage;
        }
        return loginURL;
    }
    
    private class ControlThread extends Thread
    {
        private final Socket socket;
        private final BufferedWriter writer;
        private final BufferedReader reader;
        private String otpUsername;
        private String otpPassword;

        public ControlThread(String username) throws IOException, NoSuchAlgorithmException, KeyManagementException
        {
            SocketFactory fact = null;
            if (otpUseSSL)
            {
                SSLContext sslContext = SSLContext.getInstance("SSLv3");
                sslContext.init(null, new X509TrustManager[]{ new X509TrustManager()
                    {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                    public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }}, null);
                
                fact = sslContext.getSocketFactory();
            }
            else
            {
                fact = SocketFactory.getDefault();
            }

            final SocketFactory factory = fact;
            
            if (haveJavaSecurity())
            {
                socket = (Socket) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction()
                {
                    public Object run()
                    {
                        try
                        {
                            return factory.createSocket(InetAddress.getByName(otpProxyServer), Integer.parseInt(otpProxyPort));
                        }
                        catch(IOException e)
                        {
                            return null;
                        }
                    }
                });
                if (socket == null) throw new IOException("could not connect to host " + otpProxyServer);
            }
            else socket = factory.createSocket(InetAddress.getByName(otpProxyServer), Integer.parseInt(otpProxyPort));
            
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(username);
            writer.write("\n");
            writer.flush();

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            otpUsername = reader.readLine();
            otpPassword = reader.readLine();
        }

        /**
         * @return Returns the otpPassword.
         */
        public String getOtpPassword()
        {
            return otpPassword;
        }

        /**
         * @return Returns the otpUsername.
         */
        public String getOtpUsername()
        {
            return otpUsername;
        }

        public void run()
        {
            try
            {
                if (getUsername().startsWith("error:"))
                {
                    status = getUsername().replaceFirst("error:", "");
                }
                else
                {
                    doEAP(socket, reader, writer);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        private void doEAP(Socket socket, BufferedReader reader, BufferedWriter writer) throws Exception
        {
            // We are just going to do EAP-MD5 for now...
            writeBytes(writer, eapResponse(EAP_IDENTITY, (byte) 0, getUsername().getBytes()));
            byte[] reply = readBytes(reader);

            while (reply != null)
            {
                byte[] send = doEAP(reply);
                writeBytes(writer, send);
                reply = readBytes(reader);
            }
        }

        private byte[] doEAP(byte[] d) throws Exception
        {
            ByteBuffer bb = ByteBuffer.wrap(d);

            byte rtype = bb.get();
            byte id = bb.get();
            short dlen = bb.getShort();

            if (rtype != EAP_REQUEST)
            {
                throw new Exception("Expecting an EAP-Request.. got code: " + rtype);
            }

            byte eapcode = 0;
            byte[] data = null;

            if (dlen > EAP_HEADERLEN)
            {
                eapcode = bb.get();
                dlen = (short) (dlen - EAP_HEADERLEN - 1);

                if (dlen > 0)
                {
                    data = new byte[dlen];
                    bb.get(data);
                }
            }

            if (eapcode == EAP_IDENTITY)
            {
                return eapResponse(EAP_IDENTITY, id, getUsername().getBytes());
            }

            if (eapcode != EAP_MD5)
            {
                return eapResponse(EAP_NAK, id, new byte[] { EAP_MD5 });
            }

            return eapResponse(EAP_MD5, id, doEAPMD5(id, data));
        }

        public byte[] doEAPMD5(byte id, byte[] data) throws Exception
        {
            byte md5len = data[0];
            byte[] md5data = new byte[md5len];
            System.arraycopy(data, 1, md5data, 0, md5len);

            byte[] Response = new byte[17];
            Response[0] = 16;
            System.arraycopy(chapMD5(id, getPassword().getBytes(), md5data), 0, Response, 1, 16);

            return Response;
        }

        protected byte[] eapResponse(int type, byte id, byte[] data)
        {
            short length = (short) (1 + EAP_HEADERLEN);
            if (data != null) length = (short) (length + data.length);
            byte[] Response = new byte[length];
            Response[0] = EAP_RESPONSE;
            Response[1] = id;
            Response[2] = (byte) (length >> 8 & 0xFF);
            Response[3] = (byte) (length & 0xFF);
            Response[4] = (byte) (type & 0xFF);
            if (data != null) System.arraycopy(data, 0, Response, 1 + EAP_HEADERLEN, data.length);
            return Response;
        }

        private void writeBytes(BufferedWriter writer, byte[] d) throws IOException
        {
            String s = Base64.encodeBytes(d, Base64.DONT_BREAK_LINES);
            debugWrite("Sending: " + s);
            writer.write("eap:");
            writer.write(s);
            writer.write("\n");
            writer.flush();
        }

        private byte[] readBytes(BufferedReader reader) throws IOException
        {
            String line = reader.readLine();
            if (line.startsWith("error:"))
            {
                status = line.substring(6);
                return null;
            }
            if (line.startsWith("eap:"))
            {
                String s = line.substring(4);
                debugWrite("Recv: " + s);
                return Base64.decode(s);
            }
            return null;
        }

        public byte[] chapMD5(byte id, byte[] Password, byte[] Challenge) throws Exception
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(id);
            md.update(Password, 0, Password.length);
            md.update(Challenge, 0, Challenge.length);
            return md.digest();
        }
    }

    private void doWISPrLogoff()
    {
        if (WISPrLogoff == null || WISPrLogoff.trim().length()==0)
            return;

        PageResult result = getPage(WISPrLogoff, cookie);
        
        if (result.getIsXML()==1)
            debugWrite("WISPr XML:\n" + result.getContent());
        else
            debugWrite("NO WISPr XML in:\n" + result.getContent());
        
        checkStatus();
    }

    private PageResult getPage(final String urlString, final String cookieString) throws RuntimeException
    {
        if (haveJavaSecurity())
        {
            PageResult result = (PageResult) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction()
            {
                public Object run()
                {
                    return doGetPage(urlString, cookieString);
                }
            });
            return result;
        }
        return doGetPage(urlString, cookieString);
    }

    public PageResult doGetPage(final String urlString, final String cookieString)
    {
        StringBuffer sb = new StringBuffer();
        HttpURLConnection conn = null;
        String location = null;

        try
        {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            if (conn instanceof HttpsURLConnection)
            {
                HttpsURLConnection sconn = (HttpsURLConnection)conn;

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new X509TrustManager[]{ new X509TrustManager()
                    {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                    public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }}, null);
                
                sconn.setSSLSocketFactory(sslContext.getSocketFactory());
                sconn.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) { return true; } 
                });
            }

            conn.setRequestProperty("User-Agent", getUserAgent());
            
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(7000);

            if (cookieString != null)
            {
                conn.setRequestProperty("Cookie", cookieString);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str;

            int n = 1;
            boolean done = false;
            while (!done)
            {
                String headerKey = conn.getHeaderFieldKey(n);
                String headerVal = conn.getHeaderField(n);
                if (headerKey != null || headerVal != null)
                {
                    if (headerKey.equals("Location"))
                    {
                        location = headerVal;
                    }
                    else if (headerKey.equals("Set-Cookie"))
                    {
                        cookie = headerVal;
                    }
                    System.out.println(headerKey + " = " + headerVal);
                }
                else
                {
                    done = true;
                }
                n++;
            }

            while ((str = in.readLine()) != null)
            {
                sb.append(str + "\n");
            }
            in.close();

            // Check for WISPr XML
            String wisprXml = getWISPrXML(sb.toString());
            
            if (wisprXml != null)
                return new PageResult(wisprXml,1);
            
            if (location != null)
            {
                conn.disconnect();
                return doGetPage(location, cookie);
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("getPage: " + e.getClass().getName() + ":" + e.getMessage() + "...");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("getPage: " + e.getClass().getName() + ":" + e.getMessage() + "...");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            isJavaReady = false;
        }
        finally
        {
            if (conn != null) conn.disconnect();
        }
        
        return new PageResult(sb.toString(), 0);
    }

    private static String getWISPrXML(String responseHtml) throws RuntimeException
    {
        String htmll = responseHtml.toLowerCase();
        int WISPrStartIndex = htmll.indexOf("<wispaccessgatewayparam");
        int WISPrEndIndex = htmll.indexOf("</wispaccessgatewayparam>") + 25;
        if (WISPrStartIndex > 0)
        {
            return responseHtml.substring(WISPrStartIndex, WISPrEndIndex);
        }
        return null;
    }

    private String getXMLParam(String xml, String xmll, String tag) throws RuntimeException
    {
        if (xmll == null) xmll = xml.toLowerCase();
        int startIndex = xmll.indexOf("<" + tag + ">");
        int endIndex = xmll.indexOf("</" + tag + ">");
        if (startIndex > 0)
        {
            startIndex += tag.length() + 2;
            String res = urlDecode(xml.substring(startIndex, endIndex));
            res = stringReplaceAll(res, "&amp;", "&");
            return res;
        }
        return null;
    }

    private String urlEncode(String s)
    {
        String res = s;
        try
        {
            res = URLEncoder.encode(s, ENCODING);
        }
        catch (NoSuchMethodError e)
        {
            res = URLEncoder.encode(s);
        }
        catch (Exception e)
        {
            res = URLEncoder.encode(s);
        }
        return res;
    }

    private String urlDecode(String s)
    {
        String res = s;
        try
        {
            res = URLDecoder.decode(s, ENCODING);
        }
        catch (NoSuchMethodError e)
        {
            res = URLDecoder.decode(s);
        }
        catch (Exception e)
        {
            res = URLDecoder.decode(s);
        }
        return res;
    }

    private String stringReplaceAll(String s, String find, String replace)
    {
        String result = null;
        try
        {
            result = s.replaceAll(find, replace);
        }
        catch (NoSuchMethodError e)
        {
            StringBuffer sb = new StringBuffer(s);
            int index = s.length();
            int offset = find.length();

            while ((index = s.lastIndexOf(find, index - 1)) > -1)
            {
                sb.replace(index, index + offset, replace);
            }

            result = sb.toString();
        }
        return result;
    }

    private boolean haveJavaSecurity()
    {
        if (haveJavaSecurity == null)
        {
            haveJavaSecurity = Boolean.FALSE;
            try
            {
                Class t = Class.forName("java.security.PrivilegedAction");
                haveJavaSecurity = Boolean.TRUE;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return haveJavaSecurity.booleanValue();
    }

    private boolean isCommunicator()
    {
        if (isCommunicator == null)
        {
            isCommunicator = Boolean.FALSE;
            try
            {
                Class t = Class.forName("netscape.security.UserDialogHelper");
                isCommunicator = Boolean.TRUE;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return isCommunicator.booleanValue();
    }

    public String getPassword()
    {
        return nonNullString(password);
    }

    public void setPassword(String password)
    {
        this.password = trimString(password);
    }

    public String getUsername()
    {
        return nonNullString(username);
    }

    public void setUsername(String username)
    {
        this.username = trimString(username);
    }

    public String getRealm()
    {
        return nonNullString(realm);
    }

    public void setRealm(String realm)
    {
        this.realm = trimString(realm);
    }

    public String getWISPrXML0()
    {
        return nonNullString(WISPrXML[0]);
    }

    public String getWISPrXML1()
    {
        return nonNullString(WISPrXML[1]);
    }

    public String getWISPrLocationName()
    {
        return nonNullString(WISPrLocationName);
    }

    public String getWISPrLogin()
    {
        return nonNullString(WISPrLogin);
    }

    public String getWISPrAbortLogin()
    {
        return nonNullString(WISPrAbortLogin);
    }

    public String getWISPrLogoff()
    {
        return nonNullString(WISPrLogoff);
    }

    public String getOtpCertChain()
    {
        return nonNullString(otpCertChain);
    }

    public void setOtpCertChain(String url)
    {
        otpCertChain = trimString(url);
    }

    public boolean getOtpTrustAnyCert()
    {
        return otpTrustAnyCert;
    }

    public void setOtpTrustAnyCert(boolean trustAnyCert)
    {
        otpTrustAnyCert = trustAnyCert;
    }

    public String getOtpProxyServer()
    {
        return nonNullString(otpProxyServer);
    }

    public void setOtpProxyServer(String server)
    {
        otpProxyServer = trimString(server);
    }

    public String getOtpProxyPort()
    {
        return otpProxyPort;
    }

    public void setOtpProxyPort(String port)
    {
        String s = trimString(port);
        if (s != null) otpProxyPort = s;
    }

    public boolean getOtpUseSSL()
    {
        return otpUseSSL;
    }

    public void setOtpUseSSL(boolean useSSL)
    {
        otpUseSSL = useSSL;
    }

    public boolean getSecureRoaming()
    {
        return secureRoaming;
    }

    public void setSecureRoaming(boolean secure)
    {
        secureRoaming = secure;
    }

    public String getPrefixRealm()
    {
        return nonNullString(prefixRealm);
    }

    public void setPrefixRealm(String prefixRealm)
    {
        this.prefixRealm = trimString(prefixRealm);
    }

    public String getLoginURL()
    {
        return nonNullString(loginURL);
    }

    public void setLoginURL(String url)
    {
        loginURL = trimString(url);
    }

    public String getNoWISPrURL()
    {
        return nonNullString(noWISPrURL);
    }

    public void setNoWISPrURL(String url)
    {
        noWISPrURL = trimString(url);
    }

    public String getOnlineURL()
    {
        return nonNullString(onlineURL);
    }

    public void setOnlineURL(String url)
    {
        onlineURL = trimString(url);
    }

    public String getPublicURL()
    {
        return nonNullString(publicURL);
    }

    public void setPublicURL(String url)
    {
        publicURL = trimString(url);
    }

    public String getWelcomeURL()
    {
        return nonNullString(welcomeURL);
    }

    public void setWelcomeURL(String url)
    {
        welcomeURL = trimString(url);
    }

    public String getDebugString()
    {
        return debugString.toString();
    }

    public void clearDebugString()
    {
        debugString = new StringBuffer();
    }

    private static String nonNullString(String s)
    {
        if (s == null) return "";
        s = s.trim();
        return s;
    }
    
    private static String trimString(String s)
    {
        if (s == null) return null;
        s = s.trim();
        if (s.length() == 0) return null;
        return s;
    }
    
    public boolean isOnline()
    {
        return isOnline;
    }

    public boolean isReady()
    {
        return isReady;
    }

    public boolean isJavaReady()
    {
        return isJavaReady;
    }

    public static final int EAP_HEADERLEN = 4;
    public static final int EAP_REQUEST = 1;
    public static final int EAP_RESPONSE = 2;
    public static final int EAP_SUCCESS = 3;
    public static final int EAP_FAILURE = 4;
    public static final int EAP_IDENTITY = 1;
    public static final int EAP_NOTIFICATION = 2;
    public static final int EAP_NAK = 3;
    public static final int EAP_MD5 = 4;
    public static final int EAP_OTP = 5;
    public static final int EAP_GTC = 6;
    public static final int EAP_TLS = 13;
    public static final int EAP_LEAP = 17;
    public static final int EAP_SIM = 18;
    public static final int EAP_TTLS = 21;
    public static final int EAP_PEAP = 25;
    public static final int EAP_MSCHAPV2 = 26;
    public static final int EAP_CISCO_MSCHAPV2 = 29;
    public static final int EAP_TLV = 33;

    public String getTitleMatch()
    {
        return titleMatch;
    }

    public void setTitleMatch(String titleMatch)
    {
        this.titleMatch = titleMatch;
    }

    public String getCopyright()
    {
        return copy;
    }
    
    public String getStatus()
    {
        return status;
    }

    public String getLoginResult()
    {
        return loginResult;
    }

    public String getLoginResultsUrl()
    {
        return loginResultsUrl;
    }

    public String getReplyMessage()
    {
        return replyMessage;
    }

    public String getUserAgent() {
    	if (userAgent != null && userAgent.trim().length() > 0) return userAgent;
    	return DEFAULT_USER_AGENT;
    }
    
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
}
