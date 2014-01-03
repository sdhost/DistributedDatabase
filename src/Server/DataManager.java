package Server;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
	
	private volatile Log log;
	
	private volatile ConcurrentHashMap<String, String> TupleIdToValue; 	//In memory Database, Assume Only one table, a global unique key and a value column
	
	
	/**
	 * Read from in-memory map
	 * The scheduler should make sure lock has been granted before calling read/write of data manager
	 */
	public String read(String tupleID, String gid) throws Exception {
		if(!this.TupleIdToValue.containsKey(tupleID))
			throw new Exception(tupleID + " not exist in table ");
		
		log.newlog(gid, tupleID, null, null);
		
		return this.TupleIdToValue.get(tupleID);
	}
	
	public void write(String tupleID, String newValue, String gid) throws Exception{
		String oldValue = null;
		
		if(this.TupleIdToValue.containsKey(tupleID))
			oldValue = this.TupleIdToValue.get(tupleID);
		
		log.newlog(gid, tupleID, oldValue, newValue);
		
		this.TupleIdToValue.put(tupleID, newValue);
		
	}
	/**
	 * Using to check whether tuple is in this server
	 */
	public boolean exist(String tupleID){
		return this.TupleIdToValue.contains(tupleID);
	}
	
	public void Abort(String gid){
		LinkedList<String> undoList = this.log.Abort(gid);
		
		// WRITE all items from the undoList!
		
		//TODO: Do something about the undoList, need to have an undo strategy
	}
	
	public void Commit(int gid){
		this.log.Commit(gid);
	}
	
	public void Begin(int gid){
		this.log.newTransaction(gid);
	}
	
}
