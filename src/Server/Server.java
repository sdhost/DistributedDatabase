package Server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server extends java.rmi.server.UnicastRemoteObject implements DataOperationInterface, ServerCommunicationInterface{
	private Registry registry; //RMI registry for lookup the remote objects
	private State serverState = State.OFFLINE;
	private Random random = new Random();
	private long transactionCounter = 0;
	private int uniqueServerId;
	private int serialId = 0;//Should be less than 9,999
	private int shift = 4;
	private TransactionManager _tm;
	
	//For each new account in system, the primary key uid for it will be generated as
	// uid = uniqueServerId * 10^shift + serialId
	// Each time a new uid is generated, the serialId will be increased by one
	// Then we can have the unique primary key for all the tuples among all the server
	private Map<String, State> txnStates;// All the transaction states that the result is not returned
	private Map<String, Object> txnResult;// All the transaction results that the result is not returned
	private Map<String, Long> txnTime;//All the transaction creation time that the result is not returned
	private Map<Integer, State> heartbeatStates;
	public volatile int a = 999;
	public HeartMonitor heartMonitor;
	
	
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
        
        
        _tm = new TransactionManager();
        
        //Make sure the uid will be valid
        this.txnStates = new HashMap<String,State>();
        this.txnResult = new HashMap<String, Object>();
        this.txnTime = new HashMap<String,Long>();
        this.heartbeatStates = new HashMap<Integer, State>();
        heartMonitor = new HeartMonitor(this.heartbeatStates, serverId);
        Thread heartThread = new Thread(heartMonitor);
        heartThread.start();
	}


	@Override
	public State getTxnState(String gid) throws RemoteException {
		ServerGUI.log("Called getTxnState");
		return State.ONLINE;
	}


	@Override
	public State getServerState() throws RemoteException {
		return serverState;
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
		this.txnResult.put(gid, null);
		
		return gid;
	}
	
	@Override
	public String txnCreatingAccounts(int balance) throws RemoteException {
		
		_tm.txnCreatingAccounts(199);
		
		
		/*String gid = this.txnCreation();
		try {
						
			ServerGUI.log("Waiting to create account :: " + a);
			a = balance;
			Thread.sleep(100000);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		 */
		return null;
	}


	@Override
	public String txnCheckingBalance(int uid) throws RemoteException {
		String gid = this.txnCreation();
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String txnDeposit(int uid, double amount) throws RemoteException {
		String gid = this.txnCreation();
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String txnWithdraw(int uid, double amount) throws RemoteException {
		String gid = this.txnCreation();
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String txnTransfer(int uid1, int uid2, double amount)
			throws RemoteException {
		String gid = this.txnCreation();
		// TODO Auto-generated method stub
		return null;
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


	//Send message to other server with user specification protocol
	//Return some message for user
	@Override
	public String send(String message) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Long getTxnTime(String gid) throws RemoteException {
		if(this.txnTime.containsKey(gid))
			return this.txnTime.get(gid);
		else
			return null;
	}
}
