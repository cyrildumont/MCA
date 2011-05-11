/**
 * 
 */
package org.mca.javaspace;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;

import org.mca.javaspace.exceptions.MCASpaceException;

/**
 * Interface of a MCASpace service
 * 
 * @author Cyril Dumont
 * @version 1.0
 *
 */
public interface MCASpace extends Remote {

	
	public final static int ADD_CASE = 1;
	
	public final static int REMOVE_CASE = 2;
	
	
	public ComputationCase addCase(String name, String description) throws RemoteException,MCASpaceException;
	
	public ComputationCase getCase(String name) throws RemoteException, MCASpaceException;
	
	public Collection<ComputationCase> getCases() throws RemoteException,MCASpaceException;
	
	public void removeCase(String name) throws RemoteException,MCASpaceException;
	
	public EventRegistration register(MCASpaceEventListener listener) throws RemoteException;
	
}

