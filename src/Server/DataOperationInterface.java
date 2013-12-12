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
	String txnCheckingBalance(int uid) throws RemoteException;
	
	//Deposit amount money to account uid
	//Return the transaction gid for further operation
	String txnDeposit(int uid, double amount) throws RemoteException;
	
	//Withdraw amount money from account uid
	//Return the transaction gid for further operation
	String txnWithdraw(int uid, double amount) throws RemoteException;
	
	//Transfer amount money from uid1 to uid2
	//Return the transaction gid for further operation
	String txnTransfer(int uid1, int uid2, double amount) throws RemoteException;
	
	
	
	State getTxnState(String gid) throws RemoteException;
	
	//After a transaction is submitted, the result may not be returned instantly, we will keep checking the transaction
	//state until it is failed or finished, and using this function to get the result
	//The client is responsible to give the result a type and take use of it
	Object getTxnResult(String gid) throws RemoteException;
	
	//For client, checking whether the server is online
	State getServerState() throws RemoteException;
	
	
}
