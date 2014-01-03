package Server;

public enum StepMessages {
	BEGIN("Transaction Begin"),
	PRECOMMIT("Succes - Do commit"),
	COMMITED("Commit done"),
	PREABORT("Problem - do abort"),
	ABORTED("Abort done"),
	;
	
	
	public String value;
	
	private StepMessages(String val){
		this.value = val;
	}
}
