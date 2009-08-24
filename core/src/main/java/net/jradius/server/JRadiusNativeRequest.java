package net.jradius.server;

import net.jradius.server.JRadiusRequest;
import net.jradius.server.JRadiusServer;

public abstract class JRadiusNativeRequest extends JRadiusRequest
{
	private int type;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

    public String getTypeString()
    {
        switch(getType())
        {
            case JRadiusServer.JRADIUS_authenticate: return "authenticate";
            case JRadiusServer.JRADIUS_authorize:    return "authorize";
            case JRadiusServer.JRADIUS_preacct:      return "preacct";
            case JRadiusServer.JRADIUS_accounting:   return "accounting";
            case JRadiusServer.JRADIUS_checksimul:   return "checksimul";
            case JRadiusServer.JRADIUS_pre_proxy:    return "pre_proxy";
            case JRadiusServer.JRADIUS_post_proxy:   return "post_proxy";
            case JRadiusServer.JRADIUS_post_auth:    return "post_auth";
            default:                                 return "UNKNOWN";
        }
    }
}
