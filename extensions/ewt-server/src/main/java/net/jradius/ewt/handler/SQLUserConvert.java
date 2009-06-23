package net.jradius.ewt.handler;

import java.util.List;
import java.util.Map;

import net.jradius.dal.dao.RadCheckDAO;
import net.jradius.dal.model.RadCheck;
import net.jradius.dal.model.RadCheckExample;
import net.jradius.dal.model.RadCheckExample.Criteria;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coova.ewt.server.EWTException;
import com.coova.json.JSONConverter;

public class SQLUserConvert extends JSONConverter<SQLUser>
{
    private final RadCheckDAO dao;

    private static final String passwordAttribute = "Cleartext-Password";
    
    public SQLUserConvert(RadCheckDAO dao)
    {
        this.dao = dao;
    }

	public String getActualColumnName(String name) 
	{
		return name;
	}

	public SQLUser fromJSON(SQLUser user, JSONObject jsonObject)
    {
        user.setUsername(jsonObject.optString("username"));
        return user;
    }

    public JSONObject toJSON(SQLUser user, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("username", user.getUsername());
        return jsonObject;
    }

    public JSONObject toRefJSON(RadCheck radcheck, String nameColumn, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("name",  radcheck.getUsername());
        jsonObject.put("value", radcheck.getId());
        return jsonObject;
    }

    public JSONObject toSuggestJSON(RadCheck radcheck, String nameColumn, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("suggest",  radcheck.getUsername());
        return jsonObject;
    }

    public JSONObject toJSON(RadCheck radcheck, JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("username", radcheck.getUsername());
        return jsonObject;
    }

	@Override
	public void deleteFromJSON(JSONObject jsonObject, Object sessionObject)
			throws JSONException, EWTException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertFromJSON(JSONObject jsonObject, Object sessionObject)
			throws JSONException, EWTException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void listAsArray(JSONArray array, Map<String, String> requestMap,
			JSONObject metaObject, Object sessionObject, Integer startRow,
			Integer rowCount, String orderByClause, String groupByClause)
			throws JSONException {
        RadCheckExample example = new RadCheckExample();
        Criteria criteria = example.createCriteria();

        if (requestMap != null)
        {
            criteriaBeanMapper(criteria, requestMap);
        }
        
        if (metaObject != null)
        {
            metaObject.put("count", dao.countByExample(example));
        }
        
        example.createCriteria().andAttributeEqualTo(passwordAttribute);
        
        example.setStartRow(startRow);
        example.setRowCount(rowCount);
        example.setOrderByClause(orderByClause);
        
        List<RadCheck> list = dao.selectByExample(example);
        for (RadCheck radcheck : list)
        {
        	array.put(array.length(), toJSON(radcheck, new JSONObject()));
        }
	}

	@Override
	public void listAsRefArray(JSONArray array, Map<String, String> requestMap,
			JSONObject metaObject, Object sessionObject, String nameColumn,
			boolean encodeId, String orderByClause, String groupByClause)
			throws JSONException {
        RadCheckExample example = new RadCheckExample();
        Criteria criteria = example.createCriteria();

        if (requestMap != null)
        {
            criteriaBeanMapper(criteria, requestMap);
        }
        
        example.createCriteria().andAttributeEqualTo(passwordAttribute);
        
        example.setOrderByClause(orderByClause);
        
        List<RadCheck> list = dao.selectByExample(example);
        for (RadCheck radcheck : list)
        {
        	array.put(array.length(), toRefJSON(radcheck, nameColumn, new JSONObject()));
        }
	}

	@Override
	public void updateFromJSON(JSONObject jsonObject, Object sessionObject)
			throws JSONException, EWTException {
		// TODO Auto-generated method stub
		
	}
	
	public void listAsSuggestArray(JSONArray array,
			Map<String, String> requestMap, JSONObject metaObject,
			Object sessionObject, String nameColumn, boolean encodeId,
			String orderByClause, String groupByClause) throws JSONException {
        RadCheckExample example = new RadCheckExample();
        Criteria criteria = example.createCriteria();

        if (requestMap != null)
        {
            criteriaBeanMapper(criteria, requestMap);
        }
        
        example.createCriteria().andAttributeEqualTo(passwordAttribute);
        
        example.setOrderByClause(orderByClause);
        
        List<RadCheck> list = dao.selectByExample(example);
        for (RadCheck radcheck : list)
        {
        	array.put(array.length(), toSuggestJSON(radcheck, nameColumn, new JSONObject()));
        }
	}

}