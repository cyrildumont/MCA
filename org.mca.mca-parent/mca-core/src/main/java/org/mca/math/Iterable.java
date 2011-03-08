package org.mca.math;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Iterable<E> extends Remote {

	public Iterator<E> iterator() throws RemoteException;

}
