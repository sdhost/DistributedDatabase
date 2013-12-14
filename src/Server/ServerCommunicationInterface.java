package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerCommunicationInterface extends Remote{

	//Send empty request to one of other servers
	//Return true if it is OK and false in case of error
	State heartBeat() throws RemoteException;
	
	
	//Send message to other server with user specification protocol
	//Return some message for user
	String send(String message) throws RemoteException;
	
	
}
