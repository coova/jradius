package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.RadIPPoolDAO;
import net.jradius.dal.model.RadIPPool;
import net.jradius.dal.model.RadIPPoolExample;
import net.jradius.dal.model.RadIPPoolExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class RadIPPoolConvert extends JSONConverter<RadIPPool>
{
    private final RadIPPoolDAO dao;

    public RadIPPoolConvert(RadIPPoolDAO dao)
    {
        this.dao = dao;
    }

    public RadIPPool fromJSON(RadIPPool radippool, JSONObject jsonObject) throws EWTException
    {
        radippool.setPoolName(convertString(jsonObject, "poolName", false));
        radippool.setFramedipaddress(convertString(jsonObject, "framedipaddress", false));
        radippool.setNasipaddress(convertString(jsonObject, "nasipaddress", false));
        radippool.setCalledstationid(convertString(jsonObject, "calledstationid", false));
        radippool.setCallingstationid(convertString(jsonObject, "callingstationid", false));
        radippool.setExpiryTime(stringToDate(jsonObject.optString("ExpiryTime"),null));
        radippool.setUsername(convertString(jsonObject, "username", false));
        radippool.setPoolKey(convertString(jsonObject, "poolKey", false));
        return radippool;
    }

    public JSONObject toJSON(RadIPPool radippool, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", radippool.getId());
        jsonObject.put("poolName", radippool.getPoolName());
        jsonObject.put("framedipaddress", radippool.getFramedipaddress());
        jsonObject.put("nasipaddress", radippool.getNasipaddress());
        jsonObject.put("calledstationid", radippool.getCalledstationid());
        jsonObject.put("callingstationid", radippool.getCallingstationid());
        jsonObject.put("expiryTime", radippool.getExpiryTime());
        jsonObject.put("username", radippool.getUsername());
        jsonObject.put("poolKey", radippool.getPoolKey());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadIPPool radippool, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(radippool, nameColumn, encodeId));
        jsonObject.put("value", radippool.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadIPPool radippool, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(radippool, nameColumn, encodeId));
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        dao.insert(fromJSON(new RadIPPool(), jsonObject));
    }

    public void updateFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        RadIPPool record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Integer startRow, Integer rowCount, String orderByClause) throws JSONException
    {
        RadIPPoolExample example = new RadIPPoolExample();
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
        List<RadIPPool> list = dao.selectByExample(example);
        for (RadIPPool radippool : list)
        {
            array.put(array.length(), toJSON(radippool, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, String nameColumn, boolean encodeId, String orderByClause) throws JSONException
    {
        RadIPPoolExample example = new RadIPPoolExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        List<RadIPPool> list = dao.selectByExample(example);
        for (RadIPPool radippool : list)
        {
            array.put(array.length(), toRefJSON(radippool, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, String nameColumn, boolean encodeId, String orderByClause) throws JSONException
    {
        RadIPPoolExample example = new RadIPPoolExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        List<RadIPPool> list = dao.selectByExample(example);
        for (RadIPPool radippool : list)
        {
            array.put(array.length(), toSuggestJSON(radippool, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        if ("poolName".equals(name)) return "pool_name";
        if ("expiryTime".equals(name)) return "expiry_time";
        if ("poolKey".equals(name)) return "pool_key";
        return name;
    }

}
