package org.mca.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;

import net.jini.core.event.EventRegistration;

import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.MCASpaceException;

@SuppressWarnings("serial")
public class MCASpaceProxy implements MCASpace, Serializable {

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
	public EventRegistration register(MCASpaceEventListener listener)
			throws RemoteException {
		return remoteRef.register(listener);
	}
}
