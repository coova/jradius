/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2007 David Bird <david@coova.com>
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

package net.jradius.session;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.jradius.dictionary.Attr_AcctAuthentic;
import net.jradius.dictionary.Attr_AcctInputGigawords;
import net.jradius.dictionary.Attr_AcctInputOctets;
import net.jradius.dictionary.Attr_AcctInputPackets;
import net.jradius.dictionary.Attr_AcctInterimInterval;
import net.jradius.dictionary.Attr_AcctOutputGigawords;
import net.jradius.dictionary.Attr_AcctOutputOctets;
import net.jradius.dictionary.Attr_AcctOutputPackets;
import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_AcctSessionTime;
import net.jradius.dictionary.Attr_AcctTerminateCause;
import net.jradius.dictionary.Attr_CalledStationId;
import net.jradius.dictionary.Attr_CallingStationId;
import net.jradius.dictionary.Attr_ConnectInfo;
import net.jradius.dictionary.Attr_FramedIPAddress;
import net.jradius.dictionary.Attr_IdleTimeout;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_ProxyToRealm;
import net.jradius.dictionary.Attr_Realm;
import net.jradius.dictionary.Attr_ServiceType;
import net.jradius.dictionary.Attr_SessionTimeout;
import net.jradius.dictionary.Attr_StrippedUserName;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.vsa_jradius.Attr_JRadiusRequestId;
import net.jradius.exception.RadiusException;
import net.jradius.exception.RadiusSecurityException;
import net.jradius.log.JRadiusLogEntry;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;

/**
 * The Default Radius Session Base Class.  
 *
 * @author David Bird
 */
public class RadiusSession implements JRadiusSession, Serializable
{
    public static final long serialVersionUID = 0L;
    
    // Internal "State" Attributes
    private int sessionState = JRadiusSession.UNKNOWN_STATE;
    private String sessionKey;
    private String jRadiusKey;
    private boolean newSession = true;
    private long timeStamp;
    
    private JRadiusRequest lastRadiusRequest;
    private Map<String, JRadiusLogEntry> logEntryMap = new HashMap<String, JRadiusLogEntry>();
    private Map<String, Serializable> attributeMap = new HashMap<String, Serializable>();

    // Basic Session Configuration
    private String username;
    private String realm;
    private String password;
    private String sessionId;
    private String proxyToRealm;
    private String redirectURL;
    private boolean secured;

    // Relevant Attributes
    private String connectInfo;
    private String callingStationId;
    private String calledStationId;
    private String nasIdentifier;
    private String nasIPAddress;
    private String clientIPAddress;
    private String framedIPAddress;
    private byte[] radiusClass;

    // Session Accounting Configuration
    private Long idleTimeout;
    private Long sessionTimeout;
    private Long interimInterval;

    // Session Bandwidth & Data Limit Configurations
    private Long maxBandwidthUp;
    private Long maxBandwidthDown;
    private Long minBandwidthUp;
    private Long minBandwidthDown;
    private Long maxOctetsUp;
    private Long maxOctetsDown;
    private Long maxOctetsTotal;
    
    private String nasType;
    private Long nasPortType;
    private Long acctAuthentic;

    // Session Accounting Data
    private Long serviceType;
    private Long sessionTime;
    private Long packetsIn;
    private Long packetsOut;
    private Long octetsIn;
    private Long octetsOut;
    private Long gigaWordsIn;
    private Long gigaWordsOut;
    private Long terminateCause;
    private Date authorizeTime;
    private Date startTime;
    private Date lastInterimTime;
    private Date stopTime;

	private volatile boolean locked = false;

	public void lock()
	{
		synchronized (this)
		{
			//check whether we are locked, if so... enter wait()
			while (this.locked)
			{
				try
				{
					this.wait();
				}
				catch (InterruptedException e)
				{
					Thread.yield();
				}
			}

			this.locked = true;
		}
	}

	public void unlock()
	{
		synchronized (this)
		{
			//set unlocked, notify one
			this.locked = false;
			this.notify();
		}
	}

    public RadiusSession ()
    {
    }
    
