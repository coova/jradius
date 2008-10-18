/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006 David Bird <david@coova.com>
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

package net.jradius.standard;

import java.util.List;

import net.jradius.exception.StandardViolatedException;
import net.jradius.packet.RadiusPacket;

/**
 * Interface for RADIUS Standards Checking
 *
 * @author David Bird
 */
public abstract class RadiusStandard
{
    /**
     * @return Returns the name of the standard
     */
    public abstract String getName();
    
    /**
     * Checks the packet for compliance with the implemented standard. If there
     * are missing attributes, the StandardViolatedException is thrown containing
     * a list of the missing attributes.
     * @param p RadiusPacket to be checked
     * @param ignore Attribute types to ignore
     * @throws StandardViolatedException
     */
    public abstract void checkPacket(RadiusPacket p, long[] ignore) throws StandardViolatedException;

    /**
     * Same as checkPacket(p, null)
     * @param p RadiusPacket to be checked
     * @throws StandardViolatedException
     */
    public void checkPacket(RadiusPacket p) throws StandardViolatedException 
    {
        checkPacket(p, null);
    }

    /**
     * Check for missing attributes in a RadiusPacket
     * @param p RadiusPacket to be checked
     * @param list list to append missing attributes to
     * @param check attributes to look for
     * @param ignore attributes to ignore
     */
    protected static void checkMissing(RadiusPacket p, List list, long[] check, long[] ignore)
    {
	    for (int i=0; i < check.length; i++)
	    {
	        if (p.findAttribute(check[i]) == null)
	        {
	            if (ignore != null)
	                for (int j=0; j < ignore.length; j++)
	                    if (check[i] == ignore[j])
	                        continue;
                list.add(new Long(check[i]));
	        }
	    }
	}
}
