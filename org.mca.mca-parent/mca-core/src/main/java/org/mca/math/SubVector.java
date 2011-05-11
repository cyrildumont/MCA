package org.mca.math;

import java.rmi.RemoteException;

public interface SubVector<E> extends DataPart<E>,Iterable<E>{	
	
	public E get(int index) throws RemoteException;
	
	public void set(int index, E value) throws RemoteException;
	
}
