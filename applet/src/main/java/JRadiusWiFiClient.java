/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2007 David Bird <david@coova.com>
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

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
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

public class JRadiusWiFiClient extends Applet implements Runnable
{
    static final long serialVersionUID = 0L;
    
    private Thread guiThread = null;
    private String prefixRealm = null;
    private String username = null;
    private String realm = null;
    private String password = null;
    
    private String WISPrLogin = null;
    private String WISPrAbortLogin = null;
    private String WISPrLogoff = null;
    private String WISPrLocationName = null;
    private String[] WISPrXML = new String[2];
    private static String cookie = null;
    
    private static boolean secureRoaming = false;
    private static String otpProxyServer = "ap.coova.org";
    private static String otpProxyPort = "1810";
    private static boolean otpUseSSL = true;
    private static boolean otpTrustAnyCert = true;
    private static String otpCertChain = null;
    
    private static boolean isJavaReady = true;
    private static boolean isReady = false;
    private static boolean isOnline = false;
    
    private boolean paused;
    private final Object pauseLock = new Object();
    private volatile boolean noStopRequested;

    private Dimension d;
    private String message = "JRadius WiFi Client";
    private String ENCODING = "utf-8";

    /**
     * The default url we want to get to.
     */
    public static String publicURL = "http://www.microsoft.com/en/us/default.aspx";
    public static String titleMatch = "Microsoft Corporation";
    
    /**
     * The URL we redirect after successful login.
     */
    public static String baseURL     =  "http://ap.coova.org/wifi/";
    public static String welcomeURL  =  "welcome";
    public static String onlineURL   =  "online";
    public static String loginURL    =  "login";
    public static String noWISPrURL  =  "nowispr";
    public static String badWISPrURL =  "badwispr";

    /**
     * Define the debug state.
     */
    private static boolean DEBUG = true;
    private static StringBuffer debugString = new StringBuffer();
    private boolean isApplet = true;
    private static Boolean isCommunicator;
    private Boolean haveJavaSecurity = null;
    private SecurityManager securityManager;
//    private Image bgImg;
//    private Graphics bgG;
    
    private static JRadiusWiFiClient client = null;
    private static Thread clientThread = null;
    private static ClientGUI clientGUI = null;
    private static boolean runLogout = false;
    
    private static String copy = 
        "---------------------------------------------------\n" + 
        " Running JRadius WiFi Client\n" + 
        " Copyright (c) 2005-2006 PicoPoint B.V.\n"+
        " Copyright (c) 2007 David Bird <david@coova.com>\n"+
        " All Rights Reserved.\n" + 
        "--------------------------------------------------\n";

    public JRadiusWiFiClient getInstance()
    {
        return new JRadiusWiFiClient();
    }
    
    public String getAppletInfo() 
    {
        return copy;
    }
    
    public static void main(String args[])
    {
        client = new JRadiusWiFiClient();
        client.isApplet(false);

        if (args.length >= 2) 
        {
            client.setUsername(args[0]);
            client.setPassword(args[1]);
        }

        if (args.length >= 3)
        {
            otpProxyServer = args[2];
        }

        if (args.length == 4)
        {
            publicURL = args[3];
        }

        clientGUI = new ClientGUI(client);
        client.findWISPrLogin(publicURL);
        clientGUI.setVisible(true);
    }

    public void run()
    {
        if (runLogout)
        {
            doWISPrLogoff();
        }
        else
        {
            doWISPrLogin();
        }
    }
    
    public void login(String username, String password)
    {
        if (username != null) setUsername(username);
        if (password != null) setPassword(password);
        if (client != null)
        {
            runLogout = false;
            if (clientThread != null) clientThread.interrupt();
            (clientThread = new Thread(client)).start();
        }
        else
        {
            doWISPrLogin();
        }
    }

    public void logoff()
    {
        if (client != null)
        {
            runLogout = true;
            if (clientThread != null) clientThread.interrupt();
            (clientThread = new Thread(client)).start();
        }
        else
        {
            doWISPrLogoff();
        }
    }

    public void reset()
    {
        findWISPrLogin(publicURL);
    }

