package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataOperationInterface extends Remote {
	
	int read(int id, String gid) throws RemoteException;
	
	int write(int id, double value, String gid) throws RemoteException;
	
	State getTxnState(String gid) throws RemoteException;
	
	State getServerState() throws RemoteException;
	
	int abort(String gid) throws RemoteException;
	
	int commit(String gid) throws RemoteException;
	
	String newTransaction() throws RemoteException;
	
	int getNewID() throws RemoteException;
}
