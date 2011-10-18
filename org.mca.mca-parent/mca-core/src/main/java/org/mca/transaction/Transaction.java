package org.mca.transaction;

import java.rmi.RemoteException;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.UnknownTransactionException;

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
	
	public void abort() throws MCATransactionException{
		try {
			created.transaction.abort();
		} catch (UnknownTransactionException e) {
			e.printStackTrace();
			throw new MCATransactionException();
		} catch (CannotAbortException e) {
			e.printStackTrace();
			throw new MCATransactionException();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new MCATransactionException();
		}
	}
	
	public void commit() throws MCATransactionException{
		try {
			created.transaction.commit();
		} catch (UnknownTransactionException e) {
			e.printStackTrace();
			throw new MCATransactionException();
		} catch (CannotCommitException e) {
			e.printStackTrace();
			throw new MCATransactionException();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new MCATransactionException();
		}
	}
	
}