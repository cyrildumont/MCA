package org.mca.transaction;

import java.io.Serializable;
import java.rmi.RemoteException;

import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.TransactionConstants;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.core.transaction.server.TransactionParticipant;

public class MCAParticipant implements Serializable,TransactionParticipant {

	public void abort(TransactionManager tm, long arg1)
	throws UnknownTransactionException, RemoteException {
		System.out.println("aborting ..." + this);
	}

	public void commit(TransactionManager arg0, long arg1)
	throws UnknownTransactionException, RemoteException {
		System.out.println("committing...");		
	}

	public int prepare(TransactionManager arg0, long arg1)
	throws UnknownTransactionException, RemoteException {
		System.out.println("preparing...");
		return TransactionConstants.PREPARED;
	}

	public int prepareAndCommit(TransactionManager arg0, long arg1)
	throws UnknownTransactionException, RemoteException {
		System.out.println("preparing and commit ...");
		int result = prepare(arg0, arg1);
		if (result == TransactionConstants.PREPARED) {
			commit(arg0, arg1);
			result = TransactionConstants.COMMITTED;
		}
		return result;
	}

}
