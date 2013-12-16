package Server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Receives read and write operations, which are ordered into a serializable plan,
 * using 2PL, and communication with the Lock Manager and Data Manager.
 *
 */
public class Scheduler {
	private LockManager _lockmanager;
	private DataManager _datamanager;
	
	//Handling the submitted transaction operations
	//Using strict 2PL to decide whether a lock can be granted
	//Currently, no coordination among different nodes, assume only one scheduler and one lock manager for system
	
	/*LinkedList<String> currentTxn;//Store the transactions submitted but not committed or aborted in arriving time order
	HashMap<String, List<String>> precedenceGraph;//Used as information to judge the precedence between any two transactions
											//If any two transaction has a precedence relationship txn1 -> txn2,
											//then txn2's gid will be stored in txn1's post list, precedenceGraph.get(gid1).add(gid2)
	
	
	LinkedHashMap<String, String> currentOperations;//Store the planned operation order of current transactions that not commit
													//Key for transaction gid, value is the operation
	int procPtr = -1;//The pointer indicate the latest operation that has been processed in this.currentOperations
	
	LinkedHashMap<String, String> bufferedOperations;//Store the operation that need to wait for locks
													 //Key for operations, value is the transaction gid that it need to wait
	
	public Scheduler(){
		this.currentTxn = new LinkedList<String>();
		this.precedenceGraph = new HashMap<String, List<String>>();
		this.lockmanager = new LockManager();
		this.currentOperations = new LinkedHashMap<String,String>();
		this.bufferedOperations = new LinkedHashMap<String, String>();
	}
	
	private boolean isPrecedent(String pre_gid, String post_gid){
		//Judge whether transaction with pre_gid is precedent to the one with post_gid
		//Should be called before granting lock to transaction pre_gid
		
		if(this.precedenceGraph.containsKey(pre_gid))
			return this.precedenceGraph.get(pre_gid).contains(post_gid);
		else
			return true;//No conflict between the two transactions, just return true to allow the lock
	
	}
	
	public void newOperation(String gid, String operation){
		//Schedule a new operation of transaction with gid
		// 1. If it is a new transaction, create the entry in currentTxn, precedenceGraph
		// 2. Check the precedence graph, decide whether the operation can be execute
		// 	3.a.1 If yes, grant lock using lockmanager
		//	3.a.2 Add it to the end of currentOperations
		//	3.b.1 If no, let it wait for the precedent transaction and stored in bufferedOperations list
	}
	*/
	
	public Scheduler() {
		_lockmanager = new LockManager();
		_datamanager = new DataManager();
	}
	
	/**
	 * Called to execute operations in serializable fashion
	 */
	public void execute(List<Operation> operations) {
		
	}
	
	
	
	/**
	 * Called to read in serializable order
	 */
	public String read(String tupleID, String tableName, String gid) {
		// TODO: Why has the lockmanager no tableName?
		if (!_lockmanager.lock(gid, tupleID, false)) {
			// Shared lock granted
			
			try {
				return _datamanager.read(tupleID, tableName, gid);	
			} catch (Exception ex) {
				ServerGUI.log("Error reading from DataManager: " + ex.toString());
			}
			
		} else {
			
			// Shared lock not granted ... TODO: Can lockmanager wait until requested lock is granted?
		}
		
		return null;
	}
	
	
	
	/**
	 * Call to write in serializable order
	 */
	public void write(String tupleID, String tableName, String newValue, String gid) {
		// TODO: Why has the lockmanager no tableName?
		if (!_lockmanager.lock(gid, tupleID, true)) {
			// Exclusive lock granted
			
			try {
				_datamanager.write(tupleID, tableName, newValue, gid);
			} catch (Exception ex) {
				ServerGUI.log("Error writing to DataManager: " + ex.toString());
			}
		} else {
			// Exclusive lock not granted ... TODO: Can lockmanager wait until requested lock is granted?
		}
	}
	
	public void commit(String gid) {
		// Release all locks, held by transaction
		if (!_lockmanager.release(gid)) {
			// TODO: How to behave if locks cannot be released? ... and why would it happen?
		}
		
		// Do commit
	}
	
}
