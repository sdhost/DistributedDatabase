package Server;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TransactionManager implements Serializable {
	private Thread _2PCThread;
	private Scheduler _scheduler;
	private CommitCoordinator _2PC;	
	private List<ServerCommunicationInterface> neighbour_server;
	public ConcurrentLinkedQueue<ProcessedTransaction> _processedMultiSiteTxn;
	
	public volatile ConcurrentHashMap<String, ArrayList<Integer>> _initiatedTxn = new ConcurrentHashMap<String, ArrayList<Integer>>();
	
	public TransactionManager() throws IOException {
		_scheduler = new Scheduler(this);
		_2PC = new CommitCoordinator(this);
		_processedMultiSiteTxn = new ConcurrentLinkedQueue<ProcessedTransaction>();
		
		_2PCThread = new Thread(_2PC);
		_2PCThread.start();
	}
	
	public String txnCreatingAccounts(int balance, String gid, String uid, Long timestamp) {
		
		// Create list of operations to apply
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().write(gid, uid, String.valueOf(balance)));

		// Send to scheduler
		if (_scheduler.execute(toApply, gid, timestamp) != null) {
			// SUCCES
		} else {
			// PROBLEM
		}
		
		// What to return?
		return null;
	}
	
	public void abort(String gid) {
		
		// TODO:abort transaction gid
		
	}
	
	public String txnCheckingBalance(String gid, String uid, Long timestamp) {
		// Create list of operations to apply
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().read(gid, uid));

		// Send to scheduler
		List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
		ResultSet result = rs.iterator().next();
		
		if(result == null)
			return null;
		else
			return (String)result.getVal();
	}

	public String txnDeposit(String gid, String uid, int amount, Long timestamp) {
		
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().read(gid, uid));

		//Read the current value
		List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
		ResultSet result = rs.iterator().next();
		
		int balance = Integer.valueOf((String)result.getVal());
		
		toApply.clear();
		toApply.add(new Operation().write(gid, uid, String.valueOf(balance + amount)));
		
		rs = _scheduler.execute(toApply, gid, timestamp);
		result = rs.iterator().next();
		
		if(result == null)
			return null;
		else
			return this.txnCheckingBalance(gid, uid, timestamp);
	}

	public String txnWithdraw(String gid, String uid, int amount, Long timestamp){
		
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().read(gid, uid));

		//Read the current value
		List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
		ResultSet result = rs.iterator().next();
		
		int balance = Integer.valueOf((String)result.getVal());
		
		//Add error check if needed, just remove comment for following code and deal with the exception with outer function
//		if(balance < amount){
//			this.abort();
//			throw new Exception("Not Enough Money, require " + amount + ", available " + balance);
//		}
			
		
		toApply.clear();
		toApply.add(new Operation().write(gid, uid, String.valueOf(balance - amount)));
		
		rs = _scheduler.execute(toApply, gid, timestamp);
		result = rs.iterator().next();
				
		if(result == null)
			return null;
		else
			return this.txnCheckingBalance(gid, uid, timestamp);
	}

	//Assume uid1 are in this server, uid2 in remote server or this server
	public String txnTransfer(String gid, String uid1, String uid2, int amount,
			Long timestamp) throws RemoteException {
		boolean exist = false;
		ServerCommunicationInterface svr2 = null;
		if(this.isExist(uid2)){
			exist = true;
		}else{
			for(ServerCommunicationInterface svr:this.neighbour_server){
				if(svr.isExist(uid2)){
					svr2 = svr;
					exist = true;
					break;
				}
			}
		}
		
		if(!exist){
			ServerGUI.log(uid2 + " accounts not exist in all the servers");
			return null;
		}
		int balance1, balance2;
		
		
		List<Operation> toApply = new ArrayList<Operation>();
		toApply.add(new Operation().read(gid, uid1));
		ResultSet result;
		if(svr2 == null){
			toApply.add(new Operation().read(gid, uid2));
			
			List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
			result = rs.get(0);
			balance1 = Integer.valueOf(((String)result.getVal()));
			
			result = rs.get(1);
			balance2 = Integer.valueOf(((String)result.getVal()));
		}else{
			ArrayList<Integer> list = new ArrayList<Integer>();
			list.add(svr2.getServerID());
			this._initiatedTxn.put(gid, list);
			List<ResultSet> rs = _scheduler.execute(toApply, gid, timestamp);
			result = rs.get(0);
			balance1 = Integer.valueOf(((String)result.getVal()));
			
			toApply.clear();
			toApply.add(new Operation().read(gid, uid2));
			rs = svr2.remoteExecute(toApply, gid, timestamp);
			
			result = rs.get(0);
			balance2 = Integer.valueOf(((String)result.getVal()));
		}
		
		
		
		
		
		
		//Add error check if needed, just remove comment for following code and deal with the exception with outer function
//		if(balance1 < amount){
//			this.abort();
//			throw new Exception("Not Enough Money, require " + amount + ", available " + balance);
//		}
		
		balance1 -= amount;
		balance2 += amount;
		toApply.clear();
		toApply.add(new Operation().write(gid, uid1, String.valueOf(balance1)));
		
		
		
		if(svr2 == null){
			toApply.add(new Operation().write(gid, uid2, String.valueOf(balance2)));
			
			_scheduler.execute(toApply, gid, timestamp);
		}else{
			_scheduler.execute(toApply, gid, timestamp);
			toApply.clear();
			toApply.add(new Operation().write(gid, uid2, String.valueOf(balance2)));
			svr2.remoteExecute(toApply, gid, timestamp);
		}
		
		
		if(result == null)
			return null;
		else
			return this.txnCheckingBalance(gid, uid1, timestamp);
	}

	public void initNeighbour(List<ServerCommunicationInterface> neighbour){
		this.neighbour_server = neighbour;
	}
	
	public List<ResultSet> executeRemote(List<Operation> ops, String gid, long timestamp){
		
		return this._scheduler.execute(ops, gid, timestamp);
	}

	public boolean isExist(String tupleId){
		return this._scheduler.isInServer(tupleId);
	}

	public void commit(String gid) {
		// TODO Auto-generated method stub
		
	}
	


}
