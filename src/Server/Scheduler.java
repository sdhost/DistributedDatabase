package Server;

import java.util.LinkedList;
import java.util.List;
import Server.Operation.type;

/**
 * Receives read and write operations, which are ordered into a serializable plan,
 * using Strict 2PL.
 */
public class Scheduler {
	private LockManager _lockmanager;
	private DataManager _datamanager;
	
	public Scheduler() {
		_lockmanager = new LockManager();
		_datamanager = new DataManager();
	}
	
	/**
	 * Called to execute transaction
	 */
	public List<ResultSet> execute(List<Operation> tx, String gid, Long timestamp) {
		
		List<ResultSet> rs = new LinkedList<ResultSet>();
		
		_lockmanager.prepareLocking(gid, timestamp);
		
		for (Operation o : tx) {
			if (o.getType() == type.READ){
				ResultSet result = new ResultSet(read(o.getTupleID(), gid));
				rs.add(result);
				
			}
			if (o.getType() == type.WRITE)
				write(o.getTupleID(), o.getNewValue(), gid);
		}
		
		// Release all locks, held by transaction
		while (!_lockmanager.release(gid)) {
			// Locks not released. Keep retrying?
			ServerGUI.log("Locks not released for " + gid + ". Retrying...");
			try {
				Thread.sleep(500);	
			}  catch (Exception ex) {}
		}
		
		return rs;
	}
	
	/**
	 * Called to read
	 */
	private String read(String tupleID, String gid) {
		if (_lockmanager.lock(gid, tupleID, false)) {
			// Shared lock not granted
			while (!_lockmanager.isLocked(gid, tupleID)) {
				// Sleep, until lock is granted
				try {
					Thread.sleep(500);	
				} catch (Exception ex) {}
			}
		}
		
		// Shared lock granted
		try {
			return _datamanager.read(tupleID, "tableNotUsed", gid);	
		} catch (Exception ex) {
			ServerGUI.log("Error reading from DataManager: " + ex.toString());
			return null;
		}
	}
	
	/**
	 * Call to write
	 */
	private void write(String tupleID, String newValue, String gid) {
		if (_lockmanager.lock(gid, tupleID, true)) {
			// Exclusive lock not granted
			while (!_lockmanager.isLocked(gid, tupleID)) {
				// Sleep, until lock is granted
				try {
					Thread.sleep(500);	
				} catch (Exception ex) {}
			}
		}
		
		// Exclusive lock granted
		try {
			_datamanager.write(tupleID, "tableNotUsed", newValue, gid);
		} catch (Exception ex) {
			ServerGUI.log("Error writing to DataManager: " + ex.toString());
		}
	}	
}
