/**
 * JRadiusSimulator
 * Copyright (C) 2004-2005 PicoPoint, B.V.
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

package net.jradius.client.gui;

import java.io.Serializable;

/**
 * An Entry in the JRadiusSimulator Attributes Table.
 * @author David Bird
 */
public class AttributesTableEntry implements Serializable {
    
    private static final long serialVersionUID = (long)0;
    
    private String attributeName = "";
    private String attributeValue = "";
    private Boolean accessRequest = Boolean.FALSE;
    private Boolean tunnelRequest = Boolean.FALSE;
    private Boolean accountingStart = Boolean.FALSE;
    private Boolean accountingUpdate = Boolean.FALSE;
    private Boolean accountingStop = Boolean.FALSE;
    private Boolean coaRequest = Boolean.FALSE;
    private Class valueClass;
    
    public AttributesTableEntry()
    {
    }

    /**
     * @return Returns the accessRequest.
     */
    public Boolean getAccessRequest() {
        return accessRequest;
    }

    /**
     * @param accessRequest The accessRequest to set.
     */
    public void setAccessRequest(Boolean accessRequest) {
        this.accessRequest = accessRequest;
    }

    /**
     * @return Returns the accountingStart.
     */
    public Boolean getAccountingStart() {
        return accountingStart;
    }

    /**
     * @param accountingStart The accountingStart to set.
     */
    public void setAccountingStart(Boolean accountingStart) {
        this.accountingStart = accountingStart;
    }

    /**
     * @return Returns the accountingUpdate.
     */
    public Boolean getAccountingUpdate() {
        return accountingUpdate;
    }

    /**
     * @param accountingUpdate The accountingUpdate to set.
     */
    public void setAccountingUpdate(Boolean accountingUpdate) {
        this.accountingUpdate = accountingUpdate;
    }

    /**
     * @return Returns the accountingStop.
     */
    public Boolean getAccountingStop() {
        return accountingStop;
    }

    /**
     * @return Returns the coaRequest.
     */
    public Boolean getCoARequest() {
        return coaRequest;
    }

    /**
     * @param accountingStop The accountingStop to set.
     */
    public void setAccountingStop(Boolean accountingStop) {
        this.accountingStop = accountingStop;
    }

    /**
     * @return Returns the attributeName.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName The attributeName to set.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * @return Returns the attributeValue.
     */
    public String getAttributeValue() {
        return attributeValue;
    }

    /**
     * @param attributeValue The attributeValue to set.
     */
    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    /**
     * @return Returns the tunnelRequest.
     */
    public Boolean getTunnelRequest() {
        return tunnelRequest;
    }

    /**
     * @param tunnelRequest The tunnelRequest to set.
     */
    public void setTunnelRequest(Boolean tunnelRequest) {
        this.tunnelRequest = tunnelRequest;
    }

    /**
     * @return Returns the valueClass.
     */
    public Class getValueClass() {
        return valueClass;
    }

    /**
     * @param valueClass The valueClass to set.
     */
    public void setValueClass(Class valueClass) {
        this.valueClass = valueClass;
    }

	public void setCoARequest(Boolean v) {
		this.coaRequest = v;
	}

};

