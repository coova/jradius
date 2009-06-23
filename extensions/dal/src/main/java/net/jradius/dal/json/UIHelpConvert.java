package net.jradius.dal.json;

import java.util.*;
import org.json.*;

import net.jradius.dal.dao.UIHelpDAO;
import net.jradius.dal.model.UIHelp;
import net.jradius.dal.model.UIHelpExample;
import net.jradius.dal.model.UIHelpExample.Criteria;


import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class UIHelpConvert extends JSONConverter<UIHelp>
{
    private final UIHelpDAO dao;

    public UIHelpConvert(UIHelpDAO dao)
    {
        this.dao = dao;
    }

    public UIHelp fromJSON(UIHelp uihelp, JSONObject jsonObject) throws EWTException
    {
        uihelp.setKeyid(convertString(jsonObject, "keyid", true));
        uihelp.setHelptext(convertString(jsonObject, "helptext", true));
        return uihelp;
    }

    public JSONObject toJSON(UIHelp uihelp, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("uid", uihelp.getId());
        jsonObject.put("keyid", uihelp.getKeyid());
        jsonObject.put("helptext", uihelp.getHelptext());
        return jsonObject;
    }

    public JSONObject toRefJSON(UIHelp uihelp, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name", beanField(uihelp, nameColumn, encodeId));
        jsonObject.put("value", uihelp.getId());
        jsonObject.put("uid", uihelp.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(UIHelp uihelp, String nameColumn, boolean encodeId, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest", beanField(uihelp, nameColumn, encodeId));
        jsonObject.put("uid", uihelp.getId());
        return jsonObject;
    }

    public void insertFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        jsonObject.put("uid", dao.insert(fromJSON(new UIHelp(), jsonObject)));
    }

    public void updateFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        Long id = jsonObject.optLong("uid");
        UIHelp record = dao.selectByPrimaryKey(id);
        if (record != null) dao.updateByPrimaryKey(fromJSON(record, jsonObject));
    }

    public void deleteFromJSON(JSONObject jsonObject, Object sessionObject) throws EWTException, JSONException
    {
        dao.deleteByPrimaryKey(jsonObject.getLong("uid"));
    }

    public void listAsArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, Integer startRow, Integer rowCount, String orderByClause, String groupByClause) throws JSONException
    {
        UIHelpExample example = new UIHelpExample();
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
        List<UIHelp> list = dao.selectByExample(example);
        for (UIHelp uihelp : list)
        {
            array.put(array.length(), toJSON(uihelp, new JSONObject()));
        }
    }

    public void listAsRefArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        UIHelpExample example = new UIHelpExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<UIHelp> list = dao.selectByExample(example);
        for (UIHelp uihelp : list)
        {
            array.put(array.length(), toRefJSON(uihelp, nameColumn, encodeId, new JSONObject()));
        }
    }

    public void listAsSuggestArray(JSONArray array, Map<String, String> requestMap, JSONObject metaObject, Object sessionObject, String nameColumn, boolean encodeId, String orderByClause, String groupByClause) throws JSONException
    {
        UIHelpExample example = new UIHelpExample();
        if (requestMap != null)
        {
            Criteria criteria = example.createCriteria();
            criteriaBeanMapper(criteria, requestMap);
        }
        example.setOrderByClause(orderByClause);
        example.setGroupByClause(groupByClause);
        List<UIHelp> list = dao.selectByExample(example);
        for (UIHelp uihelp : list)
        {
            array.put(array.length(), toSuggestJSON(uihelp, nameColumn, encodeId, new JSONObject()));
        }
    }

    public String getActualColumnName(String name)
    {
        if ("uid".equals(name)) return "id";
        return name;
    }

}
