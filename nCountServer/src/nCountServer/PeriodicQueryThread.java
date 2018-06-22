package nCountServer;

import java.net.Socket;

public class PeriodicQueryThread implements Runnable
{

	Socket refsocket;
	
	public PeriodicQueryThread(Socket refsocket)
	{
		this.refsocket = refsocket;
	}
	
	
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		boolean done = false;
        for(;;)
        {
            double time = System.currentTimeMillis() / 1000.0;
            if (time % 60.0 != 0)
            {
                done = false;
            }
            if (time % 60.0 == 0  && !done) 
            {
            	
                done = true;
            }
        }

	}

}