    public RadiusSession(String session)
    {
        this();
        setSessionKey(session);
        RadiusLog.debug("Creating new session: " + session);
    }
    
    
	public boolean isAccountingReversed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setAttribute(String name, Serializable value) {
		attributeMap.put(name, value);
	}

	public Serializable getAttribute(String name) {
		return attributeMap.get(name);
	}

	public boolean isLogging()
	{
		return true;
	}
	
	public JRadiusLogEntry getLogEntry(JRadiusEvent event, String key)
    {
        JRadiusLogEntry entry = logEntryMap.get(key);
        
        if (entry == null)
        {
            RadiusLog.debug("Creating new Session/LogEntry: " + getSessionKey() + "/" + key);
            entry = JRadiusSessionManager.getManager(event.getSender()).newLogEntry(event, this, key);
            logEntryMap.put(key, entry);
        }
        
        RadiusLog.debug("Retreived Session/LogEntry: " + getSessionKey() + "/" + key);
        entry.setLastUpdate(new Date());
        return entry;
    }
    
    public JRadiusLogEntry getLogEntry(JRadiusRequest request) throws RadiusException
    {
        AttributeList ci = request.getConfigItems();
        RadiusAttribute a = ci.get(Attr_JRadiusRequestId.TYPE);
        String key;

        if (a != null) key = (String)a.getValue().getValueObject();
        else key = Integer.toString((char)request.getRequestPacket().getIdentifier());

        JRadiusLogEntry entry = getLogEntry(request, key);
        entry.setCode(new Integer(request.getReturnValue()));
        return entry;
    }
    
    public void addLogMessage(JRadiusRequest request, String message) throws RadiusException
    {
        getLogEntry(request).addMessage(message);
    }

    public void commitLogEntries(int result)
    {
        Iterator<Map.Entry<String, JRadiusLogEntry>> it = logEntryMap.entrySet().iterator();

        long now = new Date().getTime();
        long threshold = 180;

        while (it.hasNext())
        {
            Map.Entry<String, JRadiusLogEntry> mapEntry = it.next();
            JRadiusLogEntry entry = mapEntry.getValue();
            if (entry.isFinished() && !entry.isCommitted())
            {
                entry.setCode(new Integer(result));
                commitLogEntry(entry, result);
                entry.setCommitted(true);
            }
            if (entry.getLastUpdate().getTime() < (now - threshold))
            {
                it.remove();
            }
        }
    }

    public void commitLogEntry(JRadiusLogEntry entry, int result)
    {
        String mesg = entry.toString();
        if (mesg != null) RadiusLog.info(mesg);
    }

    public boolean onPreProcessing(JRadiusRequest request) throws RadiusException
    {
        switch(request.getType())
        {
            case JRadiusServer.JRADIUS_authorize:
            {
                if (getSessionState() == UNKNOWN_STATE)
                    setSessionState(AUTH_PENDING);
            }
            break;

            case JRadiusServer.JRADIUS_preacct:
            {
                if (!request.isAccountingRequest()) break;
                AccountingRequest accountingRequest = (AccountingRequest) request.getRequestPacket();

                switch (accountingRequest.getAccountingStatusType())
                {
                    case -1:
                    {
                        return onNoAccountingStatusType(request);
                    }

                    case AccountingRequest.ACCT_STATUS_START:
                    case AccountingRequest.ACCT_STATUS_ACCOUNTING_ON:
                    {
                        ensureSessionState(request, AUTH_ACCEPTED);
                        setSessionState(ACCT_STARTED);
                    }
                    break;
                    
                    case AccountingRequest.ACCT_STATUS_STOP:
                    case AccountingRequest.ACCT_STATUS_ACCOUNTING_OFF:
                    {
                        ensureSessionState(request, ACCT_STARTED);
                        setSessionState(ACCT_STOPPED);
                    }
                    break;
                    
                    case AccountingRequest.ACCT_STATUS_INTERIM:
                    {
                        ensureSessionState(request, ACCT_STARTED);
                    }
                    break;
                }
            }
            break;
        }
        
        return false;
    }
    
