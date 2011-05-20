/**
 * 
 */
package org.mca.server;

import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

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
import org.mca.log.LogUtil;

/**
 * @author Cyril
 *
 */
@SuppressWarnings("serial")
public abstract class JavaSpaceParticipant implements Serializable{

	/**  */
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
	protected Collection<Entry> readEntry(Collection<Entry> templates, Transaction txn) throws MCASpaceException{
		try {
			Collection<Entry> entries = new ArrayList<Entry>();
			MatchSet set = space.contents(templates, txn, Lease.ANY, Integer.MAX_VALUE);
			while (true) {
				Entry e = set.next();
				if (e == null)
					break;
				entries.add(e);
			}
			return entries;
		} catch (RemoteException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		} catch (TransactionException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		} catch (UnusableEntryException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
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
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		} catch (RemoteException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
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
	protected Entry readEntry(Entry template, Transaction txn, long timeToWait) 
			throws EntryNotFoundException, MCASpaceException {
		try {
			Entry entry = space.read(template, txn, timeToWait);
			if (entry == null) throw new EntryNotFoundException(template, host);
			return entry;
		} catch (RemoteException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		} catch (TransactionException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		} catch (UnusableEntryException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		} catch (InterruptedException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		}
	}

	/**
	 * 
	 * @param template
	 * @return
	 */
	protected Entry readEntry(Entry template, Transaction txn) 
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
	protected Entry takeEntry(Entry template, Transaction txn, Long timeout) 		
			throws MCASpaceException  {
		try {
			Entry entry = space.take(template, txn, timeout);
			return entry;
		} catch (Exception e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			e.printStackTrace();
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
	protected Collection<Entry> takeEntry(Collection<Entry> templates, Transaction txn) throws MCASpaceException{
		try {
			return space.take(templates, txn, 0, Integer.MAX_VALUE);
		} catch (RemoteException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		} catch (UnusableEntriesException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		} catch (TransactionException e) {
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
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
			LogUtil.error("[" + host + "]" + e.getMessage(),getClass());
			throw new MCASpaceException();
		}
	}

}
