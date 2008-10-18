package net.jradius.ewt.handler;

public class SQLUser
{
	private String realm;
    private String username;
    private String password;
    private String group;

    
    public String getRealm() 
    {
		return realm;
	}

	public void setRealm(String realm) 
	{
		this.realm = realm;
	}

	public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

	public String getPassword() 
	{
		return password;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}

	public String getGroup() 
	{
		return group;
	}

	public void setGroup(String group) 
	{
		this.group = group;
	}
}