    public void onPostProcessing(JRadiusRequest request) throws RadiusException
    {
        // If we have a session, fire off events
        switch(request.getType())
        {
            case JRadiusServer.JRADIUS_authorize:
                onAuthorization(request);
                break;
            case JRadiusServer.JRADIUS_post_auth:
                onPostAuthentication(request);
                break;
            case JRadiusServer.JRADIUS_preacct:
                onAccounting(request);
                break;
        }
    }
    
    public void onAuthorization(JRadiusRequest request) throws RadiusException 
    { 
        if (!checkSessionState(AUTH_PENDING | AUTH_ACCEPTED | ACCT_STARTED))
            setSessionState(AUTH_PENDING);
    }
    
    public void onPostAuthentication(JRadiusRequest request) throws RadiusException 
    { 
        RadiusPacket rep = request.getReplyPacket();
        boolean success = (rep instanceof AccessAccept && request.getReturnValue() != JRadiusServer.RLM_MODULE_REJECT);
        RadiusLog.debug("Authentication: " + request + " was" + (success ? "" : " NOT") + " sucessful");
        if (success)
        {
            Long sessionTimeout = (Long)rep.getAttributeValue(Attr_SessionTimeout.TYPE);
            if (checkSessionState(ACCT_STARTED))
            {
                if (sessionTimeout != null)
                {
                    Long sessionTime = getSessionTime();
                    if (sessionTime != null)
                    {
                        // Compensate the sessionTimeout for re-authentications
                        sessionTimeout = new Long(sessionTimeout.longValue() - sessionTime.longValue());
                    }
                }
            }
            else
            {
                setSessionState(AUTH_ACCEPTED);
            }
            setIdleTimeout((Long)rep.getAttributeValue(Attr_IdleTimeout.TYPE));
            setInterimInterval((Long)rep.getAttributeValue(Attr_AcctInterimInterval.TYPE));
            setSessionTimeout(sessionTimeout);
        }
        else
        {
            setSessionState(AUTH_REJECTED);
        }
    }

    public void stopSession(boolean force)
    {
        setSessionState(JRadiusSession.ACCT_STOPPED);
    }

    public void initSession(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();

        String username = (String)req.getAttributeValue(Attr_UserName.TYPE);
        String realm = (String)req.getAttributeValue(Attr_Realm.TYPE);
        String stripUsername = null;

        if (username != null)
        {
            stripUsername = username;
            
            if (realm == null)
            {
                String[] s = RadiusSessionSupport.splitUserName(username);
                if (s != null && s.length == 2)
                {
                    stripUsername = s[0];
                    realm = s[1];
                }
            }
            else
            {
                stripUsername = (String)req.getAttributeValue(Attr_StrippedUserName.TYPE);
                if (stripUsername == null) stripUsername = username;
            }
        }
        
        setUsername(stripUsername);
        setRealm(realm);

        Long zero = new Long(0);
        
        setSessionTime(zero);
        setPacketsIn(zero);
        setPacketsOut(zero);
        setOctetsIn(zero);
        setOctetsOut(zero);
        setGigaWordsIn(zero);
        setGigaWordsOut(zero);
        
        setServiceType((Long)req.getAttributeValue(Attr_ServiceType.TYPE));
        setNasPortType((Long)req.getAttributeValue(Attr_NASPortType.TYPE));
        setConnectInfo((String)req.getAttributeValue(Attr_ConnectInfo.TYPE));
        setCallingStationId((String)req.getAttributeValue(Attr_CallingStationId.TYPE));
        setCalledStationId((String)req.getAttributeValue(Attr_CalledStationId.TYPE));
        setSessionId((String) req.getAttributeValue(Attr_AcctSessionId.TYPE));
        setNasIdentifier((String)req.getAttributeValue(Attr_NASIdentifier.TYPE));
        
        InetAddress inet = (InetAddress) req.getAttributeValue(Attr_NASIPAddress.TYPE);
        if (inet != null) setNasIPAddress(inet.getHostAddress());

        inet = (InetAddress) req.getAttributeValue(Attr_FramedIPAddress.TYPE);
        if (inet != null) setFramedIPAddress(inet.getHostAddress());

        if (getNasIdentifier() == null) setNasIdentifier(getNasIPAddress());
    }

