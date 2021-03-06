package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataOperationInterface extends Remote {
	
	
	//Creating new accounts with initial balance
	//Return the transaction gid for further operation
	//The connected server will be the local branch of this account
	String txnCreatingAccounts(int balance) throws RemoteException;
	
	//Check balance for a given account primary key uid
	//Return the transaction gid for further operation
	String txnCheckingBalance(String uid) throws RemoteException;
	
	//Deposit amount money to account uid
	//Return the transaction gid for further operation
	String txnDeposit(String uid, int amount) throws RemoteException;
	
	//Withdraw amount money from account uid
	//Return the transaction gid for further operation
	String txnWithdraw(String uid, int amount) throws RemoteException;
	
	//Transfer amount money from uid1 to uid2
	//Return the transaction gid for further operation
	String txnTransfer(String uid1, String uid2, int amount) throws RemoteException;
	
	
	
	State getTxnState(String gid) throws RemoteException;
	
	//Get the creation time of a transaction, the time is represented as the return of System.currentTimeMillis()
	//If the transaction is not running, this method will return null
	Long getTxnTime(String gid) throws RemoteException;
	
	//After a transaction is submitted, the result may not be returned instantly, we will keep checking the transaction
	//state until it is failed or finished, and using this function to get the result
	//The client is responsible to give the result a type and take use of it
	Object getTxnResult(String gid) throws RemoteException;
	
	//For client, checking whether the server is online
	State getServerState() throws RemoteException;
	
	
	
}
