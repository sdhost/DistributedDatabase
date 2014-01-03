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
		
		if(rmiServer.getServerState() == State.ONLINE) {
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
	public String txnCreatingAccounts(int balance) throws RemoteException {
		if (!initialized)
			return null;
		
		String gid = rmiServer.txnCreatingAccounts(balance);
		if (gid == null)
			return null;
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH) {
				return (String)rmiServer.getTxnResult(gid);
			} else if(s == State.ERROR) {
				ClientGUI.log(rmiServer.getTxnResult(gid).toString());
				return null;
			}
			
			try {
				Thread.sleep(100);	
			} catch (InterruptedException ex) {}
		}
	}
	
	/**
	 * Check the balance for account uid
	 * @return balance or null in case of problems
	 */
	public String txnCheckingBalance(String uid) throws RemoteException{
		if(!initialized){
			return null;
		}
		
		String gid = rmiServer.txnCheckingBalance(uid);
		if (gid == null)
			return null;
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (String)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				ClientGUI.log(rmiServer.getTxnResult(gid).toString());
				return null;
			}

			try {
				Thread.sleep(100);	
			} catch (InterruptedException ex) {}
		}
		
		
	}
	
	/**
	 * Deposit some money to account uid
	 * @return balance or -1 in case of problems
	 */
	public String txnDeposit(String uid, int amount) throws RemoteException{
		if(!initialized){
			return null;
		}
		
		String gid = rmiServer.txnDeposit(uid, amount);
		if (gid == null)
			return null;
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (String)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				ClientGUI.log(rmiServer.getTxnResult(gid).toString());
				return null;
			}

			try {
				Thread.sleep(100);	
			} catch (InterruptedException ex) {}
		}
	}
	
	/**
	 * Withdraw some money from account uid
	 * @return balance or negative numbers in case of error
	 */
	public String txnWithdraw(String uid, int amount) throws RemoteException{
		if(!initialized){
			return null;
		}
		
		String gid = rmiServer.txnWithdraw(uid, amount);
		if (gid == null)
			return null;
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (String)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				ClientGUI.log(rmiServer.getTxnResult(gid).toString());
				return null;
			}

			try {
				Thread.sleep(100);	
			} catch (InterruptedException ex) {}
		}
		
	}
	
	/**
	 * Transfer some money from account uid1 to uid2
	 * @return balance of uid1 or negative numbers in case of error
	 */
	public String txnTransfer(String uid1, String uid2, int amount) throws RemoteException{
		if(!initialized){
			return null;
		}

		String gid = rmiServer.txnTransfer(uid1, uid2, amount);
		if (gid == null)
			return null;
		
		while(true){
			State s = rmiServer.getTxnState(gid);
			if( s == State.FINISH)
				return (String)rmiServer.getTxnResult(gid);
			else if(s == State.ERROR){
				ClientGUI.log(rmiServer.getTxnResult(gid).toString());
				return null;
			}

			try {
				Thread.sleep(100);	
			} catch (InterruptedException ex) {}
		}
		
	}

}