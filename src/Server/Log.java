package Server;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains log of all operations
 */
public class Log {
	private volatile ConcurrentHashMap<Integer, String> _rawLog; //Map<serialID, LogContent>
	private volatile int _serialID = 0;
	
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
		_serialID++;
	}
	
	public LinkedList<String> Abort(String gid) {
		_rawLog.put(_serialID++, String.valueOf(gid) + "\tAbort");
		
		LinkedList<String> undoList = new LinkedList<String>();
		for (String line : _rawLog.values()) {
			if (line.startsWith(gid)) {
				if (line.split("\t").length == 4 && !line.split("\t")[2].toLowerCase().equals("null"))
					undoList.add(line);
			}
		}
		
		// UndoList should be list{<gid\t tupleID\t oldValue\t newValue\t}
		return undoList;
	}
	
	public void newTransaction(String gid) {
		_rawLog.put(_serialID++, String.valueOf(gid) + "\tBegin");
	}

}
