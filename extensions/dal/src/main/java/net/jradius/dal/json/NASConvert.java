package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.NASDAO;
import net.jradius.dal.model.NAS;
import net.jradius.dal.model.NASExample;
import net.jradius.dal.model.NASExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class NASConvert extends JSONConverter<NAS>
{
    private final NASDAO dao;

    public NASConvert(NASDAO dao)
    {
        this.dao = dao;
    }

    public NAS fromJSON(NAS nas, JSONObject jsonObject) throws EWTException
    {
        nas.setNasname(convertString(jsonObject, "nasname", false));
        nas.setNastype(convertString(jsonObject, "nastype", true));
        nas.setCalledstationid(convertString(jsonObject, "calledstationid", true));
        nas.setShortname(convertString(jsonObject, "shortname", true));
        nas.setPorts(convertInteger(jsonObject, "ports", true));
        nas.setSecret(convertString(jsonObject, "secret", true));
        nas.setCommunity(convertString(jsonObject, "community", true));
        nas.setDescription(convertString(jsonObject, "description", true));
        nas.setLatitude(convertDouble(jsonObject, "latitude", true));
        nas.setLongitude(convertDouble(jsonObject, "longitude", true));
        return nas;
    }

    public JSONObject toJSON(NAS nas, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", nas.getId());
        jsonObject.put("nasname", nas.getNasname());
        jsonObject.put("nastype", nas.getNastype());
        jsonObject.put("calledstationid", nas.getCalledstationid());
        jsonObject.put("shortname", nas.getShortname());
        jsonObject.put("ports", nas.getPorts());
        jsonObject.put("secret", nas.getSecret());
        jsonObject.put("community", nas.getCommunity());
        jsonObject.put("description", nas.getDescription());
        jsonObject.put("latitude", nas.getLatitude());
        jsonObject.put("longitude", nas.getLongitude());
        return jsonObject;
    }

    public JSONObject toRefJSON(NAS nas, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(nas, nameColumn, encodeId));
        jsonObject.put("value", nas.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(NAS nas, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(nas, nameColumn, encodeId));
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        dao.insert(fromJSON(new NAS(), jsonObject));
    }

    public void updateFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        NAS record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Integer startRow, Integer rowCount, String orderByClause) throws JSONException
    {
        NASExample example = new NASExample();
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
        List<NAS> list = dao.selectByExample(example);
        for (NAS nas : list)
        {
            array.put(array.length(), toJSON(nas, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, String nameColumn, boolean encodeId, String orderByClause) throws JSONException
    {
        NASExample example = new NASExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        List<NAS> list = dao.selectByExample(example);
        for (NAS nas : list)
        {
            array.put(array.length(), toRefJSON(nas, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, String nameColumn, boolean encodeId, String orderByClause) throws JSONException
    {
        NASExample example = new NASExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        List<NAS> list = dao.selectByExample(example);
        for (NAS nas : list)
        {
            array.put(array.length(), toSuggestJSON(nas, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        return name;
    }

}
