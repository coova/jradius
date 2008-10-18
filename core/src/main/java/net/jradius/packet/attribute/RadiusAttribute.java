/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (c) 2006-2007 David Bird <david@coova.com>
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

import java.io.Serializable;

import net.jradius.packet.attribute.value.AttributeValue;

/**
 * The RADIUS Attribute. All radius attributes (as build by RadiusDictionary)
 * are derived from this abstract class.
 *
 * @author David Bird
 */
public abstract class RadiusAttribute implements Serializable
{
    private static final long serialVersionUID = 0L;

    protected long attributeType = 0;
    protected int attributeOp = Operator.SET;
    protected AttributeValue attributeValue = null;
    protected String attributeName = "Unknown Attribute";
    
    public RadiusAttribute() 
    { 
    }
 
    abstract public void setup();
    
    protected void setup(Serializable value)
    {
    		setup(value, Operator.SET);
    }

    protected void setup(Serializable o, int op) 
    { 
        setup();
        attributeOp = op;
        if (o == null) return;

        if (o instanceof AttributeValue)
        {
            attributeValue = (AttributeValue) o;
        }
        else
        {
            attributeValue.setValueObject(o);
        }
    }

    /**
     * @return Returns the attribute type
     */
    public long getType()
    {
        return attributeType;
    }

    /**
     * @return Returns the (internal) formatted attribute type
     */
    public long getFormattedType()
    {
        return attributeType;
    }

    /**
     * @return Returns the AttributeValue
     */
    public AttributeValue getValue()
    {
        return attributeValue;
    }
    
    /**
     * @return Returns the name of the attribute
     */
    public String getAttributeName()
    {
        return attributeName;
    }
    
    /**
     * @return Returns the "operator" of the attribute
     */
    public int getAttributeOp()
    {
        return attributeOp;
    }
    
    /**
     * @param attributeOp The new attribute "operator" to be set
     */
    public void setAttributeOp(int attributeOp)
    {
        this.attributeOp = attributeOp;
    }
    
    /**
     * @param attributeOp The new attribute "operator" to be set
     */
    public void setAttributeOp(String attributeOp)
    {
        this.attributeOp = Operator.operatorFromString(attributeOp);
    }
    
    /**
     * @param b The new attribute value to be set
     */
    public void setValue(byte b[]) 
    {
        attributeValue.setValue(b);
    }

    /**
     * @param value The new attribute value to be set
     */
    public void setValue(String value) 
    {
        attributeValue.setValue(value);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return attributeName + " " + Operator.operatorToString(attributeOp) + " " + attributeValue.toString();
    }
    
    /**
     * The JRadius RadiusAttribute Operator Type.
     * @author David Bird
     */
    public final static class Operator
    {
        public static final int ADD           = 8;   /* += */
        public static final int SUB           = 9;   /* -= */
        public static final int SET           = 10;  /* := */
        public static final int EQ            = 11;  /* = */
        public static final int NE            = 12;  /* != */
        public static final int GE            = 13;  /* >= */
        public static final int GT            = 14;  /* > */
        public static final int LE            = 15;  /* <= */
        public static final int LT            = 16;  /* < */
        public static final int REG_EQ        = 17;  /* =~ */
        public static final int REG_NE        = 18;  /* !~ */
        public static final int CMP_TRUE      = 19;  /* =* */
        public static final int CMP_FALSE     = 20;  /* !* */
        public static final int CMP_EQ        = 21;  /* == */

        public static String operatorToString(int op)
        {
            switch(op)
            {
            	case ADD: 		 return "+=";
                case SUB:        return "-=";
                case SET:        return ":=";
                case EQ:         return "=";
                case NE:         return "!=";
                case GE:         return ">=";
                case GT:         return ">";
                case LE:         return "<=";
                case LT:         return "<";
                case REG_EQ:     return "=~";
                case REG_NE:     return "!~";
                case CMP_TRUE:   return "=*";
                case CMP_FALSE:  return "!*";
                case CMP_EQ:     return "==";
            }
            return "="; // for display purposes
        }

        public static int operatorFromString(String op)
        {
            if (op == null) return 0;
            if (op.equals("+=")) return ADD;
            if (op.equals("-=")) return SUB;
            if (op.equals(":=")) return SET;
            if (op.equals("="))  return EQ;
            if (op.equals("!=")) return NE;
            if (op.equals(">=")) return GE;
            if (op.equals(">"))  return GT;
            if (op.equals("<=")) return LE;
            if (op.equals("<"))  return LT;
            if (op.equals("=~")) return REG_EQ;
            if (op.equals("!~")) return REG_NE;
            if (op.equals("=*")) return CMP_TRUE;
            if (op.equals("!*")) return CMP_FALSE;
            if (op.equals("==")) return CMP_EQ;
            return 0;
        }
    }    
}
