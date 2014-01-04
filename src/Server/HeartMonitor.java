package Server;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HeartMonitor implements Runnable{
	public Map<Integer, State> heartbeatStates;
	public int serverId;
	public long period = 500;
	public Registry registry;
	public ServerCommunicationInterface rmiServer;
	public Configuration conf;
	public Server server;
	
	public HeartMonitor(Server s) throws IOException{
		this.server = s;
		this.heartbeatStates = s.heartbeatStates;
		this.conf= Configuration.fromFile("conf.txt");
		this.serverId = s.getServerID();
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
										if(objState.equals(  State.OFFLINE)){
											heartbeatStates.put(id, State.OFFLINE);
											
											ArrayList<String> target = new ArrayList<String>();
											for(Entry<String, Integer> txn: server.get_tm()._participantTxn.entrySet()){
												if(txn.getValue() == id){
													String gid = txn.getKey();
													State logState = server.multiTxnState.unfinishedTxn.get(gid);
													
													try {
														if(logState == State.TPCSTART){
															server.multiTxnState.unfinishedTxn.put(txn.getKey(), State.TPCABORT);
															server.get_tm().abort(txn.getKey());
															target.add(txn.getKey());
														}
													} catch (Exception e1) {
														e1.printStackTrace();
													}
												}
											}
											for(String gid: target){
												server.get_tm()._participantTxn.remove(gid);
												for(ProcessedTransaction pt : server.get_tm()._processedMultiSiteTxn){
													if(pt.getGid().equals(gid)){
														pt.setState(State.TPCABORT);
													}
												}
											}
										}else{
												heartbeatStates.put(id, State.ONLINE);
										}
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
