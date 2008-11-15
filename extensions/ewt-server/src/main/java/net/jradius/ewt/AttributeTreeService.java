package net.jradius.ewt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.jradius.ewt.handler.AttributeSearchTree;

import org.json.JSONArray;
import org.json.JSONObject;

import com.coova.service.EWTService;

public class AttributeTreeService implements EWTService
{
    private AttributeSearchTree attributeSearchTree;

    public AttributeTreeService(AttributeSearchTree tree)
    {
    	attributeSearchTree = tree;
    }
    
    public String getServiceName() 
	{
		return "attributes";
	}

	public Object handle(Map<String, String> parameterMap, JSONObject jsonObject, Object sessionObject) throws Exception
    {
        JSONArray newarray = new JSONArray();

        String query = parameterMap.get("query");
        String limit = parameterMap.get("limit");

        List<String> results = new LinkedList<String>();

        int resultsLimit;
        try { resultsLimit = Integer.parseInt(limit); }
        catch (Exception e) { resultsLimit = 20; }

        attributeSearchTree.prefixSearch(query, results, resultsLimit);
        for (String suggestion : results)
        {
            JSONObject obj;
            newarray.put(newarray.length(), obj = new JSONObject());
            obj.put("suggest", suggestion);
        }
        
        return newarray;
    }
}
