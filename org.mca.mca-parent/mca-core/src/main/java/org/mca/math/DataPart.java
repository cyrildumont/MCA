package org.mca.math;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author Cyril Dumont
 *
 * @param <E>
 */
public interface DataPart extends Remote {

	public Object getValues() throws RemoteException;
		
}
