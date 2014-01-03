package Server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommitCoordinator implements Runnable{
	public Registry registry;
	public ServerCommunicationInterface rmiServer;
	public Configuration conf;
	public ConcurrentLinkedQueue<ProcessedTransaction> processedTxn;
	public HashMap<String, ArrayList<Integer>> initiatedTxn;
	public TransactionManager tm;
	public long waitTime = 1500;
	
	public CommitCoordinator(TransactionManager _tm) throws IOException{
		this.conf= Configuration.fromFile("conf.txt");
		this.tm = _tm;
		this.processedTxn = _tm._processedTxn;
		this.initiatedTxn = _tm._initiatedTxn;
	}
	
	@Override
	public void run(){
		while(true){
			if(processedTxn.size() == 0){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else{
				while(processedTxn.size()!=0){
						ProcessedTransaction txn = processedTxn.poll();
						String gid = txn.getGid();
						//select the current server as the 2PC coordinator
						if(initiatedTxn.containsKey(gid)){
							//TODO:write prepare message to log
							
							
							State decision = State.EMPTY;
							//initialized a vote pool to store the vote results
							ArrayList<State> votePool = new ArrayList<State>();
							votePool.add(txn.getState());
								
								//contact other participants for vote results
								for(int i=0; i<initiatedTxn.get(gid).size();i++){
									int cid = initiatedTxn.get(gid).get(i);
										String serverAddress = conf.getAllServers().get(cid).split(":")[0];
										int serverPort = Integer.valueOf(conf.getAllServers().get(cid).split(":")[1]);
										try {
											registry = LocateRegistry.getRegistry(serverAddress, serverPort);
											rmiServer = (ServerCommunicationInterface)(registry.lookup("rmiServer"));
											State vote;
											do{
												//if the participant failed, assume it returned abort
												vote = rmiServer.replyVote(gid);
												if(vote == State.OFFLINE){
													vote = State.PREABORT;
													break;
												}
												if(vote == State.TPCWAIT || vote == null){
													Thread.sleep(500);
												}
											}
											while(vote == State.TPCWAIT || vote == null);
											
											votePool.add(vote);
											//make decision after receiving all the vote resuts
											if(votePool.size() == initiatedTxn.get(gid).size() + 1){
												if(votePool.contains(State.PREABORT))
														decision = State.TPCABORT;
													else
														decision = State.TPCCOMMIT;
													
													break;
												}
									
										} catch (RemoteException | NotBoundException | InterruptedException e1) {continue;}
										
								}
								
								if(decision == State.TPCABORT){
									//TODO: write abort into log
									
								}
								else if(decision == State.TPCCOMMIT){
									//TODO: write commit into log
							
								}
								
								//announce other participants about the vote result
								ArrayList<Integer> ackList = new ArrayList<Integer>();
								for(int i=0; i<initiatedTxn.get(gid).size();i++){
									int cid = initiatedTxn.get(gid).get(i);
										String serverAddress = conf.getAllServers().get(cid).split(":")[0];
										int serverPort = Integer.valueOf(conf.getAllServers().get(cid).split(":")[1]);
										try {
											registry = LocateRegistry.getRegistry(serverAddress, serverPort);
											rmiServer = (ServerCommunicationInterface)(registry.lookup("rmiServer"));
											
											String ack = rmiServer.proceedVoteDecision(gid, decision);
											while(ack == null){
												Thread.sleep(500);
												ack = rmiServer.proceedVoteDecision(gid, decision);
											}
											ackList.add(cid);
										}catch (RemoteException | NotBoundException | InterruptedException e1) {continue;}
								}
								//check if coordinator received ack from all participants
								if(ackList.size() == initiatedTxn.get(gid).size()){
									if(decision == State.TPCCOMMIT){
										tm.commit(gid);
										//TODO:write end of transaction into log
									}
									else if(decision == State.TPCABORT){
										tm.abort(gid);
										//TODO:write end of transaction into log
									}
								}else{
									//this won't happen
								}
						}
				}
			}
			
		}
	}

}
