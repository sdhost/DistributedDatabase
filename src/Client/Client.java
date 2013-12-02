package Client;

import java.rmi.registry.*;
import java.rmi.*;

import Server.DataOperationInterface;
import Server.State;



public class Client{

	DataOperationInterface rmiServer;
	Registry registry;
	boolean initialized = false;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public int registerRMIServer(String serverAddress, int serverPort) throws RemoteException, NotBoundException{
		
		registry = LocateRegistry.getRegistry(serverAddress, serverPort);
		rmiServer = (DataOperationInterface)(registry.lookup("rmiServer"));
		
		//Get server state for special transaction ID 0
		if(rmiServer.getState(0) == State.ONLINE){
			initialized = true;
			return 0; // 0 for success
		}else{
			return -1; // -1 for failure
		}
			
		
	}
	
	
	public int txnCreatingAccounts(int balance) throws RemoteException{
		//Returning integer is the account ID
		//Error in creating account will return negative integer
		if(!initialized){
			return -1;
		}
		
		int gid = rmiServer.newTransaction();
		
		int uid = rmiServer.getNewID();
		
		rmiServer.write(uid, balance, gid);
		
		rmiServer.commit(gid);
		
		if(rmiServer.getState(gid) == State.FINISH)
			return uid;
		else{
			rmiServer.abort(gid);
			return -1;
		}
	}
	
	public double txnCheckingBalance(int uid) throws RemoteException{
		//Returning the balance
		// Error will return negative number
		if(!initialized){
			return -1;
		}
		
		int gid = rmiServer.newTransaction();
		
		double balance = rmiServer.read(uid, gid);
		
		rmiServer.commit(gid);
		
		if(rmiServer.getState(gid) == State.FINISH)
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
		
		int gid = rmiServer.newTransaction();
		
		double balance = rmiServer.read(uid, gid);
		
		if(rmiServer.getState(gid)==State.ERROR){
			//User id not founded
			rmiServer.abort(gid);
			return -1;
		}
		
		rmiServer.write(uid, balance + amount, gid);
		
		rmiServer.commit(gid);
		
		if(rmiServer.getState(gid) == State.FINISH)
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
		
		int gid = rmiServer.newTransaction();
		
		double balance = rmiServer.read(uid, gid);
		
		if(rmiServer.getState(gid)==State.ERROR){
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
		
		if(rmiServer.getState(gid) == State.FINISH)
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

		int gid = rmiServer.newTransaction();

		double balance1 = rmiServer.read(uid1, gid);

		if(rmiServer.getState(gid)==State.ERROR){
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
		
		if(rmiServer.getState(gid)==State.ERROR){
			//User id 2 not founded
			rmiServer.abort(gid);
			return -3;
		}
		

		rmiServer.write(uid1, balance1 - amount, gid);
		
		rmiServer.write(uid2, balance2 + amount, gid);

		rmiServer.commit(gid);

		if(rmiServer.getState(gid) == State.FINISH)
			return 0;
		else{
			return -1;
		}
		
	}

}