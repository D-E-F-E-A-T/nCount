package nCountServer;

public class Device implements Comparable<Device>
{
	private int IDNum;
	private String MAC_Addr;
	private boolean _isAuthenticated = false;
	
	public Device(int IDNum, String MAC_Addr)
	{
		this.IDNum = IDNum;
		this.MAC_Addr = MAC_Addr;
	}
	
	public int getID()
	{
		return IDNum;
	}
	
	public String getMAC()
	{
		return MAC_Addr;
	}
	
	public boolean getAuthStatus()
	{
		return _isAuthenticated;
	}
	
	public void setAuthenticated(boolean isAuthenticated)
	{
		_isAuthenticated = isAuthenticated;
	}
	
	@Override
	public String toString()
	{
		return IDNum + "";
	}
	
	@Override
	public int compareTo(Device o) 
	{
		return toString().compareTo(o.toString());
	}
	
}
