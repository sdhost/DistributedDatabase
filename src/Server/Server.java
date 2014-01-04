package Server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends java.rmi.server.UnicastRemoteObject implements DataOperationInterface, ServerCommunicationInterface{
	private Registry registry; //RMI registry for lookup the remote objects
	private State serverState = State.OFFLINE;
	private Random random = new Random();
	private long transactionCounter = 0;
	private int uniqueServerId;
	private int serialId = 0;//Should be less than 9,999
	private int shift = 4;
	private TransactionManager _tm;
	private List<ServerCommunicationInterface> neighbour_server;
	public Configuration conf;

	
	//For each new account in system, the primary key uid for it will be generated as
	// uid = uniqueServerId * 10^shift + serialId
	// Each time a new uid is generated, the serialId will be increased by one
	// Then we can have the unique primary key for all the tuples among all the server
	private volatile ConcurrentHashMap<String, State> txnStates;// All the transaction states that the result is not returned
	private volatile ConcurrentHashMap<String, String> txnResult;// All the transaction results that the result is not returned
	private volatile ConcurrentHashMap<String, Long> txnTime;//All the transaction creation time that the result is not returned
	public Map<Integer, State> heartbeatStates;
	public volatile int a = 999;
	public HeartMonitor heartMonitor;
	public MultiTxnState multiTxnState;
	
	protected Server(String ip, int port, int serverId) throws IOException {
		super();

        try{
        	// create the registry, set serverId as the name, bind the server.
        	registry = LocateRegistry.createRegistry( port );
            registry.rebind("rmiServer", this);
        } catch (RemoteException e) {
        	serverState = State.OFFLINE;
        	throw e;
        }
        
        serverState = State.ONLINE;
        uniqueServerId = serverId;
        this.multiTxnState = new MultiTxnState();
        this.conf= Configuration.fromFile("conf.txt");
        _tm = new TransactionManager(uniqueServerId, multiTxnState);
        
        //Make sure the uid will be valid
        this.txnStates = new ConcurrentHashMap<String,State>();
        this.txnResult = new ConcurrentHashMap<String, String>();
        this.txnTime = new ConcurrentHashMap<String,Long>();
        this.heartbeatStates = new HashMap<Integer, State>();
        
        heartMonitor = new HeartMonitor(this);
        Thread heartThread = new Thread(heartMonitor);
        heartThread.start();
	}
	
	public void initialNeighbour(Configuration conf) {
		this.neighbour_server = new ArrayList<ServerCommunicationInterface>();
		for(Entry<Integer, String> e : conf.getAllServers().entrySet()){
			int id = e.getKey();
			if(id != this.uniqueServerId){
					String serverAddress = e.getValue().split(":")[0];
					int serverPort = Integer.valueOf(e.getValue().split(":")[1]);
					
					while (true) {
						try {
							registry = LocateRegistry.getRegistry(serverAddress, serverPort);	
							ServerCommunicationInterface rmiServer = (ServerCommunicationInterface)(registry.lookup("rmiServer"));
							this.neighbour_server.add(rmiServer);
							break;
						} catch (Exception ex) {
							try {
								System.out.println("Waiting for server " + serverAddress + ":" + serverPort + " to start");
								Thread.sleep(1000);	
							} catch (InterruptedException ee) {}
						}
					}
			}
		}
		
		this._tm.initNeighbours(neighbour_server);
	}


	@Override
	public State getTxnState(String gid) throws RemoteException {
		if (txnStates.containsKey(gid))
			return txnStates.get(gid); 
		return State.NONEXIST;
	}


	@Override
	public State getServerState() throws RemoteException {
		return serverState;
	}
	
	public void setServerState(State s) {
		serverState = s;
	}

	
	//**************************************************************
	//For each transactions, it should have the following operations
	// 1. Get a unique global transaction id: gid, Memorize the transaction creation timestamp
	// 2.1 Create the entry in txnState with initial state Processing
	// 2.2 Create the entry in txnResult with initial value of null
	// 3. Submit the queries one by one to scheduler
	// 4. Wait for scheduler return the result
	// 5.1 If OK, send commit request to TransactionManager
	// 5.2 Else, send abort request to TransactionManager
	// 6. Update the txnState and txnResult
	//**************************************************************
	
	
	//Act as action 1 to 2.2 of the above description
	//Should be executed before processing the queries
	private String txnCreation(){
		Long begin = System.currentTimeMillis();
		String gid = String.valueOf(begin) + String.valueOf(this.uniqueServerId);
		this.txnTime.put(gid, begin);
		this.txnStates.put(gid, State.PROCESSING);
		this.txnResult.put(gid, "");
		
		return gid;
	}
	
	private String nextUid(){
		String uid = String.valueOf(uniqueServerId * (int)Math.pow(10,shift) + serialId);
		serialId++;
		return uid;
	}
	
	@Override
	public String txnCreatingAccounts(int balance) throws RemoteException {
		if (serverState != State.ONLINE)
			return null;
		
		String gid = this.txnCreation();
		String uid = this.nextUid();
		_tm.txnCreatingAccounts(balance, gid, uid, this.txnTime.get(gid));

		this.txnStates.put(gid, State.FINISH);
		this.txnResult.put(gid, uid);
		return gid;
	}


	@Override
	public String txnCheckingBalance(String uid) throws RemoteException {
		if (serverState != State.ONLINE)
			return null;
		
		String gid = this.txnCreation();
		String balance = _tm.txnCheckingBalance(gid, uid, this.txnTime.get(gid));
		
		this.txnStates.put(gid, State.FINISH);
		this.txnResult.put(gid, balance);
		
		return gid;
	}


	@Override
	public String txnDeposit(String uid, int amount) throws RemoteException {
		if (serverState != State.ONLINE)
			return null;
		
		String gid = this.txnCreation();
		String balance = _tm.txnDeposit(gid,uid,amount, this.txnTime.get(gid));

		this.txnStates.put(gid, State.FINISH);
		this.txnResult.put(gid, balance);
		
		return gid;
	}


	@Override
	public String txnWithdraw(String uid, int amount) throws RemoteException {
		if (serverState != State.ONLINE)
			return null;
		
		String gid = this.txnCreation();
		String balance = _tm.txnWithdraw(gid,uid,amount, this.txnTime.get(gid));

		this.txnStates.put(gid, State.FINISH);
		this.txnResult.put(gid, balance);
		
		return gid;
	}


	@Override
	public String txnTransfer(String uid1, String uid2, int amount)	throws RemoteException {
		if (serverState != State.ONLINE)
			return null;
		
		String gid = this.txnCreation();
		String balance = _tm.txnTransfer(gid,uid1,uid2,amount, this.txnTime.get(gid));
		
		if(balance == null)
			this.txnStates.put(gid, State.ERROR);
		else
			this.txnStates.put(gid, State.FINISH);
		this.txnResult.put(gid, balance);
		
		return gid;
	}

	
	
	//To implement get State of transaction and get result of transaction,
	// the server need to have a map to hold the information of transactions
	// This information can be discarded after we call getTxnResult function if it is succeeded
	// Since the get result will return a raw object, it's the client's responsibility to
	// check which class the result is. 
	@Override
	public Object getTxnResult(String gid) throws RemoteException {
		if(this.txnResult.containsKey(gid) && this.txnStates.get(gid) != State.PROCESSING){
			//If the transaction state is not Processing, then we assume all the other works like lock release,
			// 2PC, logging and redo/undo is done for that transaction.
			Object result = this.txnResult.get(gid);
			this.txnResult.remove(gid);
			this.txnStates.remove(gid);
			this.txnTime.remove(gid);
			return result;
		}else if(this.txnResult.containsKey(gid)){		
			return new Exception("In Processing");
		}else{
			return new Exception("Transaction Not Exist");
		}
	}

	
	@Override
	public State heartBeat() throws RemoteException {
		return this.serverState;
	}
	//TODO: write start log for participant txn.
	@Override
	public State replyVote(String gid){
		if(serverState == State.OFFLINE)
			return null;
		
		ProcessedTransaction targetTxn = null;
		Iterator<ProcessedTransaction> it = _tm._processedMultiSiteTxn.iterator();
		while(it.hasNext()){
			ProcessedTransaction txn = it.next();
			if(txn.getGid() == gid)
				targetTxn = txn;
		}
		
		if(targetTxn == null){
			return State.TPCWAIT;
		}
		else{
			if(targetTxn.getState() == State.PREABORT){
				//write PREabort into log
				multiTxnState.unfinishedTxn.put(gid, State.PREABORT);
			}
			if(targetTxn.getState() == State.PRECOMMIT){
				// write PREcommit into log
				multiTxnState.unfinishedTxn.put(gid, State.PRECOMMIT);
			}
			return targetTxn.getState();
		}
		
	}
	
	//proceed the global vote decision
	@Override
	public String proceedVoteDecision(String gid, State decision){
		
		if(serverState == State.OFFLINE)
			return null;
		
		
		if(_tm._participantTxn.containsKey(gid)){
			if(decision == State.TPCCOMMIT){
				//write commit into log
				multiTxnState.unfinishedTxn.put(gid, State.TPCCOMMIT);
				_tm.commit(gid);
			}else if(decision == State.TPCABORT){
				//write abort into log
				try {
					multiTxnState.unfinishedTxn.put(gid, State.TPCABORT);
					_tm.abort(gid);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		//remove the finished transaction from partitipant txns
		_tm._participantTxn.remove(gid);
		return gid;
	}
	
	/**
	 * Recover from failure
	 */
	public void recoverServer(){
		//check all the unfinished multi-site txn
		for(Entry<String, State> txn : this.multiTxnState.unfinishedTxn.entrySet()){
			String gid = txn.getKey();
			State state = txn.getValue();
			boolean isCoordinator = false;
			
			for(ProcessedTransaction t: this._tm._coordinatorTxn){
				if(t.getGid() == gid){
					isCoordinator = true;
					break;
				}
			}
			
			//txn with this server as coordinator
			if(isCoordinator){
				
				if(state == State.TPCPREPARE){
					//do nothing	
				}
				
				if(state == State.TPCSTART){
					//do nothing	
				}
				
				if(state == State.TPCCOMMIT || state == State.TPCABORT){
					
					if(state == State.TPCABORT){
						try {
							//undo == abort
							this._tm.abort(gid);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					//get all acks to go into FINISH
					//announce other participants about the vote result
					ArrayList<Integer> ackList = new ArrayList<Integer>();
					for(int i=0; i<this._tm._initiatedTxn.get(gid).size();i++){
						int cid = this._tm._initiatedTxn.get(gid).get(i);
							String serverAddress = conf.getAllServers().get(cid).split(":")[0];
							int serverPort = Integer.valueOf(conf.getAllServers().get(cid).split(":")[1]);
							Registry registry;
							ServerCommunicationInterface rmiServer;
							try {
								registry = LocateRegistry.getRegistry(serverAddress, serverPort);
								rmiServer = (ServerCommunicationInterface)(registry.lookup("rmiServer"));
								
								String ack = rmiServer.proceedVoteDecision(gid, state);
								while(ack == null){
									Thread.sleep(500);
									ack = rmiServer.proceedVoteDecision(gid, state);
								}
								ackList.add(cid);
							}catch (RemoteException | NotBoundException | InterruptedException e1) {continue;}
					}
					//check if coordinator received acks from all participants
					if(ackList.size() == this._tm._initiatedTxn.get(gid).size()){
						multiTxnState.unfinishedTxn.remove(gid);
						multiTxnState.finishedTxn.put(gid, State.FINISH);
					}else{
						//this won't happen
					}
					
					//remove the finished txn
					for(ProcessedTransaction t : this._tm._coordinatorTxn){
						if(t.getGid() == gid){
							this._tm._coordinatorTxn.remove(t);
							break;
						}
					}
					
				}	
			}
			//txn with this server as participant
			else{
				
				//before vote
				if(state == State.TPCSTART){
					try {
						//undo == abort
						this._tm.abort(gid);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if(state == State.TPCABORT){
					try {
						//undo == abort
						this._tm.abort(gid);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				
				
				if(state == State.TPCCOMMIT){
					//redo == do nothing
				}
				
				if(state == State.PRECOMMIT || state == State.PREABORT){
					//consult the coordinator to see the final decision
					int coordinatorId = this._tm._participantTxn.get(gid);
					String address = conf.getAllServers().get(coordinatorId);
					String serverAddress = address.split(":")[0];
					int serverPort = Integer.valueOf(address.split(":")[1]);
					
					Registry registry;
					ServerCommunicationInterface rmiServer;
					try {
						registry = LocateRegistry.getRegistry(serverAddress, serverPort);
						rmiServer = (ServerCommunicationInterface)(registry.lookup("rmiServer"));
						
						MultiTxnState cMultiTxnState = rmiServer.getMultiTxnState();
						while(cMultiTxnState == null){
							Thread.sleep(500);
							cMultiTxnState = rmiServer.getMultiTxnState();
						}
						
						State finalDecision = cMultiTxnState.unfinishedTxn.get(gid);
						if(finalDecision == State.TPCCOMMIT){
							//redo == do nothing
						}
						
						if(finalDecision == State.TPCABORT){
							try {
								//undo == abort
								this._tm.abort(gid);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (RemoteException | NotBoundException | InterruptedException e) {e.printStackTrace();}
					
				}
			}
			
		}
	}
	//Send message to other server with user specification protocol
	//Return some message for user
	@Override
	public String send(String message) throws RemoteException {
		
		return null;
	}


	@Override
	public Long getTxnTime(String gid) throws RemoteException {
		if(this.txnTime.containsKey(gid))
			return this.txnTime.get(gid);
		else
			return null;
	}

	@Override
	public List<ResultSet> remoteExecute(List<Operation> ops, String gid, long timestamp, int sid) throws RemoteException {
		return this._tm.executeRemote(ops, gid, timestamp, sid);
		
	}

	@Override
	public boolean isExist(String tupleId) throws RemoteException {
		return this._tm.exists(tupleId);
	}

	@Override
	public int getServerID() throws RemoteException {
		return this.uniqueServerId;
	}

	@Override
	public MultiTxnState getMultiTxnState(){
		if(this.serverState == State.OFFLINE)
			return null;
		
		return multiTxnState;
	}
	
	public TransactionManager get_tm() {
		return _tm;
	}

	
}
