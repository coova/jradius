package net.jradius.session;

import net.jradius.dictionary.Attr_AcctInterimInterval;
import net.jradius.dictionary.Attr_IdleTimeout;
import net.jradius.dictionary.Attr_SessionTimeout;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.packet.attribute.AttributeList;

public class RadiusSessionSupportExt 
{
	public static void addAccessAcceptAttribtues(JRadiusSession session, AttributeList attrs)
    {
        String s;
        Long i;

        if ((s = session.getUsername()) != null)
        {
            attrs.remove(Attr_UserName.TYPE);
            attrs.add(new Attr_UserName(s));
        }
        if ((i = session.getSessionTimeout()) != null)
        {
            attrs.remove(Attr_SessionTimeout.TYPE);
            attrs.add(new Attr_SessionTimeout(i));
        }
        if ((i = session.getIdleTimeout()) != null && i.longValue() > 0)
        {
            attrs.remove(Attr_IdleTimeout.TYPE);
            attrs.add(new Attr_IdleTimeout(i));
        }
        if ((i = session.getInterimInterval()) != null && i.longValue() > 0)
        {
            attrs.remove(Attr_AcctInterimInterval.TYPE);
            attrs.add(new Attr_AcctInterimInterval(i));
        }
    }
}
