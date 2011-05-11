/**
 * 
 */
package org.mca.transaction;


import java.rmi.RemoteException;

import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.NestableServerTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.lookup.Lookup;

/**
 * @author Cyril Dumont
 * @version 1.0
 *
 */
public class TransactionManager {

	/** Log */
	private final static Log LOG = LogFactory.getLog(TransactionManager.class);
	
	public static Transaction create(String host, long leaseTime){
		
		Lookup finder = new Lookup(net.jini.core.transaction.server.TransactionManager.class);
		net.jini.core.transaction.server.TransactionManager transactionManager =
			(net.jini.core.transaction.server.TransactionManager) finder.getService(host);
		
		LOG.debug("A TransactionManager has been discovered : " + transactionManager);
		net.jini.core.transaction.Transaction.Created tnx = null;
		try {
			tnx = TransactionFactory.create(transactionManager,leaseTime);
		} catch (LeaseDeniedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		LOG.debug("A Transaction has been created : " + tnx);
		Transaction transaction = new Transaction(tnx);
		return transaction;
	}
}
