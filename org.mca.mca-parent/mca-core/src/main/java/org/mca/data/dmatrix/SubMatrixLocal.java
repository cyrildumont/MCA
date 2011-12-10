package org.mca.data.struct;

import org.mca.data.DataPartInfo;
import org.mca.data.DataPartLocal;


public class SubMatrixLocal extends DataPartLocal implements SubMatrix {

	/** Width of the submatrix */
	public int width;

	/** Height of the submatrix */
	public int height;

	public double[][] values;
	
	/**
	 * 
	 * @param values
	 * @throws Exception 
	 */
	public SubMatrixLocal (double[][] values) throws Exception {
		super();
		height = values.length;
		width = values[0].length;
		this.values = values;
	}
	
	@Override
	public int getColumnDimension() {
		return width;
	}
	
	@Override
	public int getRowDimension() {
		return height;
	}

	@Override
	public double[] getNorthBorder() throws Exception {
		return values[0];
	}

	@Override
	public double[] getSouthBorder() throws Exception {
		return values[height - 1];
	}

	@Override
	public double[] getWestBorder() throws Exception {
		return getColumn(0);
	}

	@Override
	public double[] getEastBorder() throws Exception {
		return getColumn(width-1);
	}

	@Override
	public double[] getColumn(int numColumn) throws Exception {
		double[] column = new double[height];
		for (int i = 0; i < height; i++) {
			column[i] = values[i][numColumn];
		}
		return column;
	}

	@Override
	public double[] getRow(int numRow) throws Exception {
		return values[numRow];
	}

	@Override
	public double[][] getValues() {
		return values;
	}
	
	@Override
	public DataPartInfo getInfos() {
		return new SubMatrixInfo(height,width);
	}
	
	public void sendColumn(int numColumn) throws Exception{
		sendData(COLUMN_NAME_ENTRY + "-" + numColumn, getColumn(numColumn));
	}
	
	public void sendRow(int numRow) throws Exception{
		sendData(ROW_NAME_ENTRY + "-" + numRow, getRow(numRow));
	}
	
	public void sendNorthBorder() throws Exception {
		sendRow(0);
	}

	public void sendSouthBorder() throws Exception {
		sendRow(height - 1);
	}

	public void sendWestBorder() throws Exception {
		sendColumn(0);
	}

	public void sendEastBorder() throws Exception {
		sendColumn(width-1);
	}
	
	public void sendBorders() throws Exception {
		sendRow(0);
		sendRow(height - 1);
		sendColumn(width-1);
		sendColumn(0);
	}
	
}
