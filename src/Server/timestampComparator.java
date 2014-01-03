package Server;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

public class timestampComparator implements Comparator<String> {
	
	private ConcurrentHashMap<String, Long> txnTime;
	
	public timestampComparator(ConcurrentHashMap<String, Long> txnTime){
		this.txnTime = txnTime;
	}
	
	@Override
	public int compare(String o1, String o2) {
		if(txnTime.get(o1) < txnTime.get(o2))
			return -1;
		else if(txnTime.get(o1) == txnTime.get(o2))
			return 0;
		else
			return 1;
	}

}
