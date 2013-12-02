package Server;

public enum State {
	ONLINE(1),
	OFFLINE(2),
	TPCSTART(3),
	TPCPREPARE(4),
	TPCWAIT(5),
	//PREABORT(6),
	//PRECOMMIT(7),
	TPCABORT(8),
	TPCCOMMIT(9),
	FINISH(10),
	ERROR(11),
	WAITFORLOCK(12);
	private int value;
	
	private State(int value){
		this.value = value;
	}
}
