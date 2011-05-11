package org.mca.math;

import java.rmi.RemoteException;
import java.util.Collection;

public interface SubMatrix<E> extends DataPart<E>{

	public E get(int row, int column) throws RemoteException;
	
	public void set(int row, int column, E value) throws RemoteException;
	
	public Collection<E> getNorthBorder() throws RemoteException;
	
	public Collection<E> getSouthBorder() throws RemoteException;
	
	public Collection<E> getWestBorder() throws RemoteException;

	public Collection<E> getEastBorder() throws RemoteException;
}
