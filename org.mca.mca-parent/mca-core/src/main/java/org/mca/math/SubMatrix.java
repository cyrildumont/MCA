package org.mca.math;

import java.rmi.RemoteException;

public interface SubMatrix<E> extends DataPart<E>{

	public int getRowDimension() throws RemoteException;

	public int getColumnDimension() throws RemoteException;
	
	public E[] getColumn(int numColumn) throws RemoteException;
	
	public E[] getRow(int numRow) throws RemoteException;
	
	public E get(int row, int column) throws RemoteException;
	
	public void set(int row, int column, E value) throws RemoteException;
	
	public E[] getNorthBorder() throws RemoteException;
	
	public E[] getSouthBorder() throws RemoteException;
	
	public E[] getWestBorder() throws RemoteException;

	public E[] getEastBorder() throws RemoteException;

	@Override
	public E[][] getValues() throws RemoteException;
}
