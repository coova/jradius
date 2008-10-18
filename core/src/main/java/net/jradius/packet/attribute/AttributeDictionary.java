/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
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

package net.jradius.packet.attribute;

import java.util.Map;

/**
 * Attribute Dictionary Interface. Attribute dictionary classes, like
 * that built RadiusDictionary, implement this interface.
 *
 * @author David Bird
 */
public abstract interface AttributeDictionary
{
    // Some commonly used standard RADIUS Attribute types.
    // Of course, a dictionary supporing them must also be loaded.
    // Values are added as they are used in the jradius package (which
    // should not be referencing any attribute class directly).
    public static final int USER_NAME 				= 1;	// User-Name
    public static final int USER_PASSWORD 			= 2;	// User-Password
    public static final int STATE					= 24;	// State
    public static final int NAS_IDENTIFIER			= 32;	// NAS-Identifier
    public static final int ACCT_STATUS_TYPE 		= 40;	// Acct-Status-Type
    public static final int EAP_MESSAGE				= 79;	// EAP-Message
    public static final int MESSAGE_AUTHENTICATOR	= 80;	// Message-Authenticator
    
    public void loadVendorCodes(Map map);
    public void loadAttributes(Map map);
    public void loadAttributesNames(Map map);
}
