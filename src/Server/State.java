package Server;

import java.io.Serializable;

public enum State implements Serializable {
	EMPTY(0),
	ONLINE(1),
	OFFLINE(2),
	NONEXIST(3),
			//State 1, 2, 3 for Server state
	TPCSTART(4),
	TPCPREPARE(5),
	
	TPCWAIT(6),
	PREABORT(7),
	PRECOMMIT(8),
	TPCABORT(9),
	TPCCOMMIT(10),
			//State 4 ~ 10 for 2PC state
	FINISH(11),
	ERROR(12),
	PROCESSING(13);

			//State 10 ~ 12 for transaction execution state
	public int value;
	
	private State(int value){
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
}
