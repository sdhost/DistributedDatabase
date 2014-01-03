package Server;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class LockManager {
	
	private volatile ConcurrentHashMap<String, LinkedHashMap<String, Boolean>> tupleLocks; //Map<TupleID, Map<TxnID,LockType>>
													 // There are two types of lock, true for Exclusive lock
													 // and false for share lock
	private volatile ConcurrentHashMap<String, Long> txnTime;//Map<TxnId,Timestamp>, for all the transactions that hold the lock
	
	private volatile ConcurrentHashMap<String, LinkedList<String>> waitingQueue;// A queue for lock requests, LinkedHashMap<TupleId, LinkedList<TxnID+LockType>>
															 // TODO: Maybe need to have a priority queue? Refined later.
											
	//private volatile ConcurrentHashMap<String,Map<String,String>> message; // A error message holder, Map<TxnId,Map<TxnId, ErrorMessage>>
													// ErrorMessage will be split by tab("\t") if contains messages more than one column
	private TransactionManager _tm = null;
	
	private DataManager _dm = null;
	
	// Maintain for each bank account
	// - The type of lock that is currently held
	// - A list of transactions holding the lock
	// - a queue of lock requests
	public LockManager(TransactionManager tm, DataManager dm){
		this._tm = tm;
		this._dm = dm;
		this.tupleLocks = new ConcurrentHashMap<String, LinkedHashMap<String, Boolean>>();
		this.txnTime = new ConcurrentHashMap<String, Long>();
		this.waitingQueue = new ConcurrentHashMap<String, LinkedList<String>>();
		//this.message = new ConcurrentHashMap<String, Map<String,String>>();
	}
	
	// When a lock request arrives, check if any other transaction is
	// holding a conflicting lock. If no, then grant the lock and put the transaction
	// into the list. Otherwise put in the waiting queue.
	// Return false if the lock were succeed granted, otherwise return true
	// If error exist, call getError(gid, tupleId) for further information
	public boolean lock(String gid, String tupleId, Boolean type) throws Exception{
		if(!this.txnTime.containsKey(gid)){
//			Map<String,String> newError = new HashMap<String,String>();
//			String mess = "E\tCall prepareLocking to initialize the timestamp first";
//			newError.put(gid, mess);
//			this.message.put(gid, newError);
			return true;
		}
		if(!tupleLocks.containsKey(tupleId)){//No other transactions hold the lock
			LinkedHashMap<String, Boolean> newlock = new LinkedHashMap<String,Boolean>();
			newlock.put(gid, type);
			this.tupleLocks.put(tupleId, newlock);
			return false;
		}else{//There exist some other transactions hold the lock
			LinkedHashMap<String, Boolean> oldLock = this.tupleLocks.get(tupleId);
			if((oldLock.containsKey(gid) && type == false) || (oldLock.containsKey(gid) && type == oldLock.get(gid))){
				//Already granted
				return false;
			}
				
			LinkedList<String> abortTxn = new LinkedList<String>(); 
			boolean wait = false;
			boolean abortOther = false;
			for(Entry<String, Boolean> e:oldLock.entrySet()){
				if(e.getValue() || type){
					if(this.txnTime.get(e.getKey()) > this.txnTime.get(gid)){
						abortOther = true;
						abortTxn.add(e.getKey());
					}
					else{
						abortOther = false;
						wait = true;
						break;
					}
				}
					
			}
			if(!wait)//Check the waiting list, if there exist some transaction that has a earlier timestamp,
				//this transaction should wait to avoid aborted by that transaction
				for(Entry<String, LinkedList<String>> e:this.waitingQueue.entrySet()){
					for(String req:e.getValue()){
						String[] elem = req.split("\t");
						Boolean wtype = Boolean.valueOf(elem[1]);
						String wgid = elem[0];
						if(wtype || type){
							if(this.txnTime.get(wgid) < this.txnTime.get(gid)){
								abortOther = false;
								wait = true;
								break;
							}
						}
					}
				}
			if(wait){//This transaction should wait
				if(this.waitingQueue.containsKey(tupleId)){
					this.waitingQueue.get(tupleId).add(gid + "\t" + String.valueOf(type));
				}else{
					LinkedList<String> list = new LinkedList<String>();
					list.add(gid + "\t" + String.valueOf(type));
					this.waitingQueue.put(tupleId, list);
				}
				
//				Map<String,String> newError = new HashMap<String,String>();
//				String mess = "I\tWaiting for other transactions finish";
//				newError.put(gid, mess);
//				this.message.put(gid, newError);
				return true;
			}
			else if(abortOther){//All the other transactions hold the lock need to abort
//				Map<String,String> newError;
//				if(this.message.contains(gid))
//					newError = this.message.get(gid);
//				else
//					newError = new HashMap<String,String>();
				
				for(String txn:abortTxn){
					_tm._processedMultiSiteTxn.add(new ProcessedTransaction(txn,State.PREABORT));
					if(_tm._initiatedTxn.contains(txn)){
						_tm._coordinatorTxn.add(new ProcessedTransaction(txn, State.PREABORT));
					}
					_dm.Abort(txn);
					this.release(txn);
					//String mess = "O\tNeed to Abort";
					//newError.put(txn, mess);
				}
				this.tupleLocks.get(tupleId).put(gid, type);
				//this.message.put(gid, newError);
				return false;
			}else{//All share locks
				this.tupleLocks.get(tupleId).put(gid, type);
				return false;
			}
		}
	}
	//Before each transaction started to acquire lock, it need to have the creation time setted in the lock manager
	public void prepareLocking(String gid, long time){
		this.txnTime.put(gid, time);
	}
	
	public boolean isPrepared(String gid){
		return this.txnTime.containsKey(gid);
	}
	
//	public Map<String, String> getMessages(String gid){
//		return this.message.get(gid);
//	}
	
	
	
	//Release all the locks hold by a transaction, may be called in transaction commit or abort
	public boolean release(String gid){
		if(!this.txnTime.containsKey(gid)){//This transaction doesn't have any locks request in lockManager
//			Map<String,String> newInfo = new HashMap<String,String>();
//			String mess = "I\tNo lock request exist for this transaction";
//			newInfo.put(gid, mess);
//			this.message.put(gid, newInfo);
			return true;
		}
		else{//Since the simultaneous transaction will not be a lot, 
			// we will not use separate lock list for each transaction but search the whole lock tables for this transaction
			for(Entry<String, LinkedHashMap<String, Boolean>> entryTuple:this.tupleLocks.entrySet()){
				if(entryTuple.getValue().containsKey(gid)){
					entryTuple.getValue().remove(gid);
					if(this.waitingQueue.containsKey(entryTuple.getKey()) && !this.waitingQueue.get(entryTuple.getKey()).isEmpty()){
						LinkedList<Integer> processed = new LinkedList<Integer>();
						Collections.sort(this.waitingQueue.get(entryTuple.getKey()), new timestampComparator(this.txnTime));
						for(String req:this.waitingQueue.get(entryTuple.getKey())){
							
							String elem[] = req.split("\t");
							String txnId = elem[0];
							boolean type = Boolean.valueOf(elem[1]);
							if(entryTuple.getValue().isEmpty()){
								processed.add(this.waitingQueue.get(entryTuple.getKey()).indexOf(req));
								entryTuple.getValue().put(txnId, type);
							}else{
								LinkedList<String> abortTxn = new LinkedList<String>(); 
								boolean wait = false;
								boolean abortOther = false;
								for(Entry<String, Boolean> e:entryTuple.getValue().entrySet()){
									if(e.getValue() || type){
										if(this.txnTime.get(e.getKey()) > this.txnTime.get(gid)){
											abortOther = true;
											abortTxn.add(e.getKey());
										}
										else{
											abortOther = false;
											wait = true;
											break;
										}
									}
										
								}
								if(wait){//Later request will has a later timestamp, then none of the rest request can be granted
									break;
								}else if(abortOther){
									//This situation will not occur if the schedule is serializable
								}else{//All share locks
									processed.add(this.waitingQueue.get(entryTuple.getKey()).indexOf(req));
									entryTuple.getValue().put(txnId, type);
								}
							}
								
						}
						if(!processed.isEmpty()){
							for(Integer idx:processed)
								this.waitingQueue.get(entryTuple.getKey()).remove(idx.intValue());
						}
					}
				}
			}
			//Remove the entries in txnTime
			this.txnTime.remove(gid);
			return false;//May need to return some other informations if there exist some abort in processing the waiting requests
			
		}
		
	}
	
	
	//Check whether a tuple lock request has been granted
	//This function only check the granted list, will not check whether this request is in waiting queue
	public boolean isLocked(String gid, String tupleId){
		if(this.tupleLocks.get(tupleId).containsKey(gid))
			synchronized(this){
				this.notify();
				return true;
			}
		else
			return false;
	}
	
}
