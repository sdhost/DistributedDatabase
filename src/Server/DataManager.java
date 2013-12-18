package Server;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
	
	private volatile Log log;
	private volatile ConcurrentHashMap<String, Map<String, String>> tableToTupleIdToValue; 	//In memory log
	
	/**
	 * Read from in-memory map
	 */
	public String read(String tupleID, String tableName, String gid) throws Exception {
		if(!this.tableToTupleIdToValue.containsKey(tableName))
			throw new Exception(tableName + " not exist!");
		if(!this.tableToTupleIdToValue.get(tableName).containsKey(tupleID))
			throw new Exception(tupleID + " not exist in table " + tableName);
		
		log.newlog(gid, tupleID, tableName, null, null);
		
		return this.tableToTupleIdToValue.get(tableName).get(tupleID);
	}
	
	public void write(String tupleID, String tableName, String newValue, String gid) throws Exception{
		String oldValue = null;
		
		if(!this.tableToTupleIdToValue.containsKey(tableName))
			throw new Exception(tableName + " not exist!");
		
		if(this.tableToTupleIdToValue.get(tableName).containsKey(tupleID))
			oldValue = this.tableToTupleIdToValue.get(tableName).get(tupleID);
		
		log.newlog(gid, tupleID, tableName, oldValue, newValue);
		
		this.tableToTupleIdToValue.get(tableName).put(tupleID, newValue);
		
	}
	
	public void Abort(int gid){
		LinkedList<String> undoList = this.log.Abort(gid);
		//TODO: Do something about the undoList, need to have an undo strategy
	}
	
	public void Commit(int gid){
		this.log.Commit(gid);
	}
	
	public void Begin(int gid){
		this.log.newTransaction(gid);
	}
	
}
