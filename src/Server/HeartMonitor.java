package Server;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HeartMonitor implements Runnable{
	public Map<Integer, State> heartbeatStates;
	public int serverId;
	public long period = 3000;
	public Registry registry;
	public ServerCommunicationInterface rmiServer;
	public Configuration conf;
	
	public HeartMonitor(Map<Integer, State> heartbeatStates, int serverId) throws IOException{
		this.heartbeatStates = heartbeatStates;
		this.conf= Configuration.fromFile("conf.txt");
		this.serverId = serverId;
		//initialize each server to be off line
		for(Entry<Integer, String> e : conf.getAllServers().entrySet()){
			if(e.getKey() != this.serverId){
				heartbeatStates.put(e.getKey(), State.NONEXIST);
			}
		}

	}

	@Override
	public void run(){
		
		while(true){		
			try {
				Thread.sleep(period);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			//detect heart beats of the other servers 3 seconds
			for(Entry<Integer, String> e : conf.getAllServers().entrySet()){
				int id = e.getKey();
				if(id != serverId){
						String serverAddress = e.getValue().split(":")[0];
						int serverPort = Integer.valueOf(e.getValue().split(":")[1]);
						//System.out.println(serverPort);
						
						try {
										registry = LocateRegistry.getRegistry(serverAddress, serverPort);
										rmiServer = (ServerCommunicationInterface)(registry.lookup("rmiServer"));

										State objState = rmiServer.heartBeat();
										if(objState.equals(  State.OFFLINE))
													heartbeatStates.put(id, State.OFFLINE);
										else
													heartbeatStates.put(id, State.ONLINE);
												
									} catch (RemoteException | NotBoundException e1) {
												//System.out.println(e1.getMessage());
												continue;
											}
					}
				}
									
			//System.out.println("Listening to hearbeats...");
				for(Entry<Integer, State> e : heartbeatStates.entrySet()){
					//System.out.println(e.getKey()+": "+e.getValue());
				}						
			
			
			
		}
		
		
		
		
	}

}