package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.RadPostAuthDAO;
import net.jradius.dal.model.RadPostAuth;
import net.jradius.dal.model.RadPostAuthExample;
import net.jradius.dal.model.RadPostAuthExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class RadPostAuthConvert extends JSONConverter<RadPostAuth>
{
    private final RadPostAuthDAO dao;

    public RadPostAuthConvert(RadPostAuthDAO dao)
    {
        this.dao = dao;
    }

    public RadPostAuth fromJSON(RadPostAuth radpostauth, JSONObject jsonObject) throws EWTException
    {
        radpostauth.setUsername(convertString(jsonObject, "username", false));
        radpostauth.setPass(convertString(jsonObject, "pass", false));
        radpostauth.setReply(convertString(jsonObject, "reply", false));
        radpostauth.setAuthdate(stringToDate(jsonObject.optString("Authdate"),new Date()));
        return radpostauth;
    }

    public JSONObject toJSON(RadPostAuth radpostauth, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", radpostauth.getId());
        jsonObject.put("username", radpostauth.getUsername());
        jsonObject.put("pass", radpostauth.getPass());
        jsonObject.put("reply", radpostauth.getReply());
        jsonObject.put("authdate", radpostauth.getAuthdate());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadPostAuth radpostauth, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(radpostauth, nameColumn, encodeId));
        jsonObject.put("value", radpostauth.getId());
        jsonObject.put("uid", radpostauth.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadPostAuth radpostauth, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(radpostauth, nameColumn, encodeId));
        jsonObject.put("uid", radpostauth.getId());
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        jsonObject.put("uid", dao.insert(fromJSON(new RadPostAuth(), jsonObject)));
    }

    public void updateFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        RadPostAuth record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, Integer startRow, Integer rowCount, String orderByClause, String groupByClause) throws JSONException
    {
        RadPostAuthExample example = new RadPostAuthExample();
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
        List<RadPostAuth> list = dao.selectByExample(example);
        for (RadPostAuth radpostauth : list)
        {
            array.put(array.length(), toJSON(radpostauth, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadPostAuthExample example = new RadPostAuthExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadPostAuth> list = dao.selectByExample(example);
        for (RadPostAuth radpostauth : list)
        {
            array.put(array.length(), toRefJSON(radpostauth, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadPostAuthExample example = new RadPostAuthExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadPostAuth> list = dao.selectByExample(example);
        for (RadPostAuth radpostauth : list)
        {
            array.put(array.length(), toSuggestJSON(radpostauth, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        return name;
    }

}
