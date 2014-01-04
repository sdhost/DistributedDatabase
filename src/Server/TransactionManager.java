package Server;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JOptionPane;

public class TransactionManager implements Serializable {
	private static final long serialVersionUID = -6594855415638227595L;
	private List<ServerCommunicationInterface> neighbour_server;
	private CommitCoordinator _2PC;	
	private Scheduler _scheduler;
	private Thread _2PCThread;
	private int serverId;
	public MultiTxnState multiTxnState;
	
	// Contain list of all txn, that are being 2PC coordinated by this server
	public ConcurrentLinkedQueue<ProcessedTransaction> _coordinatorTxn;
	
	// Contain coordinator id of each participant transfer txn on this server.
	public ConcurrentHashMap<String, Integer> _participantTxn;
	
	// Contains states of the transfer txns.
	public ConcurrentLinkedQueue<ProcessedTransaction> _processedMultiSiteTxn;
	
	// Contains participant server is, which are involved in the transfer txn initiated on this server 
	public ConcurrentHashMap<String, ArrayList<Integer>> _initiatedTxn = new ConcurrentHashMap<String, ArrayList<Integer>>();
	
	public TransactionManager(int serverId, MultiTxnState multiTxnState) throws IOException {
		this.multiTxnState = multiTxnState;
		this.serverId = serverId;
		_scheduler = new Scheduler(this);
		
		_coordinatorTxn = new ConcurrentLinkedQueue<ProcessedTransaction>();
		_processedMultiSiteTxn = new ConcurrentLinkedQueue<ProcessedTransaction>();
		_participantTxn = new ConcurrentHashMap<String, Integer>();
		
		_2PC = new CommitCoordinator(this);
		_2PCThread = new Thread(_2PC);
		_2PCThread.start();
	}
	
	public void abort(String gid) throws Exception {
		modalPopup(gid, StepMessages.PREABORT.value);
		if (_scheduler.abort(gid))
			modalPopup(gid, StepMessages.ABORTED.value);
		else
			modalPopup(gid, StepMessages.COMMITED.value);
	}
	
	public void commit(String gid){
		modalPopup(gid, StepMessages.PRECOMMIT.value);
		if (_scheduler.commit(gid))
			modalPopup(gid, StepMessages.COMMITED.value);
		else
			modalPopup(gid, StepMessages.ABORTED.value);
	}
	
	public void begin(String gid) {
		modalPopup(gid, StepMessages.BEGIN.value);
		_scheduler.begin(gid);
	}
	
	/**
	 * Create new account (only on connected server)
	 */
	public String txnCreatingAccounts(int balance, String gid, String uid, Long timestamp) {		
		_scheduler.prepareTx(gid, timestamp);	
		if (_scheduler.execute(Arrays.asList(new Operation().write(gid, uid, String.valueOf(balance))), gid, timestamp) != null) {
			// SUCCES
			commit(gid);
			return uid;
		} else {
			// PROBLEM
			try {abort(gid);} catch (Exception ex) {}
			ServerGUI.log("Problem with creating account");
			return null;
		}
	}
	
