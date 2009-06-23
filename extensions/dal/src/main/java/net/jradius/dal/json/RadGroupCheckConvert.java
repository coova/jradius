package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.RadGroupCheckDAO;
import net.jradius.dal.model.RadGroupCheck;
import net.jradius.dal.model.RadGroupCheckExample;
import net.jradius.dal.model.RadGroupCheckExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class RadGroupCheckConvert extends JSONConverter<RadGroupCheck>
{
    private final RadGroupCheckDAO dao;

    public RadGroupCheckConvert(RadGroupCheckDAO dao)
    {
        this.dao = dao;
    }

    public RadGroupCheck fromJSON(RadGroupCheck radgroupcheck, JSONObject jsonObject) throws EWTException
    {
        radgroupcheck.setGroupname(convertString(jsonObject, "groupname", false));
        radgroupcheck.setAttribute(convertString(jsonObject, "attribute", false));
        radgroupcheck.setOp(convertString(jsonObject, "op", false));
        radgroupcheck.setValue(convertString(jsonObject, "value", false));
        return radgroupcheck;
    }

    public JSONObject toJSON(RadGroupCheck radgroupcheck, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", radgroupcheck.getId());
        jsonObject.put("groupname", radgroupcheck.getGroupname());
        jsonObject.put("attribute", radgroupcheck.getAttribute());
        jsonObject.put("op", radgroupcheck.getOp());
        jsonObject.put("value", radgroupcheck.getValue());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadGroupCheck radgroupcheck, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(radgroupcheck, nameColumn, encodeId));
        jsonObject.put("value", radgroupcheck.getId());
        jsonObject.put("uid", radgroupcheck.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadGroupCheck radgroupcheck, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(radgroupcheck, nameColumn, encodeId));
        jsonObject.put("uid", radgroupcheck.getId());
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        jsonObject.put("uid", dao.insert(fromJSON(new RadGroupCheck(), jsonObject)));
    }

    public void updateFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        RadGroupCheck record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, Integer startRow, Integer rowCount, String orderByClause, String groupByClause) throws JSONException
    {
        RadGroupCheckExample example = new RadGroupCheckExample();
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
        List<RadGroupCheck> list = dao.selectByExample(example);
        for (RadGroupCheck radgroupcheck : list)
        {
            array.put(array.length(), toJSON(radgroupcheck, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadGroupCheckExample example = new RadGroupCheckExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadGroupCheck> list = dao.selectByExample(example);
        for (RadGroupCheck radgroupcheck : list)
        {
            array.put(array.length(), toRefJSON(radgroupcheck, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadGroupCheckExample example = new RadGroupCheckExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadGroupCheck> list = dao.selectByExample(example);
        for (RadGroupCheck radgroupcheck : list)
        {
            array.put(array.length(), toSuggestJSON(radgroupcheck, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        return name;
    }

}