    public void onAccounting(JRadiusRequest request) throws RadiusException
    {
        RadiusPacket req = request.getRequestPacket();
        Long i;

        if ((i = (Long)req.getAttributeValue(Attr_AcctAuthentic.TYPE)) != null) setAcctAuthentic(i);
        if ((i = (Long)req.getAttributeValue(Attr_AcctSessionTime.TYPE)) != null) setSessionTime(i);
        if ((i = (Long)req.getAttributeValue(Attr_AcctInputPackets.TYPE)) != null) setPacketsIn(i);
        if ((i = (Long)req.getAttributeValue(Attr_AcctOutputPackets.TYPE)) != null) setPacketsOut(i);
        if ((i = (Long)req.getAttributeValue(Attr_AcctTerminateCause.TYPE)) != null) setTerminateCause(i);

        Long octsIn  = (Long)req.getAttributeValue(Attr_AcctInputOctets.TYPE);
        Long octsOut = (Long)req.getAttributeValue(Attr_AcctOutputOctets.TYPE);
        Long gigaIn  = (Long)req.getAttributeValue(Attr_AcctInputGigawords.TYPE);
        Long gigaOut = (Long)req.getAttributeValue(Attr_AcctOutputGigawords.TYPE);

        Long cOctsIn = getOctetsIn();
        Long cOctsOut = getOctetsOut();
        
        if (octsIn != null && octsOut != null && cOctsIn != null && cOctsOut != null)
        {
            if (octsIn.longValue() < cOctsIn.longValue())
            {
                Long cGigaIn = getGigaWordsIn();
                long currentGigawords = (cGigaIn == null) ? 0 : cGigaIn.longValue();
                long newGigawords = (gigaIn == null) ? 0 : gigaIn.longValue();
                if (newGigawords != (currentGigawords + 1))
                {
                    addLogMessage(request, "Fixing Gigawords-In");
                    req.overwriteAttribute(new Attr_AcctInputGigawords(gigaIn = new Long(currentGigawords + 1)));
                }
            }
            if (octsOut.longValue() < cOctsOut.longValue())
            {
                Long cGigaOut = getGigaWordsOut();
                long currentGigawords = (cGigaOut == null) ? 0 : cGigaOut.longValue();
                long newGigawords = (gigaOut == null) ? 0 : gigaOut.longValue();
                if (newGigawords != (currentGigawords + 1))
                {
                    addLogMessage(request, "Fixing Gigawords-Out");
                    req.overwriteAttribute(new Attr_AcctOutputGigawords(gigaOut = new Long(currentGigawords + 1)));
                }
            }
        }

        if (octsIn  != null) setOctetsIn(octsIn);
        if (octsOut != null) setOctetsOut(octsOut);
        if (gigaIn  != null) setGigaWordsIn(gigaIn);
        if (gigaOut != null) setGigaWordsOut(gigaOut);
    }

    protected boolean checkSessionState(int state)
    {
        return !((getSessionState() & state) == 0);
    }
    
    public void ensureSessionState(JRadiusRequest request, int state) throws RadiusException
    {
        if (!checkSessionState(state))
        {
            // Remove any Proxy-To-Realm in the control items to prevent the proxy
            request.getConfigItems().remove(Attr_ProxyToRealm.TYPE);
            throw new RadiusSecurityException("Received unexpected packet for session: " + getSessionKey() + " (" + getSessionState() + " != " + state + ")");
        }
    }
    
    public boolean onNoAccountingStatusType(JRadiusRequest request) throws RadiusException
    {
    	JRadiusLogEntry logEntry = getLogEntry(request);
        String error = "Accounting packet without a Acct-Status-Type!";
        RadiusLog.error(error);
        logEntry.addMessage(error);
        request.setReturnValue(JRadiusServer.RLM_MODULE_REJECT);
        return true;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername()
    {
        return username;
    }
    
    /**
     * @param username The username to set.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    /**
     * @return Returns the realm.
     */
    public String getRealm()
    {
        return realm;
    }
    
    /**
     * @param realm The realm to set.
     */
    public void setRealm(String realm)
    {
        this.realm = realm;
    }
    
    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
        return password;
    }
    
