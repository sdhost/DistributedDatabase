package Server;

import java.util.ArrayList;
import java.util.List;

public class TransactionManager {

	private Scheduler _scheduler;
	
	public TransactionManager() {
		_scheduler = new Scheduler(); 				
	}
	
	public String txnCreatingAccounts(int balance) {
		
		// Create list of operations to apply
		List<Operation> toApply = new ArrayList<Operation>();		
		toApply.add(new Operation().read("1", "1"));
		toApply.add(new Operation().read("1", "2"));
		toApply.add(new Operation().write("1", "2", "3"));

		// Send to scheduler
		_scheduler.execute(toApply);
		return null;
	}
	
	public void abort() {
		
		// Call scheduler to undo the operations of the transaction
		
	}
	
	public void commit() {
		
		// Forward commit request to 2PC manager, which becomes coordinator in 2PC protocol.
		
		// Coordinator contacts relevant remote servers 2PC managers, to complete a 2PC.
		
		
		
	}


	


}
