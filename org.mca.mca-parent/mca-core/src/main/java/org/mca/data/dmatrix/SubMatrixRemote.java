package org.mca.data.struct;

import net.jini.space.JavaSpace05;

import org.mca.data.DataPartRemote;

public class SubMatrixRemote extends DataPartRemote implements SubMatrix{

	private static final long serialVersionUID = 1L;

	/** Width of the submatrix */
	public int width;

	/** Height of the submatrix */
	public int height;
	
	public SubMatrixRemote(int part, JavaSpace05 javaspace) throws Exception {
		super(part, javaspace);
		if(javaspace != null){
			SubMatrixInfo info = new SubMatrixInfo();
			info = readEntry(info, null);
			width = info.width;
			height = info.height;
		}
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
	public double[] getColumn(int numColumn) throws Exception {
		return (double[])recv(COLUMN_NAME_ENTRY + "-" + numColumn);
	}

	@Override
	public double[] getRow(int numRow) throws Exception {
		return (double[])recv(ROW_NAME_ENTRY + "-" + numRow);
	}

	@Override
	public double[] getNorthBorder() throws Exception {
		return getRow(0);
	}

	@Override
	public double[] getSouthBorder() throws Exception {
		return getRow(height - 1);
	}

	@Override
	public double[] getWestBorder() throws Exception {
		return getColumn(0);
	}

	@Override
	public double[] getEastBorder() throws Exception {
		return getColumn(width-1);
	}
	
}
