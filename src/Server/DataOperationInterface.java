package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataOperationInterface extends Remote {
	
	int read(int id, int gid) throws RemoteException;
	
	int write(int id, double value, int gid) throws RemoteException;
	
	State getState(int gid) throws RemoteException;
	
	int abort(int gid) throws RemoteException;
	
	int commit(int gid) throws RemoteException;
	
	int newTransaction() throws RemoteException;
	
	int getNewID() throws RemoteException;
}
