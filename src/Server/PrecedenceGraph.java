package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Server.Operation.type;

/**
 * Class implements the logic of a Precedence Graph
 */
public class PrecedenceGraph {
	private List<List<Operation>> _unfinishedTransactions;
	
	public PrecedenceGraph() {
		_unfinishedTransactions = new ArrayList<List<Operation>>();
	}
	
	/**
	 * Returns list of transactions, that must be committed, before this can begin.
	 * Can be empty, if safe to begin.
	 */
	public synchronized void addTransaction(List<Operation> t) {
		HashMap<Operation, Operation> execThisBeforeThat = new HashMap<Operation, Operation>();
		
		/**
		 * Add transaction
		 */
		_unfinishedTransactions.add(t);
		
		
		/**
		 * Calculate all edges in precedence graph
		 */
		HashMap<String, List<String>> txIdToOutgoingEdges = calcEdges();
		
		
		/**
		 * Try and break cycle if it exists in Precedence Graph
		 */
		ArrayList<String> txIdToRemove = new ArrayList<String>();
		while (getCycle(txIdToOutgoingEdges) != null) {
			
			
			
			
			
		}
		
		// Contains all txids to handle, when resolving problems
		
		/*while (isCyclic() != null) {
			
			// Remove one element from cycle, to break it
			txIdToRemove.add(isCyclic().get(0));
			
			// Calculate new edges and dependencies
			calcEdges(txIdToRemove);
		}
		
		
		return _execThisBeforeThat;*/
	}
	
	/**
	 * Returns true if there is a cycle in the precedence graph
	 */
	private List<String> getCycle(HashMap<String, List<String>> txIdToOutgoingEdges) {
		for (String gid : txIdToOutgoingEdges.keySet()) {
			List<String> cycle = hasCycle(txIdToOutgoingEdges, gid, new ArrayList<String>());
			if (cycle != null) return cycle;
		}
		return null;
	}
	
	/**
	 * Detect if there are any cycles. Do DFS, and if any 
	 * vertex is reached, which has already been seen, there
	 * must be a cycle
	 * Returns cycle if detected, otherwise null
	 */
	private List<String> hasCycle(HashMap<String, List<String>> txIdToOutgoingEdges, String currentGID, List<String> visitedGid) {
		
		// If currentGID exists in visitedGID, then there must be a cycle
		if (visitedGid.contains(currentGID)) {
			visitedGid.add(currentGID);
			return visitedGid;
		}
		
		// Add current vertex, to set of visited vertices
		visitedGid.add(currentGID);
		
		// Follow all outgoing edges
		if (txIdToOutgoingEdges.containsKey(currentGID)) {
			for (String targetGID : txIdToOutgoingEdges.get(currentGID)) {
				return hasCycle(txIdToOutgoingEdges, targetGID, visitedGid);
			}
		}		
		return null;
	}
	
	/**
	 * Function calculates all edges in precedence graph
	 */
	private HashMap<String, List<String>> calcEdges() {
		
		// Clear all edges
		HashMap<String, List<String>> txIdToOutgoingEdges = new HashMap<String, List<String>>();
				
		// Iterate all combinations of operations, for all unfinished transactions
		for (List<Operation> curTx : _unfinishedTransactions) {
			for (Operation curTxOp : curTx) {
				
				for (List<Operation> checkTx : _unfinishedTransactions) {
					for (Operation checkTxOp : checkTx) {
						if (conflict(curTxOp, checkTxOp)) {
							
							/**
							 * There is a conflict between operations.
							 */

							// If from two different transactions, curTxOp can potentially be run before checkTxOp.
							if (!curTxOp.getGID().equals(checkTxOp.getGID())) {
								System.out.println("ADDED EDGE FROM " + curTxOp.toString() + " TO " + checkTxOp.toString());
								if (!txIdToOutgoingEdges.containsKey(curTxOp.getGID()))
									txIdToOutgoingEdges.put(curTxOp.getGID(), new ArrayList<String>());
								txIdToOutgoingEdges.get(curTxOp.getGID()).add(checkTxOp.getGID());
							}
						}
					}
				}
			}
		}
		
		return txIdToOutgoingEdges;
	}
	
	private boolean conflict(Operation a, Operation b) {
		// Operation a and b, belongs to same transaction => they are in conflict
		if (a.getGID().equals(b.getGID()))
				return true;
		
		// Operation a and b, has to involve same object for them to be in conflict
		if (!a.getTupleID().equals(b.getTupleID()))
			return false;
		
		// Operation a or b, has to be write for operations to be in conflict
		if (a.getType() == type.READ && b.getType() == type.READ)
			return false;
		
		return true;
	}
}
