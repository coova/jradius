package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.RadReplyDAO;
import net.jradius.dal.model.RadReply;
import net.jradius.dal.model.RadReplyExample;
import net.jradius.dal.model.RadReplyExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class RadReplyConvert extends JSONConverter<RadReply>
{
    private final RadReplyDAO dao;

    public RadReplyConvert(RadReplyDAO dao)
    {
        this.dao = dao;
    }

    public RadReply fromJSON(RadReply radreply, JSONObject jsonObject) throws EWTException
    {
        radreply.setUsername(convertString(jsonObject, "username", false));
        radreply.setAttribute(convertString(jsonObject, "attribute", false));
        radreply.setOp(convertString(jsonObject, "op", false));
        radreply.setValue(convertString(jsonObject, "value", false));
        return radreply;
    }

    public JSONObject toJSON(RadReply radreply, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", radreply.getId());
        jsonObject.put("username", radreply.getUsername());
        jsonObject.put("attribute", radreply.getAttribute());
        jsonObject.put("op", radreply.getOp());
        jsonObject.put("value", radreply.getValue());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadReply radreply, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(radreply, nameColumn, encodeId));
        jsonObject.put("value", radreply.getId());
        jsonObject.put("uid", radreply.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadReply radreply, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(radreply, nameColumn, encodeId));
        jsonObject.put("uid", radreply.getId());
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        jsonObject.put("uid", dao.insert(fromJSON(new RadReply(), jsonObject)));
    }

    public void updateFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        RadReply record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, Integer startRow, Integer rowCount, String orderByClause, String groupByClause) throws JSONException
    {
        RadReplyExample example = new RadReplyExample();
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
        List<RadReply> list = dao.selectByExample(example);
        for (RadReply radreply : list)
        {
            array.put(array.length(), toJSON(radreply, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadReplyExample example = new RadReplyExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadReply> list = dao.selectByExample(example);
        for (RadReply radreply : list)
        {
            array.put(array.length(), toRefJSON(radreply, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadReplyExample example = new RadReplyExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadReply> list = dao.selectByExample(example);
        for (RadReply radreply : list)
        {
            array.put(array.length(), toSuggestJSON(radreply, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        return name;
    }

}
