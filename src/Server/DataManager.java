package Server;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
	private volatile Log log;	
	private volatile ConcurrentHashMap<String, String> TupleIdToValue; 	//In memory Database, Assume Only one table, a global unique key and a value column
	
	// Contains states of all transactions
	public ConcurrentHashMap<String, State> _allSeenTransactions = new ConcurrentHashMap<String,State>();
	
	public DataManager() {
		TupleIdToValue = new ConcurrentHashMap<String, String>(); 
		log = new Log();
	}
	
	/**
	 * Read from in-memory map
	 * The scheduler should make sure lock has been granted before calling read/write of data manager
	 */
	public String read(String tupleID, String gid) throws Exception {
		if(!this.TupleIdToValue.containsKey(tupleID))
			throw new Exception(tupleID + " not exist in table ");
		
		log.newEntry(gid, tupleID, null, null);
		
		return this.TupleIdToValue.get(tupleID);
	}
	
	public void write(String tupleID, String newValue, String gid) throws Exception{
		String oldValue = null;
		
		if(this.TupleIdToValue.containsKey(tupleID))
			oldValue = this.TupleIdToValue.get(tupleID);
		
		log.newEntry(gid, tupleID, oldValue, newValue);
		
		this.TupleIdToValue.put(tupleID, newValue);
		
	}
	/**
	 * Using to check whether tuple is in this server
	 */
	public boolean exist(String tupleID){
		return this.TupleIdToValue.containsKey(tupleID);
	}
	
	/**
	 * Returns true, if tx is aborted or previously was aborted
	 * otherwise false
	 */
	public boolean Abort(String gid) throws Exception {
		
		if (_allSeenTransactions.get(gid) == State.PREABORT)
			return true;
		
		if (_allSeenTransactions.get(gid) != State.PREABORT && _allSeenTransactions.get(gid) != State.PRECOMMIT) {
			_allSeenTransactions.put(gid, State.PREABORT);
			
			LinkedList<String> undoList = log.Abort(gid);
			
			// UndoList should be list{<gid\t tupleID\t oldValue\t newValue\t}
			for (String toUndo : undoList) {
				String curGid = toUndo.split("\t")[0];
				String curTupleID = toUndo.split("\t")[1];
				String curOldVal = toUndo.split("\t")[2];
				write(curTupleID, curOldVal, curGid);
			}	
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true, if tx is committed or previously was committed
	 * otherwise false
	 */
	public boolean Commit(String gid) {
		if (_allSeenTransactions.get(gid) == State.PRECOMMIT)
			return true;
		
		if (_allSeenTransactions.get(gid) != State.PRECOMMIT && _allSeenTransactions.get(gid) != State.PREABORT) {
			_allSeenTransactions.put(gid, State.PRECOMMIT);
			log.Commit(gid);
			return true;
		}
		return false;
	}
	
	public void Begin(String gid) {
		_allSeenTransactions.put(gid,  State.PROCESSING);
		log.newTransaction(gid);
	}
}
