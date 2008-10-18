package net.jradius.handler;

import java.util.List;

import net.jradius.dal.dao.RadCheckDAO;
import net.jradius.dal.dao.RadGroupCheckDAO;
import net.jradius.dal.dao.RadGroupReplyDAO;
import net.jradius.dal.dao.RadReplyDAO;
import net.jradius.dal.dao.RadUserGroupDAO;
import net.jradius.dal.model.RadCheck;
import net.jradius.dal.model.RadCheckExample;
import net.jradius.dal.model.RadGroupCheck;
import net.jradius.dal.model.RadGroupCheckExample;
import net.jradius.dal.model.RadGroupReply;
import net.jradius.dal.model.RadGroupReplyExample;
import net.jradius.dal.model.RadReply;
import net.jradius.dal.model.RadReplyExample;
import net.jradius.dal.model.RadUserGroup;
import net.jradius.dal.model.RadUserGroupExample;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;

public class SQLHandler extends RadiusSessionHandler 
{
    public boolean handle(JRadiusRequest request) throws Exception
    {
        RadiusPacket req = request.getRequestPacket();
        RadiusPacket rep = request.getReplyPacket();
        AttributeList ci = request.getConfigItems();
        
        switch(request.getType())
        {
	        case JRadiusServer.JRADIUS_authorize:
	        	return authorize(request, req, rep, ci);

	        case JRadiusServer.JRADIUS_post_auth:
	        	return post_auth(request, req, rep, ci);

	        case JRadiusServer.JRADIUS_accounting:
	        	return accounting(request, req, rep, ci);
        }
        
        return true;
    }

    protected boolean authorize(JRadiusRequest request, RadiusPacket req, RadiusPacket rep, AttributeList ci)
    {
        String username = (String) req.getAttributeValue(Attr_UserName.TYPE);

        /**
         *  Get 'Group' membership for the username.
         */
        RadUserGroupExample radUserGroupExample = new RadUserGroupExample();
        radUserGroupExample.createCriteria().andUsernameEqualTo(username);
        
        List<RadUserGroup> radUserGroupList = getRadUserGroupDAO(request).selectByExample(radUserGroupExample);
        for (RadUserGroup radUserGroup : radUserGroupList)
        {
        	try
        	{
                /**
                 *  Get 'Group' attributes for each group.
                 */
                RadGroupCheckExample radGroupCheckExample = new RadGroupCheckExample();
                radGroupCheckExample.createCriteria().andGroupnameEqualTo(radUserGroup.getGroupname());

                List<RadGroupCheck> radGroupCheckList = getRadGroupCheckDAO(request).selectByExample(radGroupCheckExample);
                for (RadGroupCheck radGroupCheck : radGroupCheckList)
                {
                	try
                	{
        	        	RadiusAttribute attribute = AttributeFactory.newAttribute(radGroupCheck.getAttribute(),radGroupCheck.getValue(),radGroupCheck.getOp());
        	        	ci.add(attribute, true);
                	}
                	catch (Exception e)
                	{
                		e.printStackTrace();
                	}
                }
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }

        /**
         *  Get 'Check' items for the username.
         */
        RadCheckExample radCheckExample = new RadCheckExample();
        radCheckExample.createCriteria().andUsernameEqualTo(username);

        List<RadCheck> radCheckList = getRadCheckDAO(request).selectByExample(radCheckExample);
        for (RadCheck radCheck : radCheckList)
        {
        	try
        	{
	        	RadiusAttribute attribute = AttributeFactory.newAttribute(radCheck.getAttribute(),radCheck.getValue(),radCheck.getOp());
	        	ci.add(attribute, false);
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }

        return false;
    }

    protected boolean post_auth(JRadiusRequest request, RadiusPacket req, RadiusPacket rep, AttributeList ci)
    {
    	boolean authorized = (rep != null && rep instanceof AccessAccept);
        String username = (String) req.getAttributeValue(Attr_UserName.TYPE);

        /**
         *   Only when authorized
         */
        if (!authorized) return false;
        
        /**
         *  Get 'Group' membership for the username.
         */
        RadUserGroupExample radUserGroupExample = new RadUserGroupExample();
        radUserGroupExample.createCriteria().andUsernameEqualTo(username);
        
        List<RadUserGroup> radUserGroupList = getRadUserGroupDAO(request).selectByExample(radUserGroupExample);
        for (RadUserGroup radUserGroup : radUserGroupList)
        {
        	try
        	{
                /**
                 *  Get 'Reply' attributes for each group.
                 */
                RadGroupReplyExample radGroupReplyExample = new RadGroupReplyExample();
                radGroupReplyExample.createCriteria().andGroupnameEqualTo(radUserGroup.getGroupname());

                List<RadGroupReply> radGroupReplyList = getRadGroupReplyDAO(request).selectByExample(radGroupReplyExample);
                for (RadGroupReply radGroupReply : radGroupReplyList)
                {
                	try
                	{
        	        	RadiusAttribute attribute = AttributeFactory.newAttribute(radGroupReply.getAttribute(),radGroupReply.getValue(),radGroupReply.getOp());
        	        	rep.addAttribute(attribute);
                	}
                	catch (Exception e)
                	{
                		e.printStackTrace();
                	}
                }
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }

        /**
         *  Get 'Reply' items for the username.
         */
        RadReplyExample radReplyExample = new RadReplyExample();
        radReplyExample.createCriteria().andUsernameEqualTo(username);

        List<RadReply> list = getRadReplyDAO(request).selectByExample(radReplyExample);
        for (RadReply radReply : list)
        {
        	try
        	{
	        	RadiusAttribute attribute = AttributeFactory.newAttribute(radReply.getAttribute(),radReply.getValue(),radReply.getOp());
	        	rep.addAttribute(attribute);
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }

        return false;
    }
    
    protected boolean accounting(JRadiusRequest request, RadiusPacket req, RadiusPacket rep, AttributeList ci)
    {
    	String sessionId = request.getSessionKey();
        String username = (String) req.getAttributeValue(Attr_UserName.TYPE);

        return false;
    }
    
    protected RadCheckDAO getRadCheckDAO(JRadiusRequest request)
    {
    	return (RadCheckDAO)request.getApplicationContext().getBean("radCheckDAO");
    }

    protected RadGroupCheckDAO getRadGroupCheckDAO(JRadiusRequest request)
    {
    	return (RadGroupCheckDAO)request.getApplicationContext().getBean("radGroupCheckDAO");
    }

    protected RadGroupReplyDAO getRadGroupReplyDAO(JRadiusRequest request)
    {
    	return (RadGroupReplyDAO)request.getApplicationContext().getBean("radGroupReplyDAO");
    }

    protected RadReplyDAO getRadReplyDAO(JRadiusRequest request)
    {
    	return (RadReplyDAO)request.getApplicationContext().getBean("radReplyDAO");
    }

    protected RadUserGroupDAO getRadUserGroupDAO(JRadiusRequest request)
    {
    	return (RadUserGroupDAO)request.getApplicationContext().getBean("radUserGroupDAO");
    }
}
