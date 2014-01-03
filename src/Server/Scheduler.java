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
	
	public Scheduler(TransactionManager tm) {
		_datamanager = new DataManager();
		_lockmanager = new LockManager(tm, _datamanager);
	}
	
	/**
	 * Called to execute transaction
	 * 	returns null, in case of any problems
	 */
	public List<ResultSet> execute(List<Operation> tx, String gid, Long timestamp) {
		List<ResultSet> rs = new LinkedList<ResultSet>();
		_lockmanager.prepareLocking(gid, timestamp);

		/**
		 * Do read/write operations (will sleep until done)
		 */
		try {
			for (Operation o : tx) {
				if (o.getType() == type.READ)
					rs.add(new ResultSet(read(o.getTupleID(), gid)));
				
				if (o.getType() == type.WRITE)
					write(o.getTupleID(), o.getNewValue(), gid);
			}
		} catch (Exception ex) {
			ServerGUI.log("Error executing transaction " + ex.toString());
			
			// Must return null
			rs = null;
		}
		

		// Release all locks, held by transaction
		while (!_lockmanager.release(gid)) {
			// Locks not released. Keep retrying?
			ServerGUI.log("Locks not released for " + gid + ". Retrying...");
			try {
				Thread.sleep(500);	
			}  catch (InterruptedException ex) {}
		}

		return rs;
	}
	
	
	/**
	 * Called to read
	 */
	private String read(String tupleID, String gid) throws Exception {
		if (_lockmanager.lock(gid, tupleID, false)) {
			// Shared lock not granted
			while (!_lockmanager.isLocked(gid, tupleID)) {
				// Sleep, until lock is granted
				try {
					Thread.sleep(500);	
				} catch (InterruptedException ex) {}
			}
		}
		
		// Shared lock granted
		return _datamanager.read(tupleID, gid);	
	}
	
	/**
	 * Call to write
	 */
	private void write(String tupleID, String newValue, String gid) throws Exception {
		if (_lockmanager.lock(gid, tupleID, true)) {
			// Exclusive lock not granted
			while (!_lockmanager.isLocked(gid, tupleID)) {
				// Sleep, until lock is granted
				try {
					Thread.sleep(500);	
				} catch (InterruptedException ex) {}
			}
		}
		
		// Exclusive lock granted
		_datamanager.write(tupleID, newValue, gid);
	}
	
	public boolean isInServer(String tupleID){
		return _datamanager.exist(tupleID);
	}

	public void abort(String gid) throws Exception {
		_datamanager.Abort(gid);
		
	}
	
	public void commit(String gid){
		_datamanager.Commit(gid);
	}
}
