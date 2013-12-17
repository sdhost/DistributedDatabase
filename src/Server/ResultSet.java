package Server;

public class ResultSet {
	String type;
	Object val;
	
	public ResultSet(Object o){
		this.val = o;
		this.type = o.getClass().getName();
	}
	
	public String getType() {
		return type;
	}
	
	public Object getVal() {
		return val;
	}
	
	
	
}
