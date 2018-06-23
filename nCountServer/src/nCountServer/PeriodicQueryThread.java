package nCountServer;

import java.io.BufferedWriter;
import java.io.IOException;

public class PeriodicQueryThread implements Runnable
{
	
	Stopwatch st;
	BufferedWriter refwriter;
	Device device = null;
	
	public PeriodicQueryThread(BufferedWriter refwriter, Device device, Stopwatch st)
	{
		this.refwriter = refwriter;
		this.device = device;
		this.st = st;
		Chocolat.println("[" + st.elapsedTime() + "] Query thread initialized");
	}
	
	
	@Override
	public void run() 
	{
		boolean done = false;
        for(;;)
        {
            double time = System.currentTimeMillis() / 1000.0;
            if (time % 60.0 != 0)
            {
                done = false;
            }
            if (time % 60.0 == 0 && !done) 
            {
            	if (device.getAuthStatus())
            	{
            		try 
            		{
            			Chocolat.println("[" + st.elapsedTime() + "] Requesting information for device ID: " + device.getID() + ", MAC Address: " + device.getMAC());
            			refwriter.write("request_query\n");
            			refwriter.flush();
            			done = true;
            		}
            		catch (IOException e)
            		{
            			e.printStackTrace();
            		}
            	}
            }
        }

	}

}
