package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.RadGroupReplyDAO;
import net.jradius.dal.model.RadGroupReply;
import net.jradius.dal.model.RadGroupReplyExample;
import net.jradius.dal.model.RadGroupReplyExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class RadGroupReplyConvert extends JSONConverter<RadGroupReply>
{
    private final RadGroupReplyDAO dao;

    public RadGroupReplyConvert(RadGroupReplyDAO dao)
    {
        this.dao = dao;
    }

    public RadGroupReply fromJSON(RadGroupReply radgroupreply, JSONObject jsonObject) throws EWTException
    {
        radgroupreply.setGroupname(convertString(jsonObject, "groupname", false));
        radgroupreply.setAttribute(convertString(jsonObject, "attribute", false));
        radgroupreply.setOp(convertString(jsonObject, "op", false));
        radgroupreply.setValue(convertString(jsonObject, "value", false));
        return radgroupreply;
    }

    public JSONObject toJSON(RadGroupReply radgroupreply, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", radgroupreply.getId());
        jsonObject.put("groupname", radgroupreply.getGroupname());
        jsonObject.put("attribute", radgroupreply.getAttribute());
        jsonObject.put("op", radgroupreply.getOp());
        jsonObject.put("value", radgroupreply.getValue());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadGroupReply radgroupreply, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(radgroupreply, nameColumn, encodeId));
        jsonObject.put("value", radgroupreply.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadGroupReply radgroupreply, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(radgroupreply, nameColumn, encodeId));
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        dao.insert(fromJSON(new RadGroupReply(), jsonObject));
    }

    public void updateFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        RadGroupReply record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Integer startRow, Integer rowCount, String orderByClause) throws JSONException
    {
        RadGroupReplyExample example = new RadGroupReplyExample();
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
        List<RadGroupReply> list = dao.selectByExample(example);
        for (RadGroupReply radgroupreply : list)
        {
            array.put(array.length(), toJSON(radgroupreply, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, String nameColumn, boolean encodeId, String orderByClause) throws JSONException
    {
        RadGroupReplyExample example = new RadGroupReplyExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        List<RadGroupReply> list = dao.selectByExample(example);
        for (RadGroupReply radgroupreply : list)
        {
            array.put(array.length(), toRefJSON(radgroupreply, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, String nameColumn, boolean encodeId, String orderByClause) throws JSONException
    {
        RadGroupReplyExample example = new RadGroupReplyExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        List<RadGroupReply> list = dao.selectByExample(example);
        for (RadGroupReply radgroupreply : list)
        {
            array.put(array.length(), toSuggestJSON(radgroupreply, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        return name;
    }

}
