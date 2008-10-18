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
import java.util.LinkedHashMap;

import net.jradius.exception.RadiusException;


/**
 * @author David Bird
 */
public class JRadiusRealmManager
{
    private static JRadiusRealmManager defaultManager;

    private LinkedHashMap<String,RealmFactory> factories = new LinkedHashMap<String,RealmFactory>();

    static
    {
        defaultManager = new JRadiusRealmManager();
    }

    public static JRadiusRealmManager getManager()
    {
        return defaultManager;
    }

    public void setRealmFactory(String name, RealmFactory factory)
    {
        factories.put(name, factory);
    }

    public RealmFactory getRealmFactory(Serializable name)
    {
        RealmFactory factory = factories.get(name);
        if (factory == null && name != null) factory = factories.get(null);
        return factory;
    }

    public JRadiusRealm getRealm(String realm) throws RadiusException
    {
    		for (RealmFactory factory : factories.values())
    		{
    			JRadiusRealm r = factory.getRealm(realm);
    			if (r != null) return r;
    		}
    		return null;
    }

    public static JRadiusRealm get(String requestor, String realm) throws RadiusException
    {
        return defaultManager.getRealmFactory(requestor).getRealm(realm);
    }

    public static JRadiusRealm get(String realm) throws RadiusException
    {
        return defaultManager.getRealm(realm);
    }
}
