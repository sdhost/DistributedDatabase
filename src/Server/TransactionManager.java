package Server;

public class TransactionManager {

	public void handleTransaction() {

		// Read transaction
		
		// Assign gid (global unique transaction id)
		
		// Decompose to read and write operations
		
		// Pass to scheduler (which should implement strict 2PL, by interacting with the log manager)
		
	}
	
	public void abort() {
		
		// Call scheduler to undo the operations of the transaction
		
	}
	
	public void commit() {
		
		// Forward commit request to 2PC manager, which becomes coordinator in 2PC protocol.
		
		// Coordinator contacts relevant remote servers 2PC managers, to complete a 2PC.
		
		
		
	}
	


}
