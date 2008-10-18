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

package net.jradius.realm;

import java.io.Serializable;

/**
 * Interface defining a RADIUS realm.
 * @author David Bird
 */
public interface JRadiusRealm extends Serializable
{
    public boolean isLocal();
    
    /**
     * @return Returns the acctPort.
     */
    public int getAcctPort();

    /**
     * @param acctPort The acctPort to set.
     */
    public void setAcctPort(int acctPort);

    /**
     * @return Returns the authPort.
     */
    public int getAuthPort();

    /**
     * @param authPort The authPort to set.
     */
    public void setAuthPort(int authPort);

    /**
     * @return Returns the realm.
     */
    public String getRealm();

    /**
     * @param realm The realm to set.
     */
    public void setRealm(String realm);

    /**
     * @return Returns the server.
     */
    public String getServer();

    /**
     * @param server The server to set.
     */
    public void setServer(String server);

    /**
     * @return Returns the sharedSecret.
     */
    public String getSharedSecret();

    /**
     * @param sharedSecret The sharedSecret to set.
     */
    public void setSharedSecret(String sharedSecret);

    /**
     * @return Returns the strip.
     */
    public int getStrip();

    /**
     * @param strip The strip to set.
     */
    public void setStrip(int strip);
    
    /**
     * @return Returns the source.
     */
    public String getSource();

    /**
     * @param source The source to set.
     */
    public void setSource(String source);

    /**
     * @return Returns the timeStamp.
     */
    public int getTimeStamp();

    /**
     * @param timeStamp The timeStamp to set.
     */
    public void setTimeStamp(int timeStamp);
}
