package Server;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Log {

	private volatile int serialID = 0;
	private int lastCommit = -1;
	
	private volatile ConcurrentHashMap<Integer, String> rawLog; //Map<serialID, LogContent>
	
	public Log(){
		
	}
	
	public void newlog(String gid, String tupleID, String oldValue, String newValue) {
		
		if(oldValue == null)
			oldValue = "Null";
		if(newValue == null)
			newValue = "Null";
		
		String logline = String.valueOf(gid) + "\t" +
						 tupleID + "\t" +
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
		
		for(int i = serialID -1; i >= 0 ;i--){
			undoList.push(this.rawLog.get(i));
			if(this.rawLog.get(i).contentEquals(String.valueOf(gid) + "\tBegin"));
				break;
		}
		
		return undoList;
	}
	
	public void newTransaction(int gid){
		this.rawLog.put(serialID, String.valueOf(gid) + "\tBegin");
		this.serialID += 1;
	}

}
