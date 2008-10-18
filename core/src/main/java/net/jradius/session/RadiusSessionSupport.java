package net.jradius.session;

public class RadiusSessionSupport
{
    /**
     * Split the User-Name into username plus realm (checks for prefix realm
     * first - realm/username, then postfix realms - username@realm)
     * @param username The username to parse
     * @return Returns an array of 2 Strings { username, realm } if a realm
     * is found, otherwise it returns null
     */
    public static String[] splitUserName(String username)
    {
        int idx;
        
        if ((idx = username.indexOf("/")) > 0 ||
            (idx = username.indexOf("\\")) > 0)
        {
            // Prefix Realm - takes priority over Postfix
            return new String[]{ 
                    username.substring(idx + 1), 
                    username.substring(0, idx) };
        }
        
        if ((idx = username.lastIndexOf("@")) > 0)
        {
            // Postfix Realm
            return new String[]{ 
                    username.substring(0, idx), 
                    username.substring(idx + 1) };
        }
        
        return null;
    }
}
