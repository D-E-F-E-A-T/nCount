package nCountServer;

import java.util.Date;

public class CounterUpdatorThread implements Runnable
{
	@Override
	public void run()
	{
		for(;;)
		{
			DataStore.updateMainCounter();
			DataStore.refDate = new Date(System.currentTimeMillis());
			try 
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

}
