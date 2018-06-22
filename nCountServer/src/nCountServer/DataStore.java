package nCountServer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

public class DataStore 
{
	public static final String VERSION_ID = "0.1-nightly";
	
	private static int _numSensors = 0;
	private static int _numTriggers = 0;
	
	private static HashMap<Integer, Integer> IDMap = new HashMap<Integer, Integer>();
	
	public static ArrayList<Device> deviceList = new ArrayList<Device>();
	
	public static String[] COUNT;
	public static String mainCounter;
	
	public static String deviceListString = "No sensors connected";
	
	public static Date refDate = null;

	public static int getNumSensors()
	{
		return _numSensors;
	}
	
	public static void setNumSensors(int numSensors)
	{
		_numSensors = numSensors;
	}
	
	public static void incrementNumSensors()
	{
		_numSensors++;
	}
	
	public static void decrementNumSensors()
	{
		if (_numSensors != 0)
		{
			_numSensors--;
		}
	}
	
	public static int getNumTriggers()
	{
		return _numTriggers;
	}
	
	public static void setNumTriggers(int numTriggers)
	{
		_numTriggers = numTriggers;
	}
	
	public static void incrementNumTriggers()
	{
		_numTriggers++;
	}
	
	public static void decrementNumTriggers()
	{
		if (_numTriggers != 0)
		{
			_numTriggers--;
		}
	}
	
	public static boolean idExists(int id)
	{
		return IDMap.containsKey(id);
	}
	
	public static void newID(int id)
	{
		IDMap.put(id, 0);
	}
	
	public static int getLastKnownValue(int id)
	{
		return IDMap.get(id);
	}
	
	public static void setLastKnownValue(int id, int val)
	{
		IDMap.put(id, val);
	}
	
	public static void destroyID(int id)
	{
		IDMap.remove(id);
	}
	
	public static void updateMainCounter()
	{
		mainCounter = "";
		int number = _numTriggers;
		Stack<Integer> stack = new Stack<Integer>();
		while (number > 0) 
		{
		    stack.push(number % 10);
		    number = number / 10;
		}

		if (stack.isEmpty()) 
		{
			mainCounter += "<img src=\""+ DataStore.COUNT[0] + "\" alt=\"" + 0 + "\">" ;
		}
		else
		{
			while (!stack.isEmpty()) 
			{
			    // (stack.pop());
				int tmp = stack.pop();
				mainCounter += "<img src=\""+ DataStore.COUNT[tmp] + "\" alt=\"" + tmp + "\">" ;
			}
		}
	}
	
	public static void updateDeviceList()
	{
		if(deviceList.isEmpty())
		{
			deviceListString = "No sensors connected";
		}
		else
		{
			deviceListString = "ID       |         MAC<br>\n";
			for(Device d : deviceList)
			{
				deviceListString += d.getID() + "       |         " + d.getMAC() + "\n";
				deviceListString += "<br>";
			}
		}
	}
	
}