    /**
     * @param password The password to set.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    /**
     * @return Returns the sessionKey.
     */
    public String getSessionKey()
    {
        return sessionKey;
    }
    
    /**
     * @param sessionKey The sessionKey to set.
     */
    public void setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
    }
    
    /**
     * @return Returns the sessionId.
     */
    public String getSessionId()
    {
        return sessionId;
    }
    
    /**
     * @param sessionId The sessionId to set.
     */
    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }
    
    /**
     * @return Returns the serviceType.
     */
    public Long getServiceType()
    {
        return serviceType;
    }
    
    /**
     * @param serviceType The serviceType to set.
     */
    public void setServiceType(Long serviceType)
    {
        this.serviceType = serviceType;
    }
    
    /**
     * @return Returns the idleTimeout.
     */
    public Long getIdleTimeout()
    {
        return idleTimeout;
    }
    
    /**
     * @param idleTimeout The idleTimeout to set.
     */
    public void setIdleTimeout(Long idleTimeout)
    {
        this.idleTimeout = idleTimeout;
    }
    
    /**
     * @return Returns the interimInterval.
     */
    public Long getInterimInterval()
    {
        return interimInterval;
    }
    
    /**
     * @param interimInterval The interimInterval to set.
     */
    public void setInterimInterval(Long interimInterval)
    {
        this.interimInterval = interimInterval;
    }
    
    /**
     * @return Returns the sessionTimeout.
     */
    public Long getSessionTimeout()
    {
        return sessionTimeout;
    }
    
    /**
     * @param sessionTimeout The sessionTimeout to set.
     */
    public void setSessionTimeout(Long sessionTimeout)
    {
        this.sessionTimeout = sessionTimeout;
    }
    
    /**
     * @return Returns the sessionTime.
     */
    public Long getSessionTime()
    {
        return sessionTime;
    }
    
    /**
     * @param sessionTime The sessionTime to set.
     */
    public void setSessionTime(Long sessionTime)
    {
        this.sessionTime = sessionTime;
    }
    
    /**
     * @return Returns the startTime.
     */
    public Date getStartTime()
    {
        return startTime;
    }
    
    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }
    
    /**
     * @return Returns the lastInterimTime.
     */
    public Date getLastInterimTime()
    {
        return lastInterimTime;
    }
    /**
     * @param lastInterimTime The lastInterimTime to set.
     */
    public void setLastInterimTime(Date lastInterimTime)
    {
        this.lastInterimTime = lastInterimTime;
    }
    /**
     * @return Returns the stopTime.
     */
    public Date getStopTime()
    {
        return stopTime;
    }
    /**
     * @param stopTime The stopTime to set.
     */
    public void setStopTime(Date stopTime)
    {
        this.stopTime = stopTime;
    }
    /**
     * @return Returns the gigaWordsIn.
     */
    public Long getGigaWordsIn()
    {
        return gigaWordsIn;
    }
    
    /**
     * @param gigaWordsIn The gigaWordsIn to set.
     */
    public void setGigaWordsIn(Long gigaWordsIn)
    {
        this.gigaWordsIn = gigaWordsIn;
    }
    
    /**
     * @return Returns the gigaWordsOut.
     */
    public Long getGigaWordsOut()
    {
        return gigaWordsOut;
    }
    
    /**
     * @param gigaWordsOut The gigaWordsOut to set.
     */
    public void setGigaWordsOut(Long gigaWordsOut)
    {
        this.gigaWordsOut = gigaWordsOut;
    }
    
    /**
     * @return Returns the octetsIn.
     */
    public Long getOctetsIn()
    {
        return octetsIn;
    }
    
    /**
     * @param octetsIn The octetsIn to set.
     */
    public void setOctetsIn(Long octetsIn)
    {
        this.octetsIn = octetsIn;
    }
    
    /**
     * @return Returns the octetsOut.
     */
    public Long getOctetsOut()
    {
        return octetsOut;
    }
    
    /**
     * @param octetsOut The octetsOut to set.
     */
    public void setOctetsOut(Long octetsOut)
    {
        this.octetsOut = octetsOut;
    }
    
    public Long getTotalOctetsIn()
    {
        if (octetsIn == null) return null;
        long l  = octetsIn.longValue();
        if (gigaWordsIn != null) 
        {
            l |= gigaWordsIn.longValue() << 32;
        }
        return new Long(l);
    }

    public Long getTotalOctetsOut()
    {
        if (octetsOut == null) return null;
        long l  = octetsOut.longValue();
        if (gigaWordsOut != null) 
        {
            l |= gigaWordsOut.longValue() << 32;
        }
        return new Long(l);
    }
    
    /**
     * @return Returns the packetsIn.
     */
    public Long getPacketsIn()
    {
        return packetsIn;
    }
    
    /**
     * @param packetsIn The packetsIn to set.
     */
    public void setPacketsIn(Long packetsIn)
    {
        this.packetsIn = packetsIn;
    }
    
    /**
     * @return Returns the packetsOut.
     */
    public Long getPacketsOut()
    {
        return packetsOut;
    }
    
    /**
     * @param packetsOut The packetsOut to set.
     */
    public void setPacketsOut(Long packetsOut)
    {
        this.packetsOut = packetsOut;
    }
    
    /**
     * @return Returns the terminateCause.
     */
    public Long getTerminateCause()
    {
        return terminateCause;
    }
    
    /**
     * @param terminateCause The terminateCause to set.
     */
    public void setTerminateCause(Long terminateCause)
    {
        this.terminateCause = terminateCause;
    }
    
    /**
     * @return Returns the radiusClass.
     */
    public byte[] getRadiusClass()
    {
        return radiusClass;
    }

    /**
     * @param radiusClass The radiusClass to set.
     */
    public void setRadiusClass(byte[] radiusClass)
    {
        this.radiusClass = radiusClass;
    }

    /**
     * @return Returns the sessionState.
     */
    public int getSessionState()
    {
        return sessionState;
    }
    
    /**
     * @param sessionState The sessionState to set.
     */
    public void setSessionState(int sessionState)
    {
        this.sessionState |= sessionState;
    }
    
    /**
     * @return Returns the proxyToRealm.
     */
    public String getProxyToRealm()
    {
        return proxyToRealm;
    }

    /**
     * @param proxyToRealm The proxyToRealm to set.
     */
    public void setProxyToRealm(String proxyToRealm)
    {
        this.proxyToRealm = proxyToRealm;
    }
    
    /**
     * @return Returns the secured.
     */
    public boolean isSecured()
    {
        return secured;
    }

    /**
     * @param secured The secured to set.
     */
    public void setSecured(boolean secured)
    {
        this.secured = secured;
    }

    /**
     * @return Returns the calledStation.
     */
    public String getCalledStationId()
    {
        return calledStationId;
    }
    /**
     * @param calledStation The calledStation to set.
     */
    public void setCalledStationId(String calledStation)
    {
        this.calledStationId = calledStation;
    }
    /**
     * @return Returns the callingStation.
     */
    public String getCallingStationId()
    {
        return callingStationId;
    }
    /**
     * @param callingStation The callingStation to set.
     */
    public void setCallingStationId(String callingStation)
    {
        this.callingStationId = callingStation;
    }
    /**
     * @return Returns the connectInfo.
     */
    public String getConnectInfo()
    {
        return connectInfo;
    }
    /**
     * @param connectInfo The connectInfo to set.
     */
    public void setConnectInfo(String connectInfo)
    {
        this.connectInfo = connectInfo;
    }
    
    /**
     * @return Returns the clientIP.
     */
    public String getClientIPAddress()
    {
        return clientIPAddress;
    }
    
    /**
     * @return Returns the framedIPAddress.
     */
    public String getFramedIPAddress()
    {
        return framedIPAddress;
    }
    /**
     * @param framedIPAddress The framedIPAddress to set.
     */
    public void setFramedIPAddress(String framedIPAddress)
    {
        this.framedIPAddress = framedIPAddress;
    }
    /**
     * @param clientIP The clientIP to set.
     */
    public void setClientIPAddress(String clientIP)
    {
        this.clientIPAddress = clientIP;
    }
    /**
     * @return Returns the nasID.
     */
    public String getNasIdentifier()
    {
        return nasIdentifier;
    }
    /**
     * @param nasID The nasID to set.
     */
    public void setNasIdentifier(String nasID)
    {
        this.nasIdentifier = nasID;
    }
    /**
     * @return Returns the nasIP.
     */
    public String getNasIPAddress()
    {
        return nasIPAddress;
    }
    /**
     * @param nasIP The nasIP to set.
     */
    public void setNasIPAddress(String nasIP)
    {
        this.nasIPAddress = nasIP;
    }

    public Long getMaxBandwidthDown()
    {
        return maxBandwidthDown;
    }

    public void setMaxBandwidthDown(Long maxBandwidthDown)
    {
        this.maxBandwidthDown = maxBandwidthDown;
    }

    public Long getMaxBandwidthUp()
    {
        return maxBandwidthUp;
    }

    public void setMaxBandwidthUp(Long maxBandwidthUp)
    {
        this.maxBandwidthUp = maxBandwidthUp;
    }

    public Long getMinBandwidthDown()
    {
        return minBandwidthDown;
    }

    public void setMinBandwidthDown(Long minBandwidthDown)
    {
        this.minBandwidthDown = minBandwidthDown;
    }

    public Long getMinBandwidthUp()
    {
        return minBandwidthUp;
    }

    public void setMinBandwidthUp(Long minBandwidthUp)
    {
        this.minBandwidthUp = minBandwidthUp;
    }
    /**
     * @return Returns the timeStamp.
     */
    public long getTimeStamp()
    {
        return timeStamp;
    }
    
    /**
     * @param timeStamp The timeStamp to set.
     */
    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }
    
    /**
     * @return Returns the newSession.
     */
    public boolean isNewSession()
    {
        return newSession;
    }
    
    /**
     * @param newSession The newSession to set.
     */
    public void setNewSession(boolean newSession)
    {
        this.newSession = newSession;
    }
    
    public String getJRadiusKey()
    {
        return jRadiusKey;
    }
    
    public void setJRadiusKey(String jRadiusKey)
    {
        this.jRadiusKey = jRadiusKey;
    }
    
    public JRadiusRequest getLastRadiusRequest()
    {
        return lastRadiusRequest;
    }
    
    public void setLastRadiusRequest(JRadiusRequest lastRadiusRequest)
    {
        this.lastRadiusRequest = lastRadiusRequest;
    }

	public Long getMaxOctetsDown() 
	{
		return maxOctetsDown;
	}

	public void setMaxOctetsDown(Long maxOctetsDown) 
	{
		this.maxOctetsDown = maxOctetsDown;
	}

	public Long getMaxOctetsUp() 
	{
		return maxOctetsUp;
	}

	public void setMaxOctetsUp(Long maxOctetsUp) 
	{
		this.maxOctetsUp = maxOctetsUp;
	}

	public Long getMaxOctetsTotal() 
	{
		return maxOctetsTotal;
	}

	public void setMaxOctetsTotal(Long maxOctetsTotal) 
	{
		this.maxOctetsTotal = maxOctetsTotal;
	}

	public String getNasType() 
	{
		return nasType;
	}

	public void setNasType(String nasType) 
	{
		this.nasType = nasType;
	}

    public String getRedirectURL() 
    {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) 
	{
		this.redirectURL = redirectURL;
	}

    public String toString()
    {
        return getSessionKey();
    }

    public Date getAuthorizeTime()
    {
        return authorizeTime;
    }

    public void setAuthorizeTime(Date authorizeTime)
    {
        this.authorizeTime = authorizeTime;
    }

    public Long getNasPortType()
    {
        return nasPortType;
    }

    public void setNasPortType(Long nasPortType)
    {
        this.nasPortType = nasPortType;
    }

    public Long getAcctAuthentic()
    {
        return acctAuthentic;
    }

    public void setAcctAuthentic(Long acctAuthentic)
    {
        this.acctAuthentic = acctAuthentic;
    }
}
