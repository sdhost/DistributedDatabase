package Server;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class debug {


	public static void main(String[] args) {
		
		ConcurrentHashMap<String, Long> txnTime = new ConcurrentHashMap<String, Long>();
		LinkedList<String> list = new LinkedList<String>();
		
		txnTime.put("1", 1l);
		txnTime.put("2", 2l);
		txnTime.put("3", 3l);
		
		list.add("2");
		list.add("3");
		list.add("1");
		
		for(String i:list){
			System.out.println(i);
		}
		
		Collections.sort(list, new timestampComparator(txnTime));
		
		for(String i:list){
			System.out.println(i);
		}
		
		
	}

}
