package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.RadUserGroupDAO;
import net.jradius.dal.model.RadUserGroup;
import net.jradius.dal.model.RadUserGroupExample;
import net.jradius.dal.model.RadUserGroupExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class RadUserGroupConvert extends JSONConverter<RadUserGroup>
{
    private final RadUserGroupDAO dao;

    public RadUserGroupConvert(RadUserGroupDAO dao)
    {
        this.dao = dao;
    }

    public RadUserGroup fromJSON(RadUserGroup radusergroup, JSONObject jsonObject) throws EWTException
    {
        radusergroup.setUsername(convertString(jsonObject, "username", false));
        radusergroup.setGroupname(convertString(jsonObject, "groupname", false));
        radusergroup.setPriority(convertInteger(jsonObject, "priority", false));
        return radusergroup;
    }

    public JSONObject toJSON(RadUserGroup radusergroup, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", radusergroup.getId());
        jsonObject.put("username", radusergroup.getUsername());
        jsonObject.put("groupname", radusergroup.getGroupname());
        jsonObject.put("priority", radusergroup.getPriority());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadUserGroup radusergroup, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(radusergroup, nameColumn, encodeId));
        jsonObject.put("value", radusergroup.getId());
        jsonObject.put("uid", radusergroup.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadUserGroup radusergroup, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(radusergroup, nameColumn, encodeId));
        jsonObject.put("uid", radusergroup.getId());
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        jsonObject.put("uid", dao.insert(fromJSON(new RadUserGroup(), jsonObject)));
    }

    public void updateFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        RadUserGroup record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, Integer startRow, Integer rowCount, String orderByClause, String groupByClause) throws JSONException
    {
        RadUserGroupExample example = new RadUserGroupExample();
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
        List<RadUserGroup> list = dao.selectByExample(example);
        for (RadUserGroup radusergroup : list)
        {
            array.put(array.length(), toJSON(radusergroup, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadUserGroupExample example = new RadUserGroupExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadUserGroup> list = dao.selectByExample(example);
        for (RadUserGroup radusergroup : list)
        {
            array.put(array.length(), toRefJSON(radusergroup, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        RadUserGroupExample example = new RadUserGroupExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<RadUserGroup> list = dao.selectByExample(example);
        for (RadUserGroup radusergroup : list)
        {
            array.put(array.length(), toSuggestJSON(radusergroup, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        return name;
    }

}
