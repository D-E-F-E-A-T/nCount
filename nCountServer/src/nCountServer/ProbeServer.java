package nCountServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class ProbeServer
{
	public Stopwatch st;
	public ServerSocket ssock;
	public ProbeServer(Stopwatch st) throws IOException
	{
		this.st = st;
		Chocolat.println("[" + st.elapsedTime() + "] Sensor Server Listener initialized.");
		ssock = new ServerSocket(27374);
	}
	public void run()
	{
	    Chocolat.println("[" + st.elapsedTime() + "] Sensor Connection Server Listening...");
	    while (true) 
	    {
	    	Socket sock = null;
			try 
			{
				sock = ssock.accept();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
	    	Chocolat.println("[" + st.elapsedTime() + "] Sensor Connected.");
	        new Thread(new ProbeServerThread(sock, st)).start();
	    }
	}
}
class ProbeServerThread implements Runnable
{
	Socket sock;
	Stopwatch st;
	BufferedWriter bw;
	
	Thread thr;
	
	boolean isclosed = false;
	
	public ProbeServerThread(Socket sock, Stopwatch st)
	{
		this.sock = sock;
		this.st = st;
	}

	@Override
	public void run() 
	{
		try
		{
			// TODO: We need to start another thread on this socket to ask for the number of people counted periodically
			Device d = null;
			StringTokenizer stok = null;
			String receiveMessage;
			bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			InputStream istream = sock.getInputStream();
			BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));
			boolean isSatisfied = false;
			bw.write("reqauth\n");
			bw.newLine();
			bw.flush();
			for(;;)
			{
				try
				{	
					// System.out.println("A");
					if (sock.isClosed())
					{
						isclosed = true;
						break;
					}
					if (isclosed)
					{
						break;
					}
					receiveMessage = null;
					receiveMessage = receiveRead.readLine();
					if(receiveMessage != null)
					{
						// System.out.println("B");
						if (sock.isClosed())
						{
							isclosed = true;
							break;
						}
						if (isclosed)
						{
							break;
						}
						// Chocolat.println(receiveMessage);
						if(receiveMessage.contains("AUTH") && !isSatisfied)
						{
							// Message form: "AUTH [ID #] [LAST 4 DIGITS OF MAC]"
							stok = new StringTokenizer(receiveMessage);
							try
							{
								stok.nextToken();
								int id = Integer.parseInt(stok.nextToken());
								String mac = stok.nextToken();
								isSatisfied = true;
								bw.write("auth_successful\n");
								bw.flush();
								DataStore.incrementNumSensors();
								if(!DataStore.idExists(id))
								{
									d = new Device(id, mac);
									DataStore.newID(id);
									DataStore.deviceList.add(d);
									DataStore.updateDeviceList();
								}
							}
							catch (Exception e)
							{
								Chocolat.println("[" + st.elapsedTime() + "] ProbeServerThread auth failed: " + e);
								bw.write("auth_failed\n");
								bw.flush();
							}
						}
						if (!isSatisfied)
						{
							bw.write("reqauth\n");;
							bw.flush();
						}
						else
						{
							if (receiveMessage.matches("inc"))
							{
								DataStore.alterPeopleCounter(d.getID(), 1);
								bw.write("inc_done\n");
								bw.flush();
							}
							else if (receiveMessage.matches("dec"))
							{
								DataStore.alterPeopleCounter(d.getID(), -1);
								bw.write("dec_done\n");
								bw.flush();
							}
							else if (receiveMessage.matches("exit"))
							{
								bw.write("exiting\n");
								bw.newLine();
								bw.flush();
								isclosed = true;
								break;
							}
						}
					}
					else
					{
						isclosed = true;
						break;
					}
				}
				catch (Exception e)
				{
					Chocolat.println("[" + st.elapsedTime() + "] ProbeServerThread was interrupted: " + e);
					isclosed = true;
					e.printStackTrace();
					break;
				}
			}
			if (isclosed)
			{
				DataStore.decrementNumSensors();
				DataStore.destroyID(d.getID());
				DataStore.deviceList.remove(d);
				DataStore.updateDeviceList();
				sock.close();
			}
		}
		catch (IOException ioe)
		{
			Chocolat.println("[" + st.elapsedTime() + "] ProbeServerThread failed: " + ioe);
		}
		
	}
}