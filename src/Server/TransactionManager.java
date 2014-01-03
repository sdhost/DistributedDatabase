package Server;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TransactionManager implements Serializable {
	private static final long serialVersionUID = -6594855415638227595L;
	private List<ServerCommunicationInterface> neighbour_server;
	private CommitCoordinator _2PC;	
	private Scheduler _scheduler;
	private Thread _2PCThread;
	private int serverId;
	public MultiTxnState multiTxnState;
	
	// Contain list of all txn, that is being 2PC coordinated by this server
	public ConcurrentLinkedQueue<ProcessedTransaction> _coordinatorTxn;
	
	// Contain coordinator id of each participant txn on this server.
	public ConcurrentHashMap<String, Integer> _participantTxn;
	
	// Contains? (somebody add info!)
	public ConcurrentLinkedQueue<ProcessedTransaction> _processedMultiSiteTxn;
	
	// Contains? (somebody add info!) 
	public ConcurrentHashMap<String, ArrayList<Integer>> _initiatedTxn = new ConcurrentHashMap<String, ArrayList<Integer>>();
	
	
	public TransactionManager(int serverId, MultiTxnState multiTxnState) throws IOException {
		this.multiTxnState = multiTxnState;
		this.serverId = serverId;
		_scheduler = new Scheduler(this);
		_2PC = new CommitCoordinator(this);
		_coordinatorTxn = new ConcurrentLinkedQueue<ProcessedTransaction>();
		_processedMultiSiteTxn = new ConcurrentLinkedQueue<ProcessedTransaction>();
		_participantTxn = new ConcurrentHashMap<String, Integer>();
		_2PCThread = new Thread(_2PC);
		_2PCThread.start();
	}
	
	public void abort(String gid) throws Exception {
		_scheduler.abort(gid);
	}
	
	public void commit(String gid){
		_scheduler.commit(gid);
	}
	
	/**
	 * Create new account (only on connected server)
	 */
	public String txnCreatingAccounts(int balance, String gid, String uid, Long timestamp) {
		if (_scheduler.execute(Arrays.asList(new Operation().write(gid, uid, String.valueOf(balance))), gid, timestamp) != null) {
			// SUCCES
			return uid;
		} else {
			// PROBLEM
			ServerGUI.log("Problem with creating account");
			return null;
		}
	}
	
	/**
	 * Reads balance of account belonging to uid and returns value as string (only on connected server)
	 */
	public String txnCheckingBalance(String gid, String uid, Long timestamp) {
		List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid)), gid, timestamp);
		if (rs != null) {
			// SUCCES
			return (String)rs.iterator().next().getVal();
		} else {
			// Error
			return null;
		}
	}

	/**
	 * Deposit money for the account belonging to uid (only on connected server)
	 * Returns balance on account
	 */
	public String txnDeposit(String gid, String uid, int amount, Long timestamp) {

		//Read the current value
		List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid)), gid, timestamp);
		if (rs == null)
			return null;
		
		int balance = Integer.valueOf((String)rs.iterator().next().getVal());
		int updatedBalance = balance + amount;
		
		// Write updatedBalance
		rs = _scheduler.execute(Arrays.asList(new Operation().write(gid, uid, String.valueOf(updatedBalance))), gid, timestamp);
		if (rs == null)
			return null;
		
		// Return new amount of money
		return txnCheckingBalance(gid, uid, timestamp);
	}

	/**
	 * Withdraw money from the account belonging to user with uid (only on connected server)
	 * Returns balance on account
	 */
	public String txnWithdraw(String gid, String uid, int amount, Long timestamp){

		// Read the current balance from the account
		List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid)), gid, timestamp);
		if (rs == null)
			return null;
		
		int balance = Integer.valueOf((String)rs.iterator().next().getVal());
		int updatedBalance = balance - amount;

		if (updatedBalance >= 0) {
			// Write updated amount
			rs = _scheduler.execute(Arrays.asList(new Operation().write(gid, uid, String.valueOf(updatedBalance))), gid, timestamp);
			if (rs == null)
				return null;
		}
		
		return this.txnCheckingBalance(gid, uid, timestamp);
	}

	/**
	 * Transfer money from one account to another (from connected server -> remote server)
	 * @param gid	global id of transaction
	 * @param uid1	transfer from this account (assumed on connected server)
	 * @param uid2	transfer to this account (can be on remote server)
	 * @param amount	amount of money to transfer
	 * @return	balance of account belonging to uid1
	 */
	public String txnTransfer(String gid, String uid1, String uid2, int amount,	Long timestamp) throws RemoteException {
		
		/**
		 * This transaction is spanning multiple servers, and should be coordinated by 2PC
		 * This server assumes the responsibility as a 2PC coordinator
		 */
		
		
		/**
		 * Check if uid2 exists on any of the connected servers (including the currently connected one)
		 */
		boolean uid2Exists = false;
		ServerCommunicationInterface uid2AccountOnSvr = null;
		if(this.exists(uid2)){
			uid2Exists = true;
		} else {
			for(ServerCommunicationInterface svr: neighbour_server) {
				if(svr.isExist(uid2)){
					uid2AccountOnSvr = svr;
					uid2Exists = true;
					break;
				}
			}
		}
		
		if(!uid2Exists) {
			ServerGUI.log(uid2 + " account does not exist in the servers");
			return null;
		}
		
		
		/**
		 * Read balance1 and 2 (from uid1 and uid2)
		 */
		int balance1, balance2;		
		if(uid2AccountOnSvr == null) {
			// Read balance1 and balance2 from current connected server
			List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid1), new Operation().read(gid, uid2)), gid, timestamp);
			if (rs == null)
				return null;
			
			balance1 = Integer.valueOf(((String)rs.get(0).getVal()));
			balance2 = Integer.valueOf(((String)rs.get(1).getVal()));
		}else{
			// Store gid -> remote serverid
			_initiatedTxn.put(gid, new ArrayList<Integer>(Arrays.asList(uid2AccountOnSvr.getServerID())));
			
			// Read balance1 from current connected server and balance2 from connected server
			List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid1)), gid, timestamp);
			if (rs == null)
				return null;
			balance1 = Integer.valueOf(((String)rs.iterator().next().getVal()));
			
			// Read balance2 from remote serverid
			rs = uid2AccountOnSvr.remoteExecute(Arrays.asList(new Operation().read(gid, uid2)), gid, timestamp, serverId);
			if (rs == null)
				return null;
			balance2 = Integer.valueOf(((String)rs.get(0).getVal()));
		}
		
		/**
		 * Calculate updated balances and write if there is enough money
		 */
		int updatedBalance1 = balance1 - amount;
		int updatedBalance2 = balance2 + amount;
		if (updatedBalance1 >= 0) {
			if(uid2AccountOnSvr == null){				
				// Update uid1 and uid2 on connected server (as accounts are both here)				
				List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().write(gid, uid1, String.valueOf(updatedBalance1)), new Operation().write(gid, uid2, String.valueOf(updatedBalance2))), gid, timestamp);
				if (rs == null)
					return null;
			}else{				
				// Update uid1 on connected server and uid2 on remote server
				List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().write(gid, uid1, String.valueOf(balance1))), gid, timestamp);
				if (rs == null)
					return null;
				uid2AccountOnSvr.remoteExecute(Arrays.asList(new Operation().write(gid, uid2, String.valueOf(balance2))), gid, timestamp, serverId);
			}
		}
		
		// Update the ProcessedTransaction state to precommit
		for (ProcessedTransaction pt : _processedMultiSiteTxn) {
			if (pt.getGid().equals(gid))
				pt.setState(State.PRECOMMIT);
			if(_initiatedTxn.contains(gid)){
				_coordinatorTxn.add(new ProcessedTransaction(gid, State.PRECOMMIT));
			}
		}
		
		// Return balance of account belonging to uid1
		return txnCheckingBalance(gid, uid1, timestamp);
	}

	public void initNeighbours(List<ServerCommunicationInterface> neighbours){
		neighbour_server = neighbours;
	}
	
	public List<ResultSet> executeRemote(List<Operation> ops, String gid, long timestamp, int sid){
		_participantTxn.put(gid, sid);
		return this._scheduler.execute(ops, gid, timestamp);
	}

	public boolean exists(String tupleId) {
		return this._scheduler.isInServer(tupleId);
	}
}
