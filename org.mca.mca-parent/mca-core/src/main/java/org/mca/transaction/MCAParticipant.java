package org.mca.transaction;

import java.io.Serializable;
import java.rmi.RemoteException;

import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.TransactionConstants;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.core.transaction.server.TransactionParticipant;
import net.jini.export.Exporter;
import net.jini.export.ProxyAccessor;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

public class MCAParticipant implements Serializable,TransactionParticipant,ProxyAccessor {

	TransactionParticipant proxy;
	
	public MCAParticipant() {
		try {
		     Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
							  new BasicILFactory());
		     proxy = (TransactionParticipant) exporter.export(this); 
		} catch (Exception e) {
			e.printStackTrace();
		}
}
	
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
		int result = prepare(arg0, arg1);
		if (result == TransactionConstants.PREPARED) {
			commit(arg0, arg1);
			result = TransactionConstants.COMMITTED;
		}
		return result;
	}

	public Object getProxy() {
		return this.proxy;
	}

}
