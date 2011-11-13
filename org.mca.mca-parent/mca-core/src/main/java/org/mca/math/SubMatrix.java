package org.mca.math;

import java.rmi.RemoteException;

public interface SubMatrix extends DataPart{

	public int getRowDimension() throws RemoteException;

	public int getColumnDimension() throws RemoteException;
	
	public double[] getColumn(int numColumn) throws RemoteException;
	
	public double[] getRow(int numRow) throws RemoteException;
	
	public double get(int row, int column) throws RemoteException;
	
	public void set(int row, int column, double value) throws RemoteException;
	
	public double[] getNorthBorder() throws RemoteException;
	
	public double[] getSouthBorder() throws RemoteException;
	
	public double[] getWestBorder() throws RemoteException;

	public double[] getEastBorder() throws RemoteException;

	@Override
	public double[][] getValues() throws RemoteException;
}
