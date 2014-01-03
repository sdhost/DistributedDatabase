package Server;

import java.util.concurrent.ConcurrentHashMap;
/**
 * Maintaining the state of multiple site txn
 * @author lsu
 *
 */
public class MultiTxnState {
	public ConcurrentHashMap<String, State> finishedTxn;
	public ConcurrentHashMap<String, State> unfinishedTxn;
	
	public MultiTxnState(){
		finishedTxn = new ConcurrentHashMap<String, State>();
		unfinishedTxn = new ConcurrentHashMap<String, State>();
	}

	public ConcurrentHashMap<String, State> getFinishedTxn() {
		return finishedTxn;
	}

	public void setFinishedTxn(ConcurrentHashMap<String, State> finishedTxn) {
		this.finishedTxn = finishedTxn;
	}

	public ConcurrentHashMap<String, State> getUnfinishedTxn() {
		return unfinishedTxn;
	}

	public void setUnfinishedTxn(ConcurrentHashMap<String, State> unfinishedTxn) {
		this.unfinishedTxn = unfinishedTxn;
	}
	
	
}
