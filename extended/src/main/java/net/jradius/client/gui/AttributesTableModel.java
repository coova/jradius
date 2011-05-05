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

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import net.jradius.exception.RadiusException;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.RadiusAttribute;


/**
 * TableModel for the AttributesTable in the JRadiusClient.
 * @author David Bird
 */
public class AttributesTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = (long)0;
    
    private final String headers[] = { 
            "Attribute Name", 
            "AccessReq", 
            "TunnelReq", 
            "AcctStart", 
            "AcctUpdate", 
            "AcctStop", 
            "CoA", 
            "Attribute Value" 
    };
    
    private ArrayList entries = new ArrayList();
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int count) {
        return headers[count];
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return headers.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return entries.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
        AttributesTableEntry entry = (AttributesTableEntry)entries.get(row);
        switch(col) {
	        case 0: return entry.getAttributeName();
	        case 1: return entry.getAccessRequest();
	        case 2: return entry.getTunnelRequest();
	        case 3: return entry.getAccountingStart();
	        case 4: return entry.getAccountingUpdate();
	        case 5: return entry.getAccountingStop();
	        case 6: return entry.getCoARequest();
	        case 7: return entry.getAttributeValue();
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class getColumnClass(int col) {
        if (col == 0 || col == 7) return String.class;
        return Boolean.class;
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        if (col == 0) return false;
        return true;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object v, int row, int col) {
        AttributesTableEntry entry = (AttributesTableEntry)entries.get(row);
        switch(col) {
	        case 0: entry.setAttributeName((String)v); break;
	        case 1: entry.setAccessRequest((Boolean)v); break;
	        case 2: entry.setTunnelRequest((Boolean)v); break;
	        case 3: entry.setAccountingStart((Boolean)v); break;
	        case 4: entry.setAccountingUpdate((Boolean)v); break;
	        case 5: entry.setAccountingStop((Boolean)v); break;
	        case 6: entry.setCoARequest((Boolean)v); break;
	        case 7: entry.setAttributeValue((String)v); break;
        }
        fireTableCellUpdated(row, col);
    }

    public AttributesTableEntry addAttribute(String attributeName) throws RadiusException
    {
        RadiusAttribute attribute = AttributeFactory.newAttribute(attributeName);
        AttributesTableEntry entry = new AttributesTableEntry();
        entry.setAttributeName(attributeName);
        entry.setValueClass(attribute.getValue().getClass());
        entries.add(entry);
        return entry;
    }
    
    public AttributesTableEntry addAttribute(RadiusAttribute attribute) throws RadiusException
    {
        AttributesTableEntry entry = new AttributesTableEntry();
        entry.setAttributeName(attribute.getAttributeName());
        entry.setValueClass(attribute.getValue().getClass());
        entries.add(entry);
        return entry;
    }

    /**
     * @return Returns the entries.
     */
    public ArrayList getEntries()
    {
        return entries;
    }

    /**
     * @param entries The entries to set.
     */
    public void setEntries(ArrayList entries)
    {
        if (entries != null) this.entries = entries;
    }
}
