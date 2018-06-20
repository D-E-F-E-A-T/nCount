package nCountServer;

public class CounterUpdatorThread implements Runnable
{
	@Override
	public void run()
	{
		for(;;)
		{
			DataStore.updateMainCounter();
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
