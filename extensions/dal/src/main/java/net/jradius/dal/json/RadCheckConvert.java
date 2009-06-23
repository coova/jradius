package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.RadCheckDAO;
import net.jradius.dal.model.RadCheck;
import net.jradius.dal.model.RadCheckExample;
import net.jradius.dal.model.RadCheckExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class RadCheckConvert extends JSONConverter<RadCheck>
{
    private final RadCheckDAO dao;

    public RadCheckConvert(RadCheckDAO dao)
    {
        this.dao = dao;
    }

    public RadCheck fromJSON(RadCheck radcheck, JSONObject jsonObject) throws EWTException
    {
        radcheck.setUsername(convertString(jsonObject, "username", false));
        radcheck.setAttribute(convertString(jsonObject, "attribute", false));
        radcheck.setOp(convertString(jsonObject, "op", false));
        radcheck.setValue(convertString(jsonObject, "value", false));
        return radcheck;
    }

    public JSONObject toJSON(RadCheck radcheck, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", radcheck.getId());
        jsonObject.put("username", radcheck.getUsername());
        jsonObject.put("attribute", radcheck.getAttribute());
        jsonObject.put("op", radcheck.getOp());
        jsonObject.put("value", radcheck.getValue());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadCheck radcheck, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(radcheck, nameColumn, encodeId));
        jsonObject.put("value", radcheck.getId());
        jsonObject.put("uid", radcheck.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadCheck radcheck, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(radcheck, nameColumn, encodeId));
        jsonObject.put("uid", radcheck.getId());
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        jsonObject.put("uid", dao.insert(fromJSON(new RadCheck(), jsonObject)));
    }

    public void updateFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        RadCheck record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, Integer startRow, Integer rowCount, String orderByClause, String groupByClause) throws JSONException
    {
        RadCheckExample example = new RadCheckExample();
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
        example.setGroupByClause(groupByClause);
        List<RadCheck> list = dao.selectByExample(example);
        for (RadCheck radcheck : list)
        {
            array.put(array.length(), toJSON(radcheck, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadCheckExample example = new RadCheckExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadCheck> list = dao.selectByExample(example);
        for (RadCheck radcheck : list)
        {
            array.put(array.length(), toRefJSON(radcheck, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadCheckExample example = new RadCheckExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadCheck> list = dao.selectByExample(example);
        for (RadCheck radcheck : list)
        {
            array.put(array.length(), toSuggestJSON(radcheck, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        return name;
    }

}
