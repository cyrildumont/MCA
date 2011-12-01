/**
 * 
 */
package org.mca.javaspace;

import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.entry.UnusableEntriesException;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace05;
import net.jini.space.MatchSet;

import org.mca.javaspace.exceptions.EntryNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;

/**
 * @author Cyril Dumont
 *
 */

public abstract class JavaSpaceParticipant implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.JavaSpaceParticipant";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	protected JavaSpace05 space;

	protected String host;

	public void setSpace(JavaSpace05 space) {
		this.space = space; 
	}

	/**
	 * 
	 * @param templates
	 * @return
	 * @throws MCASpaceException
	 */
	protected <T extends Entry> Collection<T> readEntry(Collection<T> templates, Transaction txn) throws MCASpaceException{
		try {
			Collection<T> entries = new ArrayList<T>();
			MatchSet set = space.contents(templates, txn, Lease.ANY, Integer.MAX_VALUE);
			while (true) {
				Entry e = set.next();
				if (e == null)
					break;
				entries.add((T)e);
			}
			return entries;
		} catch (RemoteException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		} catch (TransactionException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		} catch (UnusableEntryException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		}
	}

	/**
	 * 
	 * @param entry
	 * @param transaction
	 * @throws MCASpaceException
	 * @throws RemoteException
	 */
	protected void writeEntry(Entry entry, Transaction transaction) throws MCASpaceException{
		try {
			space.write(entry, transaction, Lease.FOREVER);
		} catch (TransactionException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		} catch (RemoteException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		}
	}

	/**
	 * 
	 * @param template
	 * @param txn
	 * @param timeToWait
	 * @return
	 * @throws EntryNotFoundException
	 * @throws MCASpaceException
	 */
	protected <T extends Entry> T readEntry(T template, Transaction txn, long timeToWait) 
	throws EntryNotFoundException, MCASpaceException {
		T entry = null;
		try {
			entry = (T)space.read(template, txn, timeToWait);
		} catch (Exception e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		}
		if (entry == null) throw new EntryNotFoundException(template, host);
		return entry;
	}

	/**
	 * 
	 * @param template
	 * @return
	 */
	protected <T extends Entry> T readEntry(T template, Transaction txn) 
	throws EntryNotFoundException, MCASpaceException {
		return readEntry(template, txn, JavaSpace05.NO_WAIT);
	}



	/**
	 * 
	 * @param template
	 * @param txn
	 * @param timeout
	 * @return
	 * @throws MCASpaceException
	 * @throws EntryNotFoundException
	 */
	protected <T extends Entry> T takeEntry(T template, Transaction txn, Long timeout) 		
	throws MCASpaceException  {
		try {

			T entry = (T)space.take(template, txn, timeout);
			return entry;
		} catch (Exception e) {
			logger.warning("JavaSpaceParticipant - take entry error :" + e.getMessage());
			logger.throwing("JavaSpaceParticipant", "takeEntry", e);
			throw new MCASpaceException();
		}
	}
	/**
	 * 
	 * @param template
	 * @return
	 * @throws EntryNotFoundException 
	 */
	protected Entry takeEntry(Entry template, Transaction txn) 
	throws MCASpaceException {
		return takeEntry(template, txn, JavaSpace05.NO_WAIT);
	}

	/**
	 * 
	 * @param templates
	 * @param txn
	 * @return
	 * @throws MCASpaceException
	 */
	protected Collection<? extends Entry> takeEntries(Collection<?> templates, Transaction txn, int maxEntries) throws MCASpaceException{
			return takeEntries(templates, txn, maxEntries, JavaSpace05.NO_WAIT);
	}
	
	protected Collection<? extends Entry> 
		takeEntries(Collection<?> templates, Transaction txn, int maxEntries, Long timeout) throws MCASpaceException{
		try {
			return space.take(templates, txn, timeout, maxEntries);
		} catch (RemoteException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		} catch (UnusableEntriesException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		} catch (TransactionException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		}
	}
	
	/**
	 * 
	 * @param entries
	 * @param txn
	 * @throws MCASpaceException
	 */
	protected void writeEntries(List<? extends Entry> entries, Transaction txn) throws MCASpaceException{
		List<Long> leases = Collections.nCopies(entries.size(),Long.MAX_VALUE);
		try {
			space.write(entries, txn, leases);
		} catch (RemoteException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		} catch (TransactionException e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		}
	}


	/**
	 * 
	 * @param tmpls
	 * @param txn
	 * @param visibilityOnly
	 * @param remote
	 * @param leaseDuration
	 * @param handback
	 * @throws MCASpaceException
	 */
	protected void registerForAvailabilityEvent(Collection tmpls, 
			Transaction txn, boolean visibilityOnly, Remote remote, 
			long leaseDuration, Object handback) throws MCASpaceException{
		try {
			RemoteEventListener proxy = null;
			MarshalledObject mobject = null;
			if(remote != null){
				Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),new BasicILFactory());
				proxy = (RemoteEventListener) exporter.export(remote);
			}
			if (handback != null) mobject = new MarshalledObject(handback);
			space.registerForAvailabilityEvent(tmpls, txn, visibilityOnly, proxy, leaseDuration, mobject);
		} catch (Exception e) {
			logger.warning("[" + host + "]" + e.getMessage());
			throw new MCASpaceException();
		}
	}

}
