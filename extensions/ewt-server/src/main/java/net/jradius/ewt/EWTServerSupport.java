package net.jradius.ewt;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.jradius.dal.dao.NASDAO;
import net.jradius.dal.dao.RadAcctDAO;
import net.jradius.dal.dao.RadCheckDAO;
import net.jradius.dal.dao.RadGroupCheckDAO;
import net.jradius.dal.dao.RadGroupReplyDAO;
import net.jradius.dal.dao.RadIPPoolDAO;
import net.jradius.dal.dao.RadPostAuthDAO;
import net.jradius.dal.dao.RadReplyDAO;
import net.jradius.dal.dao.RadUserGroupDAO;
import net.jradius.dal.dao.UIHelpDAO;
import net.jradius.dal.json.NASConvert;
import net.jradius.dal.json.RadAcctConvert;
import net.jradius.dal.json.RadCheckConvert;
import net.jradius.dal.json.RadGroupCheckConvert;
import net.jradius.dal.json.RadGroupReplyConvert;
import net.jradius.dal.json.RadIPPoolConvert;
import net.jradius.dal.json.RadPostAuthConvert;
import net.jradius.dal.json.RadReplyConvert;
import net.jradius.dal.json.RadUserGroupConvert;
import net.jradius.dal.json.UIHelpConvert;
import net.jradius.ewt.handler.AttributeSearchTree;
import net.jradius.ewt.handler.SQLUserConvert;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;

import com.coova.ewt.handler.JavaStatusService;
import com.coova.ewt.server.EWTResponse;
import com.coova.service.EWTService;

public class EWTServerSupport extends com.coova.ewt.server.EWTServerSupport implements InitializingBean 
{
    private NASDAO nasDAO;
    private RadAcctDAO radAcctDAO;
    private RadCheckDAO radCheckDAO;
    private RadGroupCheckDAO radGroupCheckDAO;
    private RadReplyDAO radReplyDAO;
    private RadGroupReplyDAO radGroupReplyDAO;
    private RadPostAuthDAO radPostAuthDAO;
    private RadUserGroupDAO radUserGroupDAO;
    private RadIPPoolDAO radIPPoolDAO;
    private UIHelpDAO uiHelpDAO;

    private AttributeSearchTree attributeSearchTree;

    public void afterPropertiesSet() throws Exception
    {
    	registerService("java-status", new JavaStatusService());

		// Extended FreeRADIUS Support
        registerConverter("user", new SQLUserConvert(getRadCheckDAO()));

    	registerService("attributes", new AttributeTreeService(attributeSearchTree));

    	registerService("radiusd-config", new EWTService()
        {
            public String getServiceName() {
				return "radiusd-config";
			}

			public Object handle(Map<String, String> parameterMap, JSONObject jsonObject, Object sessionObject) throws Exception
            {
                parameterMap.put("responseType", EWTResponse.txtType);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                doTransform("xml2radiusd", "config", out);
                return new String(out.toByteArray());
            }
        });

        registerConverter("uihelp", new UIHelpConvert(getUiHelpDAO()));
        registerConverter("nas", new NASConvert(getNasDAO()));
        registerConverter("radacct", new RadAcctConvert(getRadAcctDAO()));
        registerConverter("radcheck", new RadCheckConvert(getRadCheckDAO()));
        registerConverter("radgroupcheck", new RadGroupCheckConvert(getRadGroupCheckDAO()));
        registerConverter("radreply", new RadReplyConvert(getRadReplyDAO()));
        registerConverter("radgroupreply", new RadGroupReplyConvert(getRadGroupReplyDAO()));
        registerConverter("radpostauth", new RadPostAuthConvert(getRadPostAuthDAO()));
        registerConverter("radusergroup", new RadUserGroupConvert(getRadUserGroupDAO()));
        registerConverter("radippool", new RadIPPoolConvert(getRadIPPoolDAO()));
    }

    public NASDAO getNasDAO()
    {
        return nasDAO;
    }

    public void setNasDAO(NASDAO nasDAO)
    {
        this.nasDAO = nasDAO;
    }

    public RadCheckDAO getRadCheckDAO()
    {
        return radCheckDAO;
    }

    public void setRadCheckDAO(RadCheckDAO radCheckDAO)
    {
        this.radCheckDAO = radCheckDAO;
    }

    public RadAcctDAO getRadAcctDAO()
    {
        return radAcctDAO;
    }

    public void setRadAcctDAO(RadAcctDAO radAcctDAO)
    {
        this.radAcctDAO = radAcctDAO;
    }

    public RadGroupCheckDAO getRadGroupCheckDAO()
    {
        return radGroupCheckDAO;
    }

    public void setRadGroupCheckDAO(RadGroupCheckDAO radGroupCheckDAO)
    {
        this.radGroupCheckDAO = radGroupCheckDAO;
    }

    public RadGroupReplyDAO getRadGroupReplyDAO()
    {
        return radGroupReplyDAO;
    }

    public void setRadGroupReplyDAO(RadGroupReplyDAO radGroupReplyDAO)
    {
        this.radGroupReplyDAO = radGroupReplyDAO;
    }

    public RadReplyDAO getRadReplyDAO()
    {
        return radReplyDAO;
    }

    public void setRadReplyDAO(RadReplyDAO radReplyDAO)
    {
        this.radReplyDAO = radReplyDAO;
    }

    public RadPostAuthDAO getRadPostAuthDAO()
    {
        return radPostAuthDAO;
    }

    public void setRadPostAuthDAO(RadPostAuthDAO radPostAuthDAO)
    {
        this.radPostAuthDAO = radPostAuthDAO;
    }

    public RadUserGroupDAO getRadUserGroupDAO()
    {
        return radUserGroupDAO;
    }

    public void setRadUserGroupDAO(RadUserGroupDAO radUserGroupDAO)
    {
        this.radUserGroupDAO = radUserGroupDAO;
    }

    public RadIPPoolDAO getRadIPPoolDAO()
    {
        return radIPPoolDAO;
    }

    public void setRadIPPoolDAO(RadIPPoolDAO radIPPoolDAO)
    {
        this.radIPPoolDAO = radIPPoolDAO;
    }

    public AttributeSearchTree getAttributeSearchTree()
    {
        return attributeSearchTree;
    }

    public void setAttributeSearchTree(AttributeSearchTree attributeSearchTree)
    {
        this.attributeSearchTree = attributeSearchTree;
    }

	public UIHelpDAO getUiHelpDAO() {
		return uiHelpDAO;
	}

	public void setUiHelpDAO(UIHelpDAO uiHelpDAO) {
		this.uiHelpDAO = uiHelpDAO;
	}
}
