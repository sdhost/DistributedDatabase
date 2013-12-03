package Server;

public enum State {
	ONLINE(1),
	OFFLINE(2),
			//State 1, 2 for Server state
	TPCSTART(3),
	TPCPREPARE(4),
	TPCWAIT(5),
	//PREABORT(6),
	//PRECOMMIT(7),
	TPCABORT(8),
	TPCCOMMIT(9),
			//State 3 ~ 9 for 2PC state
	FINISH(10),
	ERROR(11),
	WAITFORLOCK(12);
			//State 10 ~ 12 for transaction execution state
	private int value;
	
	private State(int value){
		this.value = value;
	}
}
