package Server;

import java.util.ArrayList;
import java.util.List;

public class TransactionManager {

	private Scheduler _scheduler;
	
	public TransactionManager() {
		_scheduler = new Scheduler(); 				
	}
	
	public String txnCreatingAccounts(int balance, String gid, String uid, Long timestamp) {
		
		// Create list of operations to apply
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().write(gid, uid, String.valueOf(balance)));

		// Send to scheduler
		_scheduler.execute(toApply, gid, timestamp);
		
		return null;
	}
	
	public void abort() {
		
		// Call scheduler to undo the operations of the transaction
		
	}
	
	public void commit() {
		
		// Forward commit request to 2PC manager, which becomes coordinator in 2PC protocol.
		
		// Coordinator contacts relevant remote servers 2PC managers, to complete a 2PC.
		
		
		
	}

	public String txnCheckingBalance(String gid, String uid, Long timestamp) {
		// Create list of operations to apply
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().read(gid, uid));

		// Send to scheduler
		List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
		ResultSet result = rs.iterator().next();
		
		this.commit();
		
		return (String)result.getVal();
	}

	public String txnDeposit(String gid, String uid, int amount, Long timestamp) {
		
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().read(gid, uid));

		//Read the current value
		List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
		ResultSet result = rs.iterator().next();
		
		int balance = Integer.valueOf((String)result.getVal());
		
		toApply.clear();
		toApply.add(new Operation().write(gid, uid, String.valueOf(balance + amount)));
		
		_scheduler.execute(toApply, gid, timestamp);
		
		this.commit();
		
		//We can read it again if necessary
		return String.valueOf(balance + amount);
	}

	public String txnWithdraw(String gid, String uid, int amount, Long timestamp){
		
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().read(gid, uid));

		//Read the current value
		List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
		ResultSet result = rs.iterator().next();
		
		int balance = Integer.valueOf((String)result.getVal());
		
		//Add error check if needed, just remove comment for following code and deal with the exception with outer function
//		if(balance < amount){
//			this.abort();
//			throw new Exception("Not Enough Money, require " + amount + ", available " + balance);
//		}
			
		
		toApply.clear();
		toApply.add(new Operation().write(gid, uid, String.valueOf(balance - amount)));
		
		_scheduler.execute(toApply, gid, timestamp);
				
		this.commit();
		//We can read it again if necessary
		return String.valueOf(balance - amount);
	}

	public String txnTransfer(String gid, String uid1, String uid2, int amount,
			Long timestamp) {
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().read(gid, uid1));
		toApply.add(new Operation().read(gid, uid2));
		
		List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
		ResultSet result = rs.get(0);
		int balance1 = Integer.valueOf(((String)result.getVal()));
		result = rs.get(1);
		int balance2 = Integer.valueOf(((String)result.getVal()));
		
		//Add error check if needed, just remove comment for following code and deal with the exception with outer function
//		if(balance1 < amount){
//			this.abort();
//			throw new Exception("Not Enough Money, require " + amount + ", available " + balance);
//		}
		
		balance1 -= amount;
		balance2 += amount;
		toApply.clear();
		toApply.add(new Operation().write(gid, uid1, String.valueOf(balance1)));
		toApply.add(new Operation().write(gid, uid2, String.valueOf(balance2)));
		
		return String.valueOf(balance1);
	}


	


}
