package org.mca.math;

import java.rmi.RemoteException;

public class SubMatrixImpl implements SubMatrix {

	/** Width of the submatrix */
	public int width;

	/** Height of the submatrix */
	public int height;

	public double[][] values;

	/**
	 * 
	 * @param values
	 */
	public SubMatrixImpl (double[][] values) {
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
	public double get(int row, int column) throws RemoteException {
		return values[row][column];
	}

	@Override
	public void set(int row, int column, double value) throws RemoteException {
		values[row][column] = value;
	}

	@Override
	public double[] getNorthBorder() throws RemoteException {
		return values[0];
	}

	@Override
	public double[] getSouthBorder() throws RemoteException {
		return values[height - 1];
	}

	@Override
	public double[] getWestBorder() throws RemoteException {
		return getColumn(width-1);
	}

	@Override
	public double[] getEastBorder() throws RemoteException {
		return getColumn(0);
	}

	@Override
	public double[] getColumn(int numColumn) throws RemoteException {
		double[] column = new double[height];
		for (int i = 0; i < height; i++) {
			column[i] = values[i][numColumn];
		}
		return column;
	}

	@Override
	public double[] getRow(int numRow) throws RemoteException {
		return values[numRow];
	}
	
	@Override
	public double[][] getValues() throws RemoteException {
		return values;
	}

}