	/**
	 * Reads balance of account belonging to uid and returns value as string (only on connected server)
	 */
	public String txnCheckingBalance(String gid, String uid, Long timestamp) {		
		_scheduler.prepareTx(gid, timestamp);
		List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid)), gid, timestamp);
		if (rs != null) {
			// SUCCES
			commit(gid);
			return (String)rs.iterator().next().getVal();
		} else {
			// Error
			try {abort(gid);} catch (Exception e) {}
			ServerGUI.log("Problem with checking balance");
			return null;
		}
	}

	/**
	 * Deposit money for the account belonging to uid (only on connected server)
	 * Returns balance on account
	 */
	public String txnDeposit(String gid, String uid, int amount, Long timestamp) {
		_scheduler.prepareTx(gid, timestamp);

		//Read the current value
		List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid)), gid, timestamp);
		if (rs == null) {
			// Error
			try {abort(gid);} catch (Exception e) {}
			ServerGUI.log("Problem reading balance of account");
			return null;
		}
		
		int balance = Integer.valueOf((String)rs.iterator().next().getVal());
		int updatedBalance = balance + amount;
		
		// Write updatedBalance
		rs = _scheduler.execute(Arrays.asList(new Operation().write(gid, uid, String.valueOf(updatedBalance))), gid, timestamp);
		if (rs == null) {
			try {abort(gid);} catch (Exception e) {}
			ServerGUI.log("Problem writing updated balance of account");
			return null;
		}
		
		// Read new balance and return
		rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid)), gid, timestamp);
		if (rs != null) {
			// SUCCES
			commit(gid);
			return (String)rs.iterator().next().getVal();
		} else {
			// Error
			try {abort(gid);} catch (Exception e) {}
			ServerGUI.log("Problem with checking balance");
			return null;
		}
	}

	/**
	 * Withdraw money from the account belonging to user with uid (only on connected server)
	 * Returns balance on account
	 */
	public String txnWithdraw(String gid, String uid, int amount, Long timestamp) {
		_scheduler.prepareTx(gid, timestamp);
		
		// Read the current balance from the account
		List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid)), gid, timestamp);
		if (rs == null) {
			// Error
			try {abort(gid);} catch (Exception e) {}
			ServerGUI.log("Problem reading balance of account");
			return null;
		}		
		
		int balance = Integer.valueOf((String)rs.iterator().next().getVal());
		int updatedBalance = balance - amount;
		
		// Write updated amount
		rs = _scheduler.execute(Arrays.asList(new Operation().write(gid, uid, String.valueOf(updatedBalance))), gid, timestamp);
		if (rs == null) {
			try {abort(gid);} catch (Exception e) {}
			ServerGUI.log("Problem writing updated balance of account");
			return null;
		}
				
		// Read new balance and return
		rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid)), gid, timestamp);
		if (rs != null) {
			// SUCCES
			commit(gid);
			return (String)rs.iterator().next().getVal();
		} else {
			// Error
			try {abort(gid);} catch (Exception e) {}
			ServerGUI.log("Problem with checking balance");
			return null;
		}
	}

	/**
	 * Transfer money from one account to another (from connected server -> remote server)
	 * @param gid	global id of transaction
	 * @param uid1	transfer from this account (assumed on connected server)
	 * @param uid2	transfer to this account (can be on remote server)
	 * @param amount	amount of money to transfer
	 * @return	"1"
	 */
	public String txnTransfer(String gid, String uid1, String uid2, int amount,	Long timestamp) throws RemoteException {		
		_scheduler.prepareTx(gid, timestamp);
		
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
					
					//record the start log of this transaction
					multiTxnState.unfinishedTxn.put(gid, State.TPCSTART);
					
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
		int updatedBalance1 = 0;
		int updatedBalance2 = 0;
		boolean error = false;
		if(uid2AccountOnSvr == null) {
			
			modalPopup(gid, "Do read");
			
			// Read balance1 and balance2 from current connected server
			List<ResultSet> rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid1), new Operation().read(gid, uid2)), gid, timestamp);
			if (rs == null) {
				try {abort(gid);} catch (Exception e) {}
				ServerGUI.log("Problem with checking balance");
				return null;
			}
			
			modalPopup(gid, "Did read, do write");
			
			balance1 = Integer.valueOf(((String)rs.get(0).getVal()));
			balance2 = Integer.valueOf(((String)rs.get(1).getVal()));
			updatedBalance1 = balance1 - amount;
			updatedBalance2 = balance2 + amount;
			
			rs = _scheduler.execute(Arrays.asList(new Operation().write(gid, uid1, String.valueOf(updatedBalance1)), new Operation().write(gid, uid2, String.valueOf(updatedBalance2))), gid, timestamp);
			if (rs == null) {
				try {abort(gid);} catch (Exception e) {}
				ServerGUI.log("Problem with writing updated balance");
				return null;
			}
			
			modalPopup(gid, "Did write, do commit");
			
			commit(gid);
			
			modalPopup(gid, "Did commit");
			
			// Return balance of account belonging to uid1
			return String.valueOf(updatedBalance1);
		}else{
			
			// Store gid -> remote serverid
			_initiatedTxn.put(gid, new ArrayList<Integer>(Arrays.asList(uid2AccountOnSvr.getServerID())));
			this.multiTxnState.unfinishedTxn.put(gid, State.TPCSTART);
			
			modalPopup(gid, "read 2");
			// Read balance2 from remote serverid
			List<ResultSet> rs = uid2AccountOnSvr.remoteExecute(Arrays.asList(new Operation().read(gid, uid2)), gid, timestamp, serverId);
			if (rs == null) {
				// TODO: Right?
				modalPopup(gid, StepMessages.PREABORT.value);
				ServerGUI.log("Problem with checking balance");
			}else{
			
				balance2 = Integer.valueOf(((String)rs.get(0).getVal()));
				updatedBalance2 = balance2 + amount;

				//Write to balance2
				rs = uid2AccountOnSvr.remoteExecute(Arrays.asList(new Operation().write(gid, uid2, String.valueOf(updatedBalance2))), gid, timestamp, serverId);	
				if (rs == null) {
					try {abort(gid);} catch (Exception e) {}
					ServerGUI.log("Problem with writing updated balance");
				}else{
					//Set precommit in remote server
					uid2AccountOnSvr.remoteExecute(new ArrayList<Operation>(), gid, timestamp, serverId);
				}
			}
			
			modalPopup(gid, "read 1");
			// Read balance1 from current connected server and balance2 from connected server
			 rs = _scheduler.execute(Arrays.asList(new Operation().read(gid, uid1)), gid, timestamp);
			if (rs == null) {
				modalPopup(gid, StepMessages.PREABORT.value);
				ServerGUI.log("Problem with checking balance");
				this._processedMultiSiteTxn.add(new ProcessedTransaction(gid, State.PREABORT));
				this.multiTxnState.unfinishedTxn.put(gid, State.PREABORT);
				error = true;
			}else{
				balance1 = Integer.valueOf(((String)rs.iterator().next().getVal()));
				//Update balance 1
				updatedBalance1 = balance1 - amount;
				//Write to balance1
				rs = _scheduler.execute(Arrays.asList(new Operation().write(gid, uid1, String.valueOf(updatedBalance1))), gid, timestamp);
				if (rs == null) {
					modalPopup(gid, StepMessages.PREABORT.value);
					ServerGUI.log("Problem with writing updated balance");
					this._processedMultiSiteTxn.add(new ProcessedTransaction(gid, State.PREABORT));
					this.multiTxnState.unfinishedTxn.put(gid, State.PREABORT);
				}else{
					this._processedMultiSiteTxn.add(new ProcessedTransaction(gid, State.PRECOMMIT));
					this.multiTxnState.unfinishedTxn.put(gid, State.PRECOMMIT);
				}
			}
			
			modalPopup(gid, "read finished");
			//update coordinator txn state
			for(ProcessedTransaction txn: this._processedMultiSiteTxn){
				if(txn.getGid() == gid){
					this._coordinatorTxn.add(new ProcessedTransaction(gid, txn.getState()));
					break;
				}
			}
			
			//detect if transaction is finished
			boolean finished;
			do{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				finished = this.multiTxnState.finishedTxn.containsKey(gid);
			}
			while(finished == false);
			
			return "1";
		}
		
		
		
	}

	public void initNeighbours(List<ServerCommunicationInterface> neighbours){
		neighbour_server = neighbours;
	}
	
	public List<ResultSet> executeRemote(List<Operation> ops, String gid, long timestamp, int sid){
		
		//Log the start state
		if(this.multiTxnState.unfinishedTxn.containsKey(gid) == false){
			this.multiTxnState.unfinishedTxn.put(gid,State.TPCSTART);
			this._participantTxn.put(gid, sid);
		}
		
		if(ops.isEmpty()){
			this._processedMultiSiteTxn.add(new ProcessedTransaction(gid, State.PRECOMMIT));
			return null;
		}
		
		_scheduler.prepareTx(gid, timestamp);
		
		List<ResultSet> ret = this._scheduler.execute(ops, gid, timestamp);
		if(ret == null){
			try {
				this._scheduler.abort(gid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this._processedMultiSiteTxn.add(new ProcessedTransaction(gid, State.PREABORT));
		}
		return ret;
	}

	public boolean exists(String tupleId) {
		return this._scheduler.isInServer(tupleId);
	}
	
	public void modalPopup(String gid, String message) {
		if (ServerGUI.getChckbxUsePopups().isSelected())
			JOptionPane.showMessageDialog(ServerGUI.getFrame(), "gid: " + gid + ":: " + message);	
	}
}
