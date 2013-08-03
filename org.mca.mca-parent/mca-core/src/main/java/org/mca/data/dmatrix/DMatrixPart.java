package org.mca.data.dmatrix;

import org.mca.data.DataPart;


public interface DMatrixPart extends DataPart{
	
	static final String COLUMN_NAME_ENTRY = "COLUMN";
	
	static final String ROW_NAME_ENTRY = "ROW";
	
	static final String PART_NAME_ENTRY = "PART";
	
	public int getRowDimension();

	public int getColumnDimension();
	
	public double[] getColumn(int numColumn) throws Exception;
	
	public double[] getRow(int numRow) throws Exception;
	
	public double[] getNorthBorder() throws Exception;
	
	public double[] getSouthBorder() throws Exception;
	
	public double[] getWestBorder() throws Exception;

	public double[] getEastBorder() throws Exception;

}
