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

package net.jradius.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * JRadius JAAS Principal.
 * @author David Bird
 */
public class JRadiusPrincipal implements Principal, Serializable
{
    private String userName;
    
    public JRadiusPrincipal(String userName)
    {
        if (userName == null) throw new NullPointerException("UserName set to null");
        this.userName = userName;
    }

    public String getName()
    {
        return userName;
    }
    
    public String toString() 
    {
        return("RadiusPrincipal: " + userName);
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof JRadiusPrincipal)) return false;
        JRadiusPrincipal that = (JRadiusPrincipal)o;
        if (getName().equals(that.getName())) return true;
        return false;
    }

    /**
     * Return a hash code for this <code>JRadiusPrincipal</code>.
     * <p>
     * @return a hash code for this <code>JRadiusPrincipal</code>.
     */
    public int hashCode() 
    {
        return userName.hashCode();
    }
}
