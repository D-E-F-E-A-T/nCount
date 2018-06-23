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
	private static HashMap<Integer, String> IDSpecificCounterMap = new HashMap<Integer, String>();
	
	public static ArrayList<Device> deviceList = new ArrayList<Device>();
	
	public static String[] COUNT;
	public static String mainCounter;
	
	public static String deviceListString = "No sensors connected";
	
	public static Date refDate = null;

	public static String getCounterString(int id)
	{
		return IDSpecificCounterMap.get(id);
	}
	
	public static void putCounterString(int id, int num)
	{
		IDSpecificCounterMap.put(id, getCounterFormattedString(num));
	}
	
	public static boolean existCounterString(int id)
	{
		return IDSpecificCounterMap.containsKey(id);
	}
	
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
	
	public static String getCounterFormattedString(int number)
	{
		String ret = "";
		Stack<Integer> stack = new Stack<Integer>();
		while (number > 0) 
		{
		    stack.push(number % 10);
		    number = number / 10;
		}

		if (stack.isEmpty()) 
		{
			ret += "<img src=\""+ DataStore.COUNT[0] + "\" alt=\"" + 0 + "\">" ;
		}
		else
		{
			while (!stack.isEmpty()) 
			{
				int tmp = stack.pop();
				ret += "<img src=\""+ DataStore.COUNT[tmp] + "\" alt=\"" + tmp + "\">" ;
			}
		}
		return ret;
	}
	
	public static void updateMainCounter()
	{
		mainCounter = "";
		mainCounter = getCounterFormattedString(_numTriggers);
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
	
	public static void alterPeopleCounter(int id, int amount)
	{
		if (idExists(id))
		{
			int curVal = IDMap.get(id);
			int curTotalTriggers = getNumTriggers();
			setLastKnownValue(id, curVal + amount);
			setNumTriggers(curTotalTriggers + amount);
			putCounterString(id, getLastKnownValue(id));
			System.out.println(getCounterFormattedString(id));
		}
	}
	
}