    public void init()
    {
        System.err.println(copy);
        setFont(new Font("Verdana", Font.PLAIN, 12));

        securityManager = System.getSecurityManager();

        if (isCommunicator())
        {
            try
            {
                netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
                netscape.security.PrivilegeManager.enablePrivilege("UniversalConnect");
            }
            catch (Throwable e)
            { }
        }
        else
        {
            try
            {
                com.ms.security.PolicyEngine.assertPermission(com.ms.security.PermissionID.NETIO);
            }
            catch (Throwable e)
            { }
        }

        try
        {
            d = getSize();
        }
        catch (NoSuchMethodError e)
        {
            d = size();
        }

        URL docBase = getCodeBase();
        baseURL = docBase.toExternalForm();
        
//        bgImg = createImage(d.width, d.height);
//        bgG = bgImg.getGraphics();

        debugWrite("Version Info:\n"
                + System.getProperty("java.vendor") + " "
                + System.getProperty("java.version") + " running on "
                + System.getProperty("os.name") + " " 
                + System.getProperty("os.version") + " " 
                + System.getProperty("os.arch"));

        validate();
        
        startGuiThread();
    }

    private void startGuiThread() {
        paused = true;
        noStopRequested = true;

        Runnable r = new Runnable() 
        {
            public void run() 
            {
                runGui();
            }
        };
        guiThread = new Thread(r, "GUI-Thread");
        guiThread.start();
     }

    private void stopGuiThread() 
    {
        noStopRequested = false;
        guiThread.interrupt();
    }

    private void runGui()
    {
        try 
        {
            while (noStopRequested) 
            {
                waitWhilePaused();
                repaint();

                if (!isOnline && !isReady)
                {
                    addItem("Finding out how to login...");
                    findWISPrLogin(publicURL);
                }

                Thread.sleep(1000);
            }
        } 
        catch (InterruptedException x) 
        {
            Thread.currentThread().interrupt();
        }
    }

    private void setPaused(boolean newPauseState) 
    {
        synchronized (pauseLock) 
        {
            if (paused != newPauseState) 
            {	
                paused = newPauseState;
                pauseLock.notifyAll();
            }
        }
    }

    private void waitWhilePaused() throws InterruptedException 
    {
        synchronized (pauseLock) 
        {
            while (paused) 
            {
                pauseLock.wait();
            }
        }
    }

    public void start()
    {
        setPaused(false);
    }

    public void stop()
    {
        isReady = false;
        isOnline = false;
        setPaused(true);
    }

    public void destroy() 
    {
        stopGuiThread();
    }

    public void update(Graphics g)
    {
    	/*
        bgG.setColor(Color.white);
        bgG.setFont(getFont());
        bgG.fillRect(0, 0, d.width, d.height);
        bgG.setColor(Color.blue);
        bgG.drawString(message, 10, d.height / 2 + 5);
        g.drawImage(bgImg, 0, 0, this);
        */
    }

    public void paint(Graphics g)
    {
    	//update(g);
    }

    private void addItem(String newWord)
    {
        addItem(newWord, true);
    }

    private void addItem(String newWord, boolean clean1st)
    {
        if (clean1st)
        {
            message = newWord;
        }
        else
        {
            message += newWord;
        }
        setStatus(message);
        System.out.println(newWord);
    }

    private void debugWrite(String msg)
    {
        if (DEBUG)
        {
            System.out.println(msg);
            debugString.append(msg).append("\n");
        }
    }

