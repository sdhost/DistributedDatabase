package Server;

public class ResultSet {
	String type;
	Object val;
	
	public ResultSet(Object o){
		if(o == null)
			this.type = null;
		else
			this.type = o.getClass().getName();
		this.val = o;
		
	}
	
	public String getType() {
		return type;
	}
	
	public Object getVal() {
		return val;
	}
	
	public State isSuccess(){
		if( !(this.type == null))
			return State.PRECOMMIT;
		else return State.PREABORT;
	}
	
}
