package Server;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains log of all operations
 */
public class Log {
	private volatile ConcurrentHashMap<Integer, String> _rawLog; //Map<serialID, LogContent>
	private volatile int _serialID = 0;
	//private int _lastCommit = -1;
	
	public Log() {
		_rawLog = new ConcurrentHashMap<Integer, String>();
	}
	
	public void newEntry(String gid, String tupleID, String oldValue, String newValue) {
		if(oldValue == null)
			oldValue = "Null";
		if(newValue == null)
			newValue = "Null";
		
		String logline = String.valueOf(gid) + "\t" +
						 tupleID + "\t" +
						 oldValue + "\t" +
						 newValue + "\t";
		_rawLog.put(_serialID++, logline);
	}
	
	public void Commit(String gid) {
		_rawLog.put(_serialID, String.valueOf(gid) + "\tCommit");
		//_lastCommit = _serialID;
		_serialID++;
	}
	
	/**
	 * TODO: DEBUG TO ENSURE IT IS CORRECT
	 */
	public LinkedList<String> Abort(String gid) {
		_rawLog.put(_serialID++, String.valueOf(gid) + "\tAbort");
		LinkedList<String> undoList = new LinkedList<String>();
		
		for (int i = _serialID -1; i >= 0; i--) {
			undoList.push(_rawLog.get(i));
			
			if(_rawLog.get(i).equals(String.valueOf(gid) + "\tBegin"))
				break;
		}
		
		// UndoList should be list{<gid\t tupleID\t oldValue\t newValue\t}
		return undoList;
	}
	
	public void newTransaction(String gid) {
		_rawLog.put(_serialID++, String.valueOf(gid) + "\tBegin");
	}

}
