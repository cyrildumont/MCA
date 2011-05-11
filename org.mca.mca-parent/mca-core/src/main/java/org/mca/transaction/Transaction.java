package org.mca.transaction;

import net.jini.core.lease.Lease;

public class Transaction {

	private net.jini.core.transaction.Transaction.Created created;
	
	public Transaction(net.jini.core.transaction.Transaction.Created created) {
		this.created = created;
	}
	
	public net.jini.core.transaction.Transaction getTransaction(){
		return created.transaction;
	}
	
	public Lease getLease() {
		return created.lease;
	}
	
}