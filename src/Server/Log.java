package Server;

import java.util.LinkedList;
import java.util.Map;

public class Log {

	private int serialID = 0;
	private int lastCommit = -1;
	
	private Map<Integer, String> rawLog; //Map<serialID, LogContent>
	
	public Log(){
		
	}
	
	public void newlog(int gid, String tupleID, String tableName, String oldValue, String newValue) {
		
		if(oldValue == null)
			oldValue = "Null";
		if(newValue == null)
			newValue = "Null";
		
		String logline = String.valueOf(gid) + "\t" +
						 tupleID + "\t" +
						 tableName + "\t" +
						 oldValue + "\t" +
						 newValue + "\t";
		this.rawLog.put(serialID, logline);
		this.serialID += 1;
		
	}
	
	public void Commit(int gid){
		this.rawLog.put(serialID, String.valueOf(gid) + "Commit");
		this.lastCommit = this.serialID;
		this.serialID += 1;
	}
	
	public LinkedList<String> Abort(int gid){
		this.rawLog.put(serialID, String.valueOf(gid) + "\tAbort");
		LinkedList<String> undoList = new LinkedList<String>();
		for(int i=this.lastCommit + 1; i < this.serialID; i++)
			undoList.push(this.rawLog.get(i));
		this.serialID += 1;
		return undoList;
	}

}
