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

package net.jradius.handler.chain;

import org.apache.commons.chain.config.ConfigRuleSet;

/**
 * The JRadius ConfigRuleSet for Jakarta Commons Chain
 * @author David Bird
 */
public class JRConfigRuleSet extends ConfigRuleSet
{
    /**
     * Default constructor that configures the ConfigRuleSet with
     * our Catalog and Chain classes defined.
     */
    public JRConfigRuleSet()
    {
        setCatalogClass("net.jradius.handler.chain.JRCatalogBase");
        setChainClass("net.jradius.handler.chain.JRChainBase");
    }
}
