package org.mca.server;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import net.jini.core.event.EventRegistration;

import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.RecoveryTaskStrategy;

import com.sun.jini.landlord.Landlord;
import com.sun.jini.start.ServiceProxyAccessor;

public interface MCASpaceServer extends ServiceProxyAccessor, Serializable, Remote, Landlord{

	/**
	 * 
	 * @param name
	 * @param description
	 * @return
	 * @throws RemoteException
	 * @throws MCASpaceException
	 */
	public ComputationCase addCase(String name, String description)
	throws RemoteException, MCASpaceException;

	/**
	 * 
	 * @param name
	 * @param description
	 * @param strategy
	 * @return
	 * @throws RemoteException
	 * @throws MCASpaceException
	 */
	public ComputationCase addCase(String name, String description, RecoveryTaskStrategy strategy)
	throws RemoteException, MCASpaceException;
	
	/**
	 * 
	 */
	public void removeCase(String name) throws RemoteException,
	MCASpaceException;

	/**
	 * 
	 * @return
	 * @throws RemoteException 
	 * @throws Exception
	 */
	public Collection<ComputationCase> getCases() throws RemoteException;

	/**
	 * 
	 * @return
	 * @throws MCASpaceException 
	 */
	public ComputationCase getCase(String name) throws RemoteException,
	MCASpaceException;

	/**
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public ComputationCase getCase() throws RemoteException;
	
	/**
	 * 
	 * @param listener
	 * @return
	 * @throws RemoteException
	 */
	public EventRegistration register(MCASpaceEventListener listener, long leaseTime) throws RemoteException;

}