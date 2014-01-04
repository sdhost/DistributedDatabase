package Server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommitCoordinator implements Runnable{
	public Registry registry;
	public ServerCommunicationInterface rmiServer;
	public Configuration conf;
	public ConcurrentLinkedQueue<ProcessedTransaction> coordinatingTxn;
	public ConcurrentHashMap<String, ArrayList<Integer>> initiatedTxn;
	public MultiTxnState multiTxnState;
	public TransactionManager tm;
	public long waitTime = 1500;
	public Server server;
	public String gid;
	public volatile boolean isStop = false;
	
	public CommitCoordinator(TransactionManager _tm, Server server, String gid) throws IOException{
		this.multiTxnState = _tm.multiTxnState;
		this.coordinatingTxn = _tm._coordinatorTxn;
		this.conf= Configuration.fromFile("conf.txt");
		this.tm = _tm;
		this.initiatedTxn = _tm._initiatedTxn;
		this.server = server;
		this.gid = gid;
	}
	
	public void kill(){
		isStop = true;
	}
	
	@Override
	public void run(){
		
				ProcessedTransaction txn = coordinatingTxn.element();
				String gid = txn.getGid();
				
				//write prepare message to log
				multiTxnState.getUnfinishedTxn().put(gid, State.TPCPREPARE);
				TransactionManager.modalPopup(gid,"2PC is prepared to start");
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
									//if the participant failed, assume it returned abort
								if(isStop == true)
									return;
								State vote = rmiServer.replyVote(gid);
									if(vote == null){
										vote = State.PREABORT;
									}
									else if(vote == State.TPCWAIT){
										Thread.sleep(500);
										if(isStop == true)
											return;
										vote = rmiServer.replyVote(gid);
										if(vote == null || vote == State.TPCWAIT){
											vote = State.PREABORT;
										}
									}
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
					
					
					
					TransactionManager.modalPopup(gid,"coordinator made the vote decision");
					if(decision == State.TPCABORT){
						// write abort into log
						multiTxnState.getUnfinishedTxn().put(gid, State.TPCABORT);
						
					}
					else if(decision == State.TPCCOMMIT){
						//write commit into log
						multiTxnState.getUnfinishedTxn().put(gid, State.TPCCOMMIT);
					}
					if(isStop == true)
						return;
					TransactionManager.modalPopup(gid,"coordinator is waiting for acks");
					//announce other participants about the vote result
					ArrayList<Integer> ackList = new ArrayList<Integer>();
					for(int i=0; i<initiatedTxn.get(gid).size();i++){
						int cid = initiatedTxn.get(gid).get(i);
							String serverAddress = conf.getAllServers().get(cid).split(":")[0];
							int serverPort = Integer.valueOf(conf.getAllServers().get(cid).split(":")[1]);
							try {
								registry = LocateRegistry.getRegistry(serverAddress, serverPort);
								rmiServer = (ServerCommunicationInterface)(registry.lookup("rmiServer"));
								
								if(isStop == true)
									return;
								String ack = rmiServer.proceedVoteDecision(gid, decision);
								while(ack == null){
									Thread.sleep(500);
									ServerGUI.log("Waiting for ack from server: " + cid);
									
									if(isStop == true)
										return;
									ack = rmiServer.proceedVoteDecision(gid, decision);
								}
								
								ackList.add(cid);
							}catch (RemoteException | NotBoundException | InterruptedException e1) {continue;}
					}
					
					TransactionManager.modalPopup(gid,"coordinator got all the acks");
					//check if coordinator received ack from all participants
					if(ackList.size() == initiatedTxn.get(gid).size()){
						if(decision == State.TPCCOMMIT){
							TransactionManager.modalPopup(gid,"2PC wants to commit");
							tm.commit(gid);
							//write end of transaction into log
							multiTxnState.unfinishedTxn.remove(gid);
							multiTxnState.finishedTxn.put(gid, State.FINISHCOMMIT);
							TransactionManager.modalPopup(gid,"2PC commited");
						}
						else if(decision == State.TPCABORT){
							TransactionManager.modalPopup(gid,"2PC wants to abort");
							try {
								tm.abort(gid);
							} catch (Exception e) {
								e.printStackTrace();
							}
							//write end of transaction into log
							multiTxnState.unfinishedTxn.remove(gid);
							multiTxnState.finishedTxn.put(gid, State.FINISHABORT);
							TransactionManager.modalPopup(gid,"2PC aborted");
						}
						ServerGUI.log("2PC Finished with state:" + decision);
						
					}else{
						//this won't happen
					}
				//remove the finished TXN from coordinatorTxn
					ProcessedTransaction target = null;
					for(ProcessedTransaction t : this.coordinatingTxn){
						if(t.gid.equals(gid)){
							target = t;
							break;
						}
					}
					this.coordinatingTxn.remove(target);
		
	}
			
	}
