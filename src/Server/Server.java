package Server;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server extends java.rmi.server.UnicastRemoteObject
implements DataOperationInterface{

	String thisAddress;
	int thisPort;
	Registry registry; //RMI registry for lookup the remote objects
	
	protected Server() throws RemoteException {
		super();
		
		try{
            // get the address of this host.
            thisAddress= (InetAddress.getLocalHost()).toString();
        }
        catch(Exception e){
            throw new RemoteException("can't get inet address.");
        }
		thisPort=3232;  // service port
        try{
        	// create the registry and bind the name and object.
        	registry = LocateRegistry.createRegistry( thisPort );
            registry.rebind("rmiServer", this);
        }
        catch(RemoteException e){
        throw e;
        }
	}

	@Override
	public int read(int id, int gid) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(int id, double value, int gid) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public State getTxnState(int gid) throws RemoteException {
		// TODO Auto-generated method stub
		return State.ONLINE;
	}

	@Override
	public int abort(int gid) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int commit(int gid) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int newTransaction() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNewID() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public State getServerState() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}



}
