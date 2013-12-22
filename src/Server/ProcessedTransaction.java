package Server;


/**
 * Scheduler should create this object for each transaction after finishing its processing.
 * @author lsu
 *
 */
public class ProcessedTransaction {
	String gid;
	//state: PRECOMMIT or PREABORT
	State state;
	
	public ProcessedTransaction(String gid, State state){
		this.gid = gid;
		this.state = state;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	
}
