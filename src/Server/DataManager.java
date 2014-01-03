package Server;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
	private volatile Log log;	
	private volatile ConcurrentHashMap<String, String> TupleIdToValue; 	//In memory Database, Assume Only one table, a global unique key and a value column
	
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
	
	public void Abort(String gid) throws Exception {
		LinkedList<String> undoList = log.Abort(gid);
		
		// TODO: Test it works!
		// UndoList should be list{<gid\t tupleID\t oldValue\t newValue\t}
		for (String toUndo : undoList) {
			String curGid = toUndo.split("\t")[0];
			String curTupleID = toUndo.split("\t")[1];
			String curOldVal = toUndo.split("\t")[2];
			write(curTupleID, curOldVal, curGid);
		}
	}
	
	public void Commit(String gid){
		log.Commit(gid);
	}
	
	public void Begin(String gid){
		log.newTransaction(gid);
	}
	
}
