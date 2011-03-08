package org.mca.math;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Iterator<E> extends Remote {

	public boolean hasNext() throws RemoteException;

	public E next() throws RemoteException;
	

}
