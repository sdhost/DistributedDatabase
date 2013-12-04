package Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class Server extends java.rmi.server.UnicastRemoteObject implements DataOperationInterface {
	private Registry registry; //RMI registry for lookup the remote objects
	private State serverState = State.OFFLINE;
	private Random random = new Random();
	private long transactionCounter = 0;
	private int uniqueServerId;
	
	protected Server(String ip, int port) throws RemoteException {
		super();

        try{
        	// create the registry and bind the name and object.
        	registry = LocateRegistry.createRegistry( port );
            registry.rebind("rmiServer", this);
        } catch (RemoteException e) {
        	serverState = State.OFFLINE;
        	throw e;
        }
        
        serverState = State.ONLINE;
        uniqueServerId = random.nextInt(Integer.MAX_VALUE);
	}

	@Override
	public int read(int id, String gid) throws RemoteException {
		ServerGUI.log("Called read");
		return 0;
	}

	@Override
	public int write(int id, double value, String gid) throws RemoteException {
		ServerGUI.log("Called write");
		return 0;
	}

	@Override
	public State getTxnState(String gid) throws RemoteException {
		ServerGUI.log("Called getTxnState");
		return State.ONLINE;
	}

	@Override
	public int abort(String gid) throws RemoteException {
		ServerGUI.log("Called abort");
		return 0;
	}

	@Override
	public int commit(String gid) throws RemoteException {
		ServerGUI.log("Called commit");
		return 0;
	}

	@Override
	public String newTransaction() throws RemoteException {
		ServerGUI.log("Called newTransaction");
		return uniqueServerId + "_" + transactionCounter++;
	}

	@Override
	public int getNewID() throws RemoteException {
		
		// TODO: Users should be created and stored in some data structure
		
		ServerGUI.log("Called getNewID");
		return 0;
	}

	@Override
	public State getServerState() throws RemoteException {
		return serverState;
	}
}
