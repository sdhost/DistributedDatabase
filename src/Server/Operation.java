package Server;

import java.io.Serializable;

/**
 * Class used to represent a single operation
 */
public class Operation implements Serializable {
	public enum type {READ, WRITE}
	
	private type _t;
	private String _gid;
	private String _tupleID, _newValue;
	
	public Operation read(String gid, String tupleID) {
		_gid = gid;
		_t = type.READ;
		_tupleID = tupleID;
		return this;
	}
	
	public Operation write(String gid, String tupleID, String newValue) {
		_gid = gid;
		_t = type.WRITE;
		_tupleID = tupleID;
		_newValue = newValue;
		return this;
	}
	
	public type getType() {
		return _t;
	}
	
	public String getGID() {
		return _gid;
	}
	
	public String getTupleID() {
		return _tupleID;
	}
	
	public String getNewValue() {
		if (_t == type.READ)
			ServerGUI.log("ERROR. Called getNewValue on operation for read");
		
		return _newValue;
	}
	
	@Override
	public String toString() {
		return "(gid: " + _gid + " type: " + _t.toString() + " TupleID: " + _tupleID + ")";
	}
}
