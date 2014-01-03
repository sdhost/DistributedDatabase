package Server;

/**
 * Class used to maintain the state of a transaction
 */
public class ResultSet {
	private String _type = null;
	private Object _val;
	
	public ResultSet(Object o) {
		_val = o;
		if (o != null)
			_type = o.getClass().getName();		
	}
	
	/**
	 * @return Class of object or null
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * @return Object containing value
	 */
	public Object getVal() {
		return _val;
	}
}
