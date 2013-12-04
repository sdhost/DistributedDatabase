package Server;

public class LockManager {
	
	// Maintain for each bank account
	// - The type of lock that is currently held
	// - A list of transactions holding the lock
	// - a queue of lock requests
	
	
	// When a lock request arrives, chedck if any other transaction is
	// holding a conflicting lock. If no, then grant the lock and put the transaction
	// into the list. Otherwise put in the waiting queue.
	
	// Deadlock can be prevented by assigning a higher priority to the older transactions.
	// If a transaction T_i wants a lock from T_j, then abort T_j, otherwise T_i will be put into the wait queue.
	// In this way, only trasnactions with lower priority will wait for locks from transactions with higher priorities,
	// hence, there will not be deadlocks.
	
}