    private void redirect(String page)
    {
        if (!isApplet()) 
            return;
        
        try
        {
            AppletContext context = getAppletContext();
            context.showDocument(new URL("javascript:showPage('"+page+"');"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setStatus(String s)
    {
        if (!isApplet()) 
        {
        	clientGUI.setStatus(s);
        	return;
        }
        try
        {
            AppletContext context = getAppletContext();
            context.showDocument(new URL("javascript:showStatus('"+s+"');"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
                addItem("You are already online.");
                redirect(onlineURL);
            }
            else
            {
                addItem("WISPr is not supported here.");
                redirect(noWISPrURL);
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
                String msg = "Found WiFi Network";
                if (WISPrLogin != null)
                {
                    isReady = true;
                }
                if (isApplet())
                {
                    if (WISPrLocationName != null)
                    {
                        msg += " - Location: " + WISPrLocationName;
                    }
                }
                else
                {
                    clientGUI.setLocation(WISPrLocationName);
                }
                addItem(msg);
                redirect(loginURL);
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

            addItem("Logging into the WiFi Network...");
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
        String loginResult = getXMLParam(xml, xmll, "responsecode");
        String loginResultsUrl = getXMLParam(xml, xmll, "loginresultsurl");
        String replyMessage = getXMLParam(xml, xmll, "replymessage");
        WISPrLogoff = getXMLParam(xml, xmll, "logoffurl");

        if (loginResult == null)
        {
            addItem("Login failed (Bad WISPr XML): " + replyMessage);
            return badWISPrURL;
        }

        if ("50".equals(loginResult))
        {
            isOnline = true;
            if (replyMessage == null)
            {
                addItem("Login Successful!");
            }
            else
            {
                addItem(replyMessage);
            }
            debugWrite("Successful login: Redirecting user to " + welcomeURL);
            return welcomeURL;
        }

        if ("201".equals(loginResult))
        {
            if (replyMessage == null)
            {
                addItem("Login pending...");
            }
            else
            {
                addItem(replyMessage);
            }
            debugWrite("Login pending: resultsUrl " + loginResultsUrl);
            return processWISPrResponse(loginResultsUrl);
        }
        
        if (replyMessage == null)
        {
            addItem("Login failed!");
        }
        else
        {
            addItem("Login failed: " + replyMessage);
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
                    addItem(getUsername().replaceFirst("error:", ""));
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
                addItem(line.substring(6));
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
        if (WISPrLogoff == null)
            return;

        addItem("Logging out of WiFi Network...");
        
        PageResult result = getPage(WISPrLogoff, cookie);
        
        if (result.getIsXML()==1)
            debugWrite("WISPr XML:\n" + result.getContent());
        else
            debugWrite("NO WISPr XML in:\n" + result.getContent());
        
        reset();
    }

    private PageResult getPage(final String urlString, final String cookieString) throws RuntimeException
    {
        if (haveJavaSecurity())
        {
            PageResult result = (PageResult) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction()
            {
                public Object run()
                {
                    return JRadiusWiFiClient.doGetPage(urlString, cookieString);
                }
            });
            return result;
        }
        return JRadiusWiFiClient.doGetPage(urlString, cookieString);
    }

    public static PageResult doGetPage(final String urlString, final String cookieString)
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
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);

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

    public static String getOtpCertChain()
    {
        return nonNullString(otpCertChain);
    }

    public static void setOtpCertChain(String url)
    {
        otpCertChain = trimString(url);
    }

    public static boolean getOtpTrustAnyCert()
    {
        return otpTrustAnyCert;
    }

    public static void setOtpTrustAnyCert(boolean trustAnyCert)
    {
        otpTrustAnyCert = trustAnyCert;
    }

    public static String getOtpProxyServer()
    {
        return nonNullString(otpProxyServer);
    }

    public static void setOtpProxyServer(String server)
    {
        otpProxyServer = trimString(server);
    }

    public static String getOtpProxyPort()
    {
        return otpProxyPort;
    }

    public static void setOtpProxyPort(String port)
    {
        String s = trimString(port);
        if (s != null) otpProxyPort = s;
    }

    public static boolean getOtpUseSSL()
    {
        return otpUseSSL;
    }

    public static void setOtpUseSSL(boolean useSSL)
    {
        otpUseSSL = useSSL;
    }

    public static boolean getSecureRoaming()
    {
        return secureRoaming;
    }

    public static void setSecureRoaming(boolean secure)
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

    public static String getBaseURL()
    {
        return nonNullString(baseURL);
    }

    public static void setBaseURL(String url)
    {
        baseURL = trimString(url);
    }

    public static String getLoginURL()
    {
        return nonNullString(loginURL);
    }

    public static void setLoginURL(String url)
    {
        loginURL = trimString(url);
    }

    public static String getNoWISPrURL()
    {
        return nonNullString(noWISPrURL);
    }

    public static void setNoWISPrURL(String url)
    {
        noWISPrURL = trimString(url);
    }

    public static String getOnlineURL()
    {
        return nonNullString(onlineURL);
    }

    public static void setOnlineURL(String url)
    {
        onlineURL = trimString(url);
    }

    public static String getPublicURL()
    {
        return nonNullString(publicURL);
    }

    public static void setPublicURL(String url)
    {
        publicURL = trimString(url);
    }

    public static String getWelcomeURL()
    {
        return nonNullString(welcomeURL);
    }

    public static void setWelcomeURL(String url)
    {
        welcomeURL = trimString(url);
    }

    public static String getDebugString()
    {
        return debugString.toString();
    }

    public static void clearDebugString()
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

    /**
     * @return Returns the isApplet.
     */
    public boolean isApplet()
    {
        return isApplet;
    }

    /**
     * @param isApplet The isApplet to set.
     */
    public void isApplet(boolean isApplet)
    {
        this.isApplet = isApplet;
    }

    public static String getTitleMatch()
    {
        return titleMatch;
    }

    public static void setTitleMatch(String titleMatch)
    {
        JRadiusWiFiClient.titleMatch = titleMatch;
    }
}
