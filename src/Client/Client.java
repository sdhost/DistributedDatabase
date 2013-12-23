package Client;

import java.rmi.registry.*;
import java.rmi.*;

import Server.DataOperationInterface;
import Server.State;
import Server.TransactionManager;

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
		
		String gid = rmiServer.txnCreatingAccounts(balance);
		
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (Integer)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				//TODO: do something about the error
				//The serve could return more information about the error
				Object err = rmiServer.getTxnResult(gid);
				return null;
			}else{
				//Still processing
				continue;
			}
		}
		
		
		
		
	}
	
	/**
	 * Check the balance for account uid
	 * @return balance or -1 in case of problems
	 */
	public double txnCheckingBalance(String uid) throws RemoteException{
		if(!initialized){
			return -1;
		}
		
		String gid = rmiServer.txnCheckingBalance(uid);
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (Double)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				//TODO: do something about the error
				//The serve could return more information about the error
				Object err = rmiServer.getTxnResult(gid);
				return -1;
			}else{
				//Still processing
				continue;
			}
		}
		
		
	}
	
	/**
	 * Deposit some money to account uid
	 * @return balance or -1 in case of problems
	 */
	public double txnDeposit(String uid, int amount) throws RemoteException{
		if(!initialized){
			return -1;
		}
		
		String gid = rmiServer.txnDeposit(uid, amount);
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (Double)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				//TODO: do something about the error
				//The serve could return more information about the error
				Object err = rmiServer.getTxnResult(gid);
				return -1;
			}else{
				//Still processing
				continue;
			}
		}
	}
	
	/**
	 * Withdraw some money from account uid
	 * @return balance or negative numbers in case of error
	 */
	public double txnWithdraw(String uid, int amount) throws RemoteException{
		if(!initialized){
			return -1;
		}
		
		String gid = rmiServer.txnWithdraw(uid, amount);
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (Double)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				//TODO: do something about the error
				//The serve could return more information about the error
				Object err = rmiServer.getTxnResult(gid);
				return -1;
			}else{
				//Still processing
				continue;
			}
		}
		
	}
	
	/**
	 * Transfer some money from account uid1 to uid2
	 * @return balance of uid1 or negative numbers in case of error
	 */
	public double txnTransfer(String uid1, String uid2, int amount) throws RemoteException{
		if(!initialized){
			return -1;
		}

		String gid = rmiServer.txnTransfer(uid1, uid2, amount);
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (Double)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				//TODO: do something about the error
				//The serve could return more information about the error
				Object err = rmiServer.getTxnResult(gid);
				return -1;
			}else{
				//Still processing
				continue;
			}
		}
		
	}

}
