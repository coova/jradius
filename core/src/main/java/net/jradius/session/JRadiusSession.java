/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (C) 2006-2007 David Bird <david@coova.com>
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
import java.util.Date;

import net.jradius.exception.RadiusException;
import net.jradius.log.JRadiusLogEntry;
import net.jradius.server.JRadiusEvent;
import net.jradius.server.JRadiusRequest;


/**
 * Defines a Radius Session. This bean is here to help with the handling
 * of Radius Sessions (an authentication event followed by accounting). 
 *
 * @author David Bird
 */
public interface JRadiusSession extends Serializable
{
    public static final long serialVersionUID = 0L;
    
    // status values
    public static final int RADIUS_ERROR      = -1;
    public static final int UNKNOWN_STATE     = 0;

    public static final int AUTH_PENDING      = (1 << 0);
    public static final int AUTH_ACCEPTED     = (1 << 1);
    public static final int AUTH_REJECTED     = (1 << 2);
    public static final int ACCT_STARTED      = (1 << 3);
    public static final int ACCT_STOPPED      = (1 << 4);
    
    public static final int SESSION_STARTED   = (AUTH_PENDING|AUTH_ACCEPTED|ACCT_STARTED);
    public static final int SESSION_STOPPED   = (AUTH_PENDING|AUTH_ACCEPTED|ACCT_STARTED|ACCT_STOPPED);
    public static final int ACCT_ONLY_STARTED = (ACCT_STARTED);
    public static final int ACCT_ONLY_STOPPED = (ACCT_STARTED|ACCT_STOPPED);

    // default attribute map names
    public static final String SESSION_ATTRIBUTE_LOCATION_ID = "locationID";
	public static final String SESSION_ATTRIBUTE_LOCATION_TYPE = "locationType";
	public static final String SESSION_ATTRIBUTE_LOCATION_NAME = "locationName";
	public static final String SESSION_ATTRIBUTE_LOCATION_CITY = "locationCity";
	public static final String SESSION_ATTRIBUTE_LOCATION_STATE_PROVINCE = "locationStateProvince";
	public static final String SESSION_ATTRIBUTE_LOCATION_COUNTRY_CODE = "locationCountryCode";
    
    public JRadiusLogEntry getLogEntry(JRadiusEvent event, String key);
    
    public JRadiusLogEntry getLogEntry(JRadiusRequest request) throws RadiusException;
    
    public void addLogMessage(JRadiusRequest request, String message) throws RadiusException;

    public void commitLogEntry(JRadiusLogEntry entry, int result);

    public void commitLogEntries(int result);

    public void lock();
    public void unlock();

