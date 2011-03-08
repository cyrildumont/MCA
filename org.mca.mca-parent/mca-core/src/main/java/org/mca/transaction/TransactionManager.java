/**
 * 
 */
package org.mca.transaction;


import java.rmi.RemoteException;

import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mca.log.LogUtil;
import org.mca.lookup.Lookup;

/**
 * @author Cyril
 *
 */
public class TransactionManager {

	/** Log */
	private final static Log LOG = LogFactory.getLog(TransactionManager.class);
	
	public static Transaction create(){
		
		Lookup finder = new Lookup(net.jini.core.transaction.server.TransactionManager.class);
		net.jini.core.transaction.server.TransactionManager transactionManager =
			(net.jini.core.transaction.server.TransactionManager) finder.getService();
		

		LOG.debug("A TransactionManager has been discovered : " + transactionManager);
		Transaction tnx = null;
		
		try {
			tnx = TransactionFactory.create(transactionManager,Long.MAX_VALUE).transaction;
		} catch (LeaseDeniedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		LOG.debug("A Transaction has been created : " + tnx);
		return tnx;
	}

	/**
	 * 
	 * @param host
	 * @return
	 * @throws Exception 
	 */
	public static Transaction create(String host) throws Exception{
		
		Lookup finder = new Lookup(net.jini.core.transaction.server.TransactionManager.class);
		net.jini.core.transaction.server.TransactionManager transactionManager =
			(net.jini.core.transaction.server.TransactionManager) finder.getService(host);
		if (transactionManager == null) {
			LogUtil.error("No TransactionManager found on " + host, TransactionManager.class);
			throw new Exception();
		}
		LOG.debug("A TransactionManager has been discovered : " + transactionManager);
		Transaction tnx = null;
		try {
			tnx = TransactionFactory.create(transactionManager,Long.MAX_VALUE).transaction;
			
		} catch (LeaseDeniedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		LOG.debug("A Transaction has been created : " + tnx);
		return tnx;
	}
}
