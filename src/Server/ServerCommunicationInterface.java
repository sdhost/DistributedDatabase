package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerCommunicationInterface extends Remote{

	//Send empty request to one of other servers
	//Return true if it is OK and false in case of error
	State heartBeat() throws RemoteException;
	
	
	//reply vote request from other servers
	State replyVote(String gid) throws RemoteException;
	
	//proceed the global vote decision
	String proceedVoteDecision(String gid, State decision) throws RemoteException;
	
	//Send message to other server with user specification protocol
	//Return some message for user
	String send(String message) throws RemoteException;
	
	//Remote Execution
	//call replyVote for return
	List<ResultSet> remoteExecute(List<Operation> ops, String gid,	long timestamp, int sid) throws RemoteException;
	
	//Check whether tuple is stored in this server
	boolean isExist(String tupleId) throws RemoteException;
	
	int getServerID() throws RemoteException;


	
}
