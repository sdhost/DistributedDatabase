package Server;

/**
 * Class used to represent a single operation
 */
public class Operation {
	public enum type {READ, WRITE}
	
	private type _t;
	private String _tupleID, _newValue;
	
	public Operation read(String tupleID) {
		_t = type.READ;
		_tupleID = tupleID;
		return this;
	}
	
	public Operation write(String tupleID, String newValue) {
		_t = type.WRITE;
		_tupleID = tupleID;
		_newValue = newValue;
		return this;
	}
	
	public type getType() {
		return _t;
	}
	
	public String getTupleID() {
		return _tupleID;
	}
	
	public String getNewValue() {
		if (_t == type.READ)
			ServerGUI.log("ERROR. Called getNewValue on operation for read");
		
		return _newValue;
	}
}
