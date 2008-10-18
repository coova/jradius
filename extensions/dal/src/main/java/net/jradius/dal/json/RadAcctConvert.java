package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.RadAcctDAO;
import net.jradius.dal.model.RadAcct;
import net.jradius.dal.model.RadAcctExample;
import net.jradius.dal.model.RadAcctExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class RadAcctConvert extends JSONConverter<RadAcct>
{
    private final RadAcctDAO dao;

    public RadAcctConvert(RadAcctDAO dao)
    {
        this.dao = dao;
    }

    public RadAcct fromJSON(RadAcct radacct, JSONObject jsonObject) throws EWTException
    {
        radacct.setAcctsessionid(convertString(jsonObject, "acctsessionid", false));
        radacct.setAcctuniqueid(convertString(jsonObject, "acctuniqueid", false));
        radacct.setUsername(convertString(jsonObject, "username", false));
        radacct.setGroupname(convertString(jsonObject, "groupname", false));
        radacct.setRealm(convertString(jsonObject, "realm", true));
        radacct.setNasipaddress(convertString(jsonObject, "nasipaddress", false));
        radacct.setNasportid(convertString(jsonObject, "nasportid", true));
        radacct.setNasporttype(convertString(jsonObject, "nasporttype", true));
        radacct.setAcctstarttime(stringToDate(jsonObject.optString("Acctstarttime"),null));
        radacct.setAcctstoptime(stringToDate(jsonObject.optString("Acctstoptime"),null));
        radacct.setAcctsessiontime(convertInteger(jsonObject, "acctsessiontime", true));
        radacct.setAcctauthentic(convertString(jsonObject, "acctauthentic", true));
        radacct.setConnectinfoStart(convertString(jsonObject, "connectinfoStart", true));
        radacct.setConnectinfoStop(convertString(jsonObject, "connectinfoStop", true));
        radacct.setAcctinputoctets(convertLong(jsonObject, "acctinputoctets", true));
        radacct.setAcctoutputoctets(convertLong(jsonObject, "acctoutputoctets", true));
        radacct.setCalledstationid(convertString(jsonObject, "calledstationid", false));
        radacct.setCallingstationid(convertString(jsonObject, "callingstationid", false));
        radacct.setAcctterminatecause(convertString(jsonObject, "acctterminatecause", false));
        radacct.setServicetype(convertString(jsonObject, "servicetype", true));
        radacct.setFramedprotocol(convertString(jsonObject, "framedprotocol", true));
        radacct.setFramedipaddress(convertString(jsonObject, "framedipaddress", false));
        radacct.setAcctstartdelay(convertInteger(jsonObject, "acctstartdelay", true));
        radacct.setAcctstopdelay(convertInteger(jsonObject, "acctstopdelay", true));
        radacct.setXascendsessionsvrkey(convertString(jsonObject, "xascendsessionsvrkey", true));
        return radacct;
    }

    public JSONObject toJSON(RadAcct radacct, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", radacct.getId());
        jsonObject.put("acctsessionid", radacct.getAcctsessionid());
        jsonObject.put("acctuniqueid", radacct.getAcctuniqueid());
        jsonObject.put("username", radacct.getUsername());
        jsonObject.put("groupname", radacct.getGroupname());
        jsonObject.put("realm", radacct.getRealm());
        jsonObject.put("nasipaddress", radacct.getNasipaddress());
        jsonObject.put("nasportid", radacct.getNasportid());
        jsonObject.put("nasporttype", radacct.getNasporttype());
        jsonObject.put("acctstarttime", radacct.getAcctstarttime());
        jsonObject.put("acctstoptime", radacct.getAcctstoptime());
        jsonObject.put("acctsessiontime", radacct.getAcctsessiontime());
        jsonObject.put("acctauthentic", radacct.getAcctauthentic());
        jsonObject.put("connectinfoStart", radacct.getConnectinfoStart());
        jsonObject.put("connectinfoStop", radacct.getConnectinfoStop());
        jsonObject.put("acctinputoctets", radacct.getAcctinputoctets());
        jsonObject.put("acctoutputoctets", radacct.getAcctoutputoctets());
        jsonObject.put("calledstationid", radacct.getCalledstationid());
        jsonObject.put("callingstationid", radacct.getCallingstationid());
        jsonObject.put("acctterminatecause", radacct.getAcctterminatecause());
        jsonObject.put("servicetype", radacct.getServicetype());
        jsonObject.put("framedprotocol", radacct.getFramedprotocol());
        jsonObject.put("framedipaddress", radacct.getFramedipaddress());
        jsonObject.put("acctstartdelay", radacct.getAcctstartdelay());
        jsonObject.put("acctstopdelay", radacct.getAcctstopdelay());
        jsonObject.put("xascendsessionsvrkey", radacct.getXascendsessionsvrkey());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadAcct radacct, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(radacct, nameColumn, encodeId));
        jsonObject.put("value", radacct.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadAcct radacct, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(radacct, nameColumn, encodeId));
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        dao.insert(fromJSON(new RadAcct(), jsonObject));
    }

    public void updateFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        RadAcct record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Integer startRow, Integer rowCount, String orderByClause) throws JSONException
    {
        RadAcctExample example = new RadAcctExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        if (metaObject != null)
        {
            metaObject.put("count", dao.countByExample(example));
        }
        example.setStartRow(startRow);
        example.setRowCount(rowCount);
        example.setOrderByClause(orderByClause);
        List<RadAcct> list = dao.selectByExample(example);
        for (RadAcct radacct : list)
        {
            array.put(array.length(), toJSON(radacct, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, String nameColumn, boolean encodeId, String orderByClause) throws JSONException
    {
        RadAcctExample example = new RadAcctExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        List<RadAcct> list = dao.selectByExample(example);
        for (RadAcct radacct : list)
        {
            array.put(array.length(), toRefJSON(radacct, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, String nameColumn, boolean encodeId, String orderByClause) throws JSONException
    {
        RadAcctExample example = new RadAcctExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        List<RadAcct> list = dao.selectByExample(example);
        for (RadAcct radacct : list)
        {
            array.put(array.length(), toSuggestJSON(radacct, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        if ("connectinfoStart".equals(name)) return "connectinfo_start";
        if ("connectinfoStop".equals(name)) return "connectinfo_stop";
        return name;
    }

}
