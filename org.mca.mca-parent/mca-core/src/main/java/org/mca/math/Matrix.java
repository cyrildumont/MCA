package org.mca.math;

import java.io.File;

import org.mca.log.LogUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

@SuppressWarnings("serial")
public class Matrix<E> extends Data<E> {

	
	
	/**
	 * 
	 * @author cyril
	 *
	 */
	private class LocalPartInfo{
	
		private int partNumber;
		private int row;
		private int column;
		
		public LocalPartInfo(int partNumber, int row, int column) {
			this.partNumber = partNumber;
			this.row = row;
			this.column = column;
		}

	}
	
	/**
	 * 
	 * @author cyril
	 *
	 */
	public class Neighborhood{
		private SubMatrix<E> north;
		private SubMatrix<E> south;
		private SubMatrix<E> east;
		private SubMatrix<E> west;
		
		public SubMatrix<E> getNorth() {
			return north;
		}
		public SubMatrix<E> getSouth() {
			return south;
		}
		public SubMatrix<E> getEast() {
			return east;
		}
		public SubMatrix<E> getWest() {
			return west;
		}
		
	}
	
	private Integer m;
	
	private Integer n;
	
	private Integer rowSize;
	
	private Integer columnSize;
	
	public Matrix(int m, int n, int rowSize, int columnSize) {
		this.m = m;
		this.n = n;
		this.rowSize = rowSize;
		this.columnSize = columnSize;
	}
	
	/**
	 * 
	 * @param inputFile
	 * @param rowSize
	 * @param columnSize
	 */
	public Matrix(File inputFile, int rowSize, int columnSize){
		
	}

	/** Get row dimension.
	   @return     m, the number of rows.
	 */
	public int getRowDimension () {
		return m;
	}

	/** Get column dimension.
	   @return     n, the number of columns.
	 */
	public int getColumnDimension () {
		return n;
	}

	public E get(int row, int column) throws Exception{
		LocalPartInfo infosLocal = getlocalPartInfo(row, column);
		SubMatrix<E> vector = (SubMatrix<E>)dataParts.get(infosLocal.partNumber);
		return vector.get(infosLocal.row, infosLocal.column);
	}
	
	public void set(int row, int column, E value) throws Exception{
		LocalPartInfo infosLocal = getlocalPartInfo(row, column);
		SubMatrix<E> subMatrix = (SubMatrix<E>)dataParts.get(infosLocal.partNumber);
		subMatrix.set(infosLocal.row, infosLocal.column, value);
	}

	private LocalPartInfo getlocalPartInfo(int row, int column) throws Exception{
		if (row >= m || row <= 0 || column >= n || column <= 0 ) throw new Exception();
		int rowPartnumber = (int)Math.ceil(row / (double)rowSize);
		int columnPartnumber = (int)Math.ceil(column / (double)columnSize);
		int nbColumnParts = m%columnSize == 0 ? m / columnSize : m / columnSize  + 1;
		int partNumber = columnPartnumber + (rowPartnumber-1) * nbColumnParts;
		int rowLocal = row - ((rowPartnumber-1) * rowSize);
		int columnLocal = column - ((columnPartnumber-1) * columnSize);
		LogUtil.debug("l'élément [" + row + "," + column + "] se trouve dans la partie " +
				"[" + partNumber + "] à l'index [" + rowLocal + "," + columnLocal + "]", getClass());
		return new LocalPartInfo(partNumber, rowLocal, columnLocal);
	}
	
	
	public Neighborhood getNeighborhood(int part){
		LogUtil.debug("Part [" + part + "] neighborhood :", getClass());
		int nbColumnParts = m%columnSize == 0 ? m / columnSize : m / columnSize  + 1;
		int nbRowParts = n%rowSize == 0 ? n / rowSize : n / rowSize  + 1;
		int nbParts = nbColumnParts * nbRowParts;
		Neighborhood neighborhood = new Neighborhood();
		
		int numPartNorth = (part - nbColumnParts) < 1 ? -1 : part - nbColumnParts ;
		int numPartSouth = (part + nbColumnParts) > nbParts  ? -1 : part + nbColumnParts ;
		int row = (int)Math.ceil(part / (double)nbColumnParts);
		int numStartRow = (row - 1) * nbColumnParts + 1;
		int numEndRow = row * nbColumnParts;
		int numPartWest = (part - 1) < numStartRow  ? -1 : part - 1 ;
		int numPartEast = (part + 1) > numEndRow  ? -1 : part + 1 ;
		neighborhood.north = (SubMatrix<E>)getDataPart(numPartNorth);
		neighborhood.south = (SubMatrix<E>)getDataPart(numPartSouth);
		LogUtil.debug("\t North neighbor : Part [" + numPartNorth + "]" , getClass());
		LogUtil.debug("\t South neighbor : Part [" + numPartSouth + "]" , getClass());
		LogUtil.debug("\t West neighbor : Part [" + numPartWest + "]" , getClass());
		LogUtil.debug("\t East neighbor : Part [" + numPartEast + "]" , getClass());
		return neighborhood;
		
	}
	
	
	@Override
	protected void storeProperties(Element node) {
		node.setAttribute("nbRows", this.n.toString());		
		node.setAttribute("nbColumns", this.m.toString());
		node.setAttribute("rowPartSize", this.rowSize.toString());
		node.setAttribute("columnPartSize", this.columnSize.toString());
	}

	@Override
	protected void parseProperties(NamedNodeMap attributes) {
		m = Integer.valueOf(attributes.getNamedItem("nbRows").getNodeValue());
		n = Integer.valueOf(attributes.getNamedItem("nbColumns").getNodeValue());
	}
}
