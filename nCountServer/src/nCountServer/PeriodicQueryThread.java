package nCountServer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class PeriodicQueryThread implements Runnable
{
	private Stopwatch st;
	private BufferedWriter refwriter;
	private Device device = null;
	private Socket socket;
	
	private String socketIPraw = "";
	private String socketIPfiltered = "";
	
	public PeriodicQueryThread(BufferedWriter refwriter, Socket socket, Device device, Stopwatch st)
	{
		this.refwriter = refwriter;
		this.socket = socket;
		this.device = device;
		this.st = st;
		socketIPraw = socket.getRemoteSocketAddress().toString();
		socketIPfiltered = socketIPraw.substring(1, socketIPraw.indexOf(":"));
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
            	try 
            	{
					if (!InetAddress.getByName(socketIPfiltered).isReachable(100))
					{
						socket.close();
						return;
					}
				} 
            	catch (IOException e1)
            	{
					e1.printStackTrace();
				}
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
