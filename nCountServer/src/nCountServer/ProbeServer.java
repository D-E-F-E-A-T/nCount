package nCountServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
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

	@SuppressWarnings("deprecation")
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
			Thread querythread = null;
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
								DataStore.incrementNumSensorsOnline();
								if (!DataStore.idExists(id))
								{
									DataStore.incrementNumSensors();
								}
								d = new Device(id, mac);
								d.setAuthenticated(true);
								querythread = new Thread(new PeriodicQueryThread(bw, sock, d, st));
								querythread.start();
								if (!DataStore.idExists(id))
								{
									DataStore.newID(id);
									Chocolat.println("[" + st.elapsedTime() + "] New Device Connected! Bonjour, allons-y! ID: " + d.getID() + ", MAC Address: " + d.getMAC());
								}
								else
								{
									Chocolat.println("[" + st.elapsedTime() + "] Existing Device Reconnected! ID: " + d.getID() + ", MAC Address: " + d.getMAC());
								}
								DataStore.deviceList.add(d);
								DataStore.updateDeviceList();
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
							else if (receiveMessage.contains("add_num_people"))
							{
								try
								{
									stok = new StringTokenizer(receiveMessage);
									stok.nextToken();
									int numAdd = Integer.parseInt(stok.nextToken());
									DataStore.alterPeopleCounter(d.getID(), numAdd);
									// TODO: Once the MCU receives the query_success command, it should reset its' own relative count to zero.
									bw.write("query_success\n");
									bw.flush();
								}
								catch (Exception e)
								{
									Chocolat.println("[" + st.elapsedTime() + "] ProbeServerThread query update failed: " + e);
									bw.write("query_failed\n");
									bw.flush();
								}
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
				DataStore.decrementNumSensorsOnline();
				// DataStore.destroyID(d.getID());
				DataStore.deviceList.remove(d);
				DataStore.updateDeviceList();
				querythread.stop();
				sock.close();
				Chocolat.println("[" + st.elapsedTime() + "] Sensor disconnected with ID: " + d.getID() + ", MAC Address: " + d.getMAC());
			}
		}
		catch (IOException ioe)
		{
			Chocolat.println("[" + st.elapsedTime() + "] ProbeServerThread failed: " + ioe);
		}
		
	}
}