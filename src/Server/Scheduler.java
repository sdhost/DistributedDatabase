package Server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class Scheduler {
	//Handling the submitted transaction operations
	//Using strict 2PL to decide whether a lock can be granted
	//Currently, no coordination among different nodes, assume only one scheduler and one lock manager for system
	
	LinkedList<String> currentTxn;//Store the transactions submitted but not committed or aborted in arriving time order
	HashMap<String, List<String>> precedenceGraph;//Used as information to judge the precedence between any two transactions
											//If any two transaction has a precedence relationship txn1 -> txn2,
											//then txn2's gid will be stored in txn1's post list, precedenceGraph.get(gid1).add(gid2)
	LockManager lockmanager;//Lock grants and queuing
	
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
	
	
	
	
}
