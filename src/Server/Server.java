package Server;

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
	//For each new account in system, the primary key uid for it will be generated as
	// uid = uniqueServerId * 10^shift + serialId
	// Each time a new uid is generated, the serialId will be increased by one
	// Then we can have the unique primary key for all the tuples among all the server
	private Map<String, State> txnStates;// All the transaction states that the result is not returned
	private Map<String, Object> txnResult;// All the transaction results that the result is not returned
	
	
	protected Server(String ip, int port) throws RemoteException {
		super();

        try{
        	// create the registry and bind the name and object.
        	registry = LocateRegistry.createRegistry( port );
            registry.rebind("rmiServer", this);
        } catch (RemoteException e) {
        	serverState = State.OFFLINE;
        	throw e;
        }
        
        serverState = State.ONLINE;
        uniqueServerId = Math.abs(random.nextInt(Integer.MAX_VALUE) % (int)Math.ceil(((double)Integer.MAX_VALUE / Math.pow(10, shift))) - 9999);
        //Make sure the uid will be valid
        
        this.txnStates = new HashMap<String,State>();
        this.txnResult = new HashMap<String, Object>();
        
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
	// 1. Get a unique global transaction id: gid
	// 2.1 Create the entry in txnState with initial state Processing
	// 2.2 Create the entry in txnResult with initial value of null
	// 3. Submit the queries one by one to scheduler
	// 4. Wait for scheduler return the result
	// 5.1 If OK, send commit request to TransactionManager
	// 5.2 Else, send abort request to TransactionManager
	// 6. Update the txnState and txnResult
	//**************************************************************
	@Override
	public String txnCreatingAccounts(int balance) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String txnCheckingBalance(int uid) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String txnDeposit(int uid, double amount) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String txnWithdraw(int uid, double amount) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String txnTransfer(int uid1, int uid2, double amount)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	//To implement get State of transaction and get result of transaction,
	// the server need to have a map to hold the information of transactions
	// This information can be discarded after we call getTxnResult function if it is succeeded
	@Override
	public Object getTxnResult(String gid) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	
	//Send empty request to one of other servers
	//Return true if it is OK and false in case of error
	@Override
	public Boolean heartBeat() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	//Send message to other server with user specification protocol
	//Return some message for user
	@Override
	public String send(String message) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
}
