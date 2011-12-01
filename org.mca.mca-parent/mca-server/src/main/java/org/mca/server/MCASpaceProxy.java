package org.mca.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;

import net.jini.core.event.EventRegistration;

import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.RecoveryTaskStrategy;


public class MCASpaceProxy implements MCASpace, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7833611893651366342L;
	
	private MCASpaceServer remoteRef;
	
	public MCASpaceProxy(MCASpaceServer remoteRef) {
		this.remoteRef = remoteRef;
	}

	@Override
	public ComputationCase addCase(String name, String description)
			throws RemoteException, MCASpaceException {
		return remoteRef.addCase(name, description);
	}

	@Override
	public ComputationCase addCase(String name, String description, RecoveryTaskStrategy strategy)
	throws RemoteException, MCASpaceException{
		return remoteRef.addCase(name, description, strategy);
	}
	@Override
	public ComputationCase getCase(String name) throws RemoteException,
			MCASpaceException {
		return remoteRef.getCase(name);
	}

	@Override
	public ComputationCase getCase() throws RemoteException,
			MCASpaceException {
		return remoteRef.getCase();
	}
	
	@Override
	public Collection<ComputationCase> getCases() throws RemoteException,
			MCASpaceException {
		return remoteRef.getCases();
	}

	@Override
	public void removeCase(String name) throws RemoteException,
			MCASpaceException {
		remoteRef.removeCase(name);
	}
	
	@Override
	public EventRegistration register(MCASpaceEventListener listener, long leaseTime)
			throws RemoteException {
		return remoteRef.register(listener, leaseTime);
	}
}
