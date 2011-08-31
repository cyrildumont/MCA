package org.mca.math;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class SubMatrixImpl<E> implements SubMatrix<E> {

	/** Width of the submatrix */
	public int width;

	/** Height of the submatrix */
	public int height;

	public E[][] values;

	/**
	 * 
	 * @param values
	 */
	public SubMatrixImpl (E[][] values) {
		height = values.length;
		width = values[0].length;
		this.values = values;
	}
	
	@Override
	public int getColumnDimension() throws RemoteException {
		return width;
	}
	
	@Override
	public int getRowDimension() throws RemoteException {
		return height;
	}
	
	@Override
	public E get(int row, int column) throws RemoteException {
		return values[row][column];
	}

	@Override
	public void set(int row, int column, E value) throws RemoteException {
		values[row][column] = value;
	}

	@Override
	public E[] getNorthBorder() throws RemoteException {
		return values[0];
	}

	@Override
	public E[] getSouthBorder() throws RemoteException {
		return values[height - 1];
	}

	@Override
	public E[] getWestBorder() throws RemoteException {
		return getColumn(width-1);
	}

	@Override
	public E[] getEastBorder() throws RemoteException {
		return getColumn(0);
	}

	@Override
	public E[] getColumn(int numColumn) throws RemoteException {
		List<E> result = new ArrayList<E>();
		for (int i = 0; i < height; i++) {
			result.set(i,values[i][numColumn]);
		}
		return (E[])result.toArray();
	}

	@Override
	public E[] getRow(int numRow) throws RemoteException {
		return values[numRow];
	}
	
	@Override
	public E[][] getValues() throws RemoteException {
		return values;
	}

}