    /**
     * This method is kicked off by the InitSessionHandler after a new
     * PPRadiusSession has been created.
     * @param request The JRadiusRequest
     */
    public void initSession(JRadiusRequest request) throws RadiusException;
    
    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);

    public void onPostProcessing(JRadiusRequest request) throws RadiusException;

    public void onAuthorization(JRadiusRequest request) throws RadiusException;

    public boolean onPreProcessing(JRadiusRequest request) throws RadiusException;
    
    public void onPostAuthentication(JRadiusRequest request) throws RadiusException; 

    /**
     * Updates the session with attributes from the accounting request. This method
     * is fired off during post processing.
     * @param request
     * @throws RadiusException
     */
    public void onAccounting(JRadiusRequest request) throws RadiusException;

    public boolean onNoAccountingStatusType(JRadiusRequest request) throws RadiusException;

    public void ensureSessionState(JRadiusRequest request, int state) throws RadiusException;

    public boolean isAccountingReversed();
    
    /**
     * @return Returns the username.
     */
    public String getUsername();
    
    /**
     * @param username The username to set.
     */
    public void setUsername(String username);
    
    /**
     * @return Returns the realm.
     */
    public String getRealm();
    
    /**
     * @param realm The realm to set.
     */
    public void setRealm(String realm);
    
    /**
     * @return Returns the password.
     */
    public String getPassword();
    
    /**
     * @param password The password to set.
     */
    public void setPassword(String password);
    
    /**
     * @return Returns the sessionKey.
     */
    public String getSessionKey();
    
    /**
     * @param sessionKey The sessionKey to set.
     */
    public void setSessionKey(String sessionKey);
    
    /**
     * @return Returns the sessionId.
     */
    public String getSessionId();
    
    /**
     * @param sessionId The sessionId to set.
     */
    public void setSessionId(String sessionId);
    
    /**
     * @return Returns the serviceType.
     */
    public Long getServiceType();

    /**
     * @param serviceType The serviceType to set.
     */
    public void setServiceType(Long serviceType);
    
    /**
     * @return Returns the idleTimeout.
     */
    public Long getIdleTimeout();
    
    /**
     * @param idleTimeout The idleTimeout to set.
     */
    public void setIdleTimeout(Long idleTimeout);
    
    /**
     * @return Returns the interimInterval.
     */
    public Long getInterimInterval();
    
    /**
     * @param interimInterval The interimInterval to set.
     */
    public void setInterimInterval(Long interimInterval);
    
    /**
     * @return Returns the sessionTimeout.
     */
    public Long getSessionTimeout();
    
    /**
     * @param sessionTimeout The sessionTimeout to set.
     */
    public void setSessionTimeout(Long sessionTimeout);
    
    /**
     * @return Returns the sessionTime.
     */
    public Long getSessionTime();
    
    /**
     * @param sessionTime The sessionTime to set.
     */
    public void setSessionTime(Long sessionTime);
    
    /**
     * @return Returns the startTime.
     */
    public Date getStartTime();
    
    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(Date startTime);
    
    /**
     * @return Returns the lastInterimTime.
     */
    public Date getLastInterimTime();

    /**
     * @param lastInterimTime The lastInterimTime to set.
     */
    public void setLastInterimTime(Date lastInterimTime);

    /**
     * @return Returns the stopTime.
     */
    public Date getStopTime();

    /**
     * @param stopTime The stopTime to set.
     */
    public void setStopTime(Date stopTime);

    /**
     * @return Returns the gigaWordsIn.
     */
    public Long getGigaWordsIn();

    /**
     * @param gigaWordsIn The gigaWordsIn to set.
     */
    public void setGigaWordsIn(Long gigaWordsIn);

    /**
     * @return Returns the gigaWordsOut.
     */
    public Long getGigaWordsOut();

    /**
     * @param gigaWordsOut The gigaWordsOut to set.
     */
    public void setGigaWordsOut(Long gigaWordsOut);

    /**
     * @return Returns the octetsIn.
     */
    public Long getOctetsIn();

    /**
     * @param octetsIn The octetsIn to set.
     */
    public void setOctetsIn(Long octetsIn);

    /**
     * @return Returns the octetsOut.
     */
    public Long getOctetsOut();

    /**
     * @param octetsOut The octetsOut to set.
     */
    public void setOctetsOut(Long octetsOut);

    public Long getTotalOctetsIn();

    public Long getTotalOctetsOut();

    /**
     * @return Returns the packetsIn.
     */
    public Long getPacketsIn();
    
    /**
     * @param packetsIn The packetsIn to set.
     */
    public void setPacketsIn(Long packetsIn);

    /**
     * @return Returns the packetsOut.
     */
    public Long getPacketsOut();

    /**
     * @param packetsOut The packetsOut to set.
     */
    public void setPacketsOut(Long packetsOut);

    /**
     * @return Returns the terminateCause.
     */
    public Long getTerminateCause();

    /**
     * @param terminateCause The terminateCause to set.
     */
    public void setTerminateCause(Long terminateCause);

    /**
     * @return Returns the radiusClass.
     */
    public byte[] getRadiusClass();

    /**
     * @param radiusClass The radiusClass to set.
     */
    public void setRadiusClass(byte[] radiusClass);

    /**
     * @return Returns the sessionState.
     */
    public int getSessionState();

    /**
     * @param sessionState The sessionState to set.
     */
    public void setSessionState(int sessionState);

    /**
     * @return Returns the proxyToRealm.
     */
    public String getProxyToRealm();

    /**
     * @param proxyToRealm The proxyToRealm to set.
     */
    public void setProxyToRealm(String proxyToRealm);

    /**
     * @return Returns the secured.
     */
    public boolean isSecured();

    /**
     * @param secured The secured to set.
     */
    public void setSecured(boolean secured);

    /**
     * @return Returns the calledStationId.
     */
    public String getCalledStationId();

    /**
     * @param calledStationId The calledStationId to set.
     */
    public void setCalledStationId(String calledStationId);

    /**
     * @return Returns the callingStationId.
     */
    public String getCallingStationId();

    /**
     * @param callingStationId The callingStationId to set.
     */
    public void setCallingStationId(String callingStationId);

    /**
     * @return Returns the connectInfo.
     */
    public String getConnectInfo();

    /**
     * @param connectInfo The connectInfo to set.
     */
    public void setConnectInfo(String connectInfo);

    /**
     * @return Returns the clientIPAddress.
     */
    public String getClientIPAddress();

    /**
     * @param clientIPAddress The clientIPAddress to set.
     */
    public void setClientIPAddress(String clientIPAddress);

    /**
     * @return Returns the nasIdentifier.
     */
    public String getNasIdentifier();

    /**
     * @param nasIdentifier The nasIdentifier to set.
     */
    public void setNasIdentifier(String nasIdentifier);

    /**
     * @return Returns the nasIPAddress.
     */
    public String getNasIPAddress();

    /**
     * @param nasIPAddress The nasIPAddress to set.
     */
    public void setNasIPAddress(String nasIPAddress);

    /**
     * @return Returns the framedIPAddress.
     */
    public String getFramedIPAddress();

    /**
     * @param framedIPAddress The framedIPAddress to set.
     */
    public void setFramedIPAddress(String framedIPAddress);

    /**
     * @return Returns the timeStamp.
     */
    public long getTimeStamp();

    /**
     * @param timeStamp The timeStamp to set.
     */
    public void setTimeStamp(long timeStamp);

    public Long getMaxBandwidthDown();

    public void setMaxBandwidthDown(Long maxBandwidthDown);

    public Long getMaxBandwidthUp();

    public void setMaxBandwidthUp(Long maxBandwidthUp);

    public Long getMinBandwidthDown();

    public void setMinBandwidthDown(Long minBandwidthDown);

    /**
     * @return Returns the minBandwidthUp.
     */
    public Long getMinBandwidthUp();

    /**
     * @param minBandwidthUp the minBandwidth to set.
     */
    public void setMinBandwidthUp(Long minBandwidthUp);

	public Long getMaxOctetsDown();

	public void setMaxOctetsDown(Long maxOctetsDown);

	public Long getMaxOctetsUp(); 

	public void setMaxOctetsUp(Long maxOctetshUp); 
	
	public Long getMaxOctetsTotal();

	public void setMaxOctetsTotal(Long maxOctetsTotal);

	public String getNasType() ;

	public void setNasType(String nasType);

	public String getRedirectURL();

	public void setRedirectURL(String redirectURL);
	
    public String getJRadiusKey();

    public void setJRadiusKey(String jRadiusKey);

    public JRadiusRequest getLastRadiusRequest();

    public void setLastRadiusRequest(JRadiusRequest lastRadiusRequest);
}
