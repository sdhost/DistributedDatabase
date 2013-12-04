package Client;

import java.rmi.registry.*;
import java.rmi.*;

import Server.DataOperationInterface;
import Server.State;

public class Client{
	private Registry registry;
	private boolean initialized = false;
	private DataOperationInterface rmiServer;
	
	/**
	 * Call to connect to server using RMI
	 */
	public boolean registerRMIServer(String serverAddress, int serverPort) throws RemoteException, NotBoundException{
		// Bind to the "rmiServer" on the remote instance, using RMI
		registry = LocateRegistry.getRegistry(serverAddress, serverPort);
		rmiServer = (DataOperationInterface)(registry.lookup("rmiServer"));
		
		if(rmiServer.getServerState() == State.ONLINE){
			initialized = true;
			return true; // success
		}else{
			return false; // failure
		}
	}
	
	/**
	 * Inform server, to create new account
	 * @return accountid or null in case of problems
	 */
	public Integer txnCreatingAccounts(int balance) throws RemoteException {
		if (!initialized)
			return null;
		
		// Get global unique transaction id (=> serverid_transactionid)
		String gid = rmiServer.newTransaction();
		
		// Get new userid from server - TODO: Not implemented yet!
		int uid = rmiServer.getNewID();
		
		// Store balance - TODO: Not implemented yet!
		rmiServer.write(uid, balance, gid);
		
		// Commit - TODO: Not implemented yet!
		rmiServer.commit(gid);
		
		if(rmiServer.getTxnState(gid) == State.FINISH)
			return uid;
		else{
			rmiServer.abort(gid);
			return null;
		}
	}
	
	public double txnCheckingBalance(int uid) throws RemoteException{
		//Returning the balance
		// Error will return negative number
		if(!initialized){
			return -1;
		}
		
		String gid = rmiServer.newTransaction();
		
		double balance = rmiServer.read(uid, gid);
		
		rmiServer.commit(gid);
		
		if(rmiServer.getTxnState(gid) == State.FINISH)
			return balance;
		else{
			return -1;
		}
		
		
	}
	
	public int txnDeposit(int uid, double amount) throws RemoteException{
		//Returning 0 for success, -1 for error
		if(!initialized){
			return -1;
		}
		
		String gid = rmiServer.newTransaction();
		
		double balance = rmiServer.read(uid, gid);
		
		if(rmiServer.getTxnState(gid)==State.ERROR){
			//User id not founded
			rmiServer.abort(gid);
			return -1;
		}
		
		rmiServer.write(uid, balance + amount, gid);
		
		rmiServer.commit(gid);
		
		if(rmiServer.getTxnState(gid) == State.FINISH)
			return 0;
		else{
			return -1;
		}
	}
	
	public int txnWithdraw(int uid, double amount) throws RemoteException{
		//Returning 0 for success, -1 for server error, -2 for not enough money
		if(!initialized){
			return -1;
		}
		
		String gid = rmiServer.newTransaction();
		
		double balance = rmiServer.read(uid, gid);
		
		if(rmiServer.getTxnState(gid)==State.ERROR){
			//User id not founded
			rmiServer.abort(gid);
			return -1;
		}
		if(balance < amount){
			//Not enough money
			rmiServer.abort(gid);
			return -2;
		}
		
		rmiServer.write(uid, balance - amount, gid);
		
		rmiServer.commit(gid);
		
		if(rmiServer.getTxnState(gid) == State.FINISH)
			return 0;
		else{
			return -1;
		}
		
	}
	
	public int txnTransfer(int uid1, int uid2, double amount) throws RemoteException{
		//Transfer amount money from uid1 to uid2
		//Returning 0 for success, -1, -3 for server error, -2 for not enough money
		if(!initialized){
			return -1;
		}

		String gid = rmiServer.newTransaction();

		double balance1 = rmiServer.read(uid1, gid);

		if(rmiServer.getTxnState(gid)==State.ERROR){
			//User id 1 not founded
			rmiServer.abort(gid);
			return -1;
		}
		if(balance1 < amount){
			//Not enough money
			rmiServer.abort(gid);
			return -2;
		}
		
		double balance2 = rmiServer.read(uid2, gid);
		
		if(rmiServer.getTxnState(gid)==State.ERROR){
			//User id 2 not founded
			rmiServer.abort(gid);
			return -3;
		}
		

		rmiServer.write(uid1, balance1 - amount, gid);
		
		rmiServer.write(uid2, balance2 + amount, gid);

		rmiServer.commit(gid);

		if(rmiServer.getTxnState(gid) == State.FINISH)
			return 0;
		else{
			return -1;
		}
		
	}

}
