package Server;

import java.util.LinkedList;
import java.util.Map;

public class DataManager {
	
	private Log log;
	
	//In memory log
	private Map<String, Map<String, String>> rawData;
	// Map<TableName, Map<TupleID, TupleData>>
	
	public String read(String tupleID, String tableName, int gid) throws Exception{
		if(!this.rawData.containsKey(tableName))
			throw new Exception(tableName + " not exist!");
		if(!this.rawData.get(tableName).containsKey(tupleID))
			throw new Exception(tupleID + " not exist in table " + tableName);
		
		log.newlog(gid, tupleID, tableName, null, null);
		
		return this.rawData.get(tableName).get(tupleID);
	}
	
	public void write(String tupleID, String tableName, String newValue, int gid) throws Exception{
		String oldValue = null;
		
		if(!this.rawData.containsKey(tableName))
			throw new Exception(tableName + " not exist!");
		
		if(this.rawData.get(tableName).containsKey(tupleID))
			oldValue = this.rawData.get(tableName).get(tupleID);
		
		log.newlog(gid, tupleID, tableName, oldValue, newValue);
		
		this.rawData.get(tableName).put(tupleID, newValue);
		
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
