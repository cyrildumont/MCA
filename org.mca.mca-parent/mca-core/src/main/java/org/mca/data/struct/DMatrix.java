package org.mca.data.struct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.jini.space.JavaSpace05;

import org.mca.data.DDataStructure;
import org.mca.data.DataPartLocal;
import org.mca.data.DataPartRemote;
import org.mca.data.Dimension;
import org.mca.data.format.DataFormat;
import org.mca.data.format.DoubleMatrixFormat;
import org.mca.data.format.FormatException;
import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class DMatrix<E> extends DDataStructure<E> {

	private static final long serialVersionUID = 1L;

	private static final DataFormat DEFAULT_MATRIX_FORMAT = new DoubleMatrixFormat();

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

	private double[][] values;

	public Integer rowSize;

	public Integer columnSize;

	public Integer rowPartSize;

	public Integer columnPartSize;

	/** Constructor for JavaSpaces specification */
	public DMatrix() {
		format = DEFAULT_MATRIX_FORMAT;
	}

	public DMatrix(DataFormat format,
			Dimension dimension, Dimension partDimension) {
		super(format);
		this.rowSize = dimension.getHeight();
		this.columnSize = dimension.getWidth();
		this.rowPartSize = partDimension.getHeight();
		this.columnPartSize = partDimension.getWidth();
	}

	public DMatrix(Dimension dimension, 
			Dimension partDimension) {
		this(DEFAULT_MATRIX_FORMAT,dimension, partDimension);
	}

	public DMatrix(double[][] values, 
			Dimension partDimension) {
		super(DEFAULT_MATRIX_FORMAT);
		rowSize = values.length;
		columnSize = values[0].length;
		this.rowPartSize = partDimension.getHeight();
		this.columnPartSize = partDimension.getWidth();
		this.values = values;
	}

	/**
	 * 
	 * @return
	 */
	public Dimension getNbPartDimension(){
		int nbColumnParts = columnSize%columnPartSize == 0 ?
				columnSize / columnPartSize : columnSize / columnPartSize  + 1;
		int nbRowParts = rowSize%rowPartSize == 0 ?
				rowSize / rowPartSize : rowSize / rowPartSize  + 1;
		return new Dimension(nbRowParts, nbColumnParts);
	}

	/** Get row dimension.
	   @return     m, the number of rows.
	 */
	public int getRowDimension () {
		return rowSize;
	}

	/** Get column dimension.
	   @return     n, the number of columns.
	 */
	public int getColumnDimension () {
		return columnSize;
	}
	
	/**
	 * 
	 * @return
	 */
	public Dimension getDimension(){
		return new Dimension(rowSize,columnSize);
	}

//	public double get(int row, int column) throws Exception{
//		LocalPartInfo infosLocal = getlocalPartInfo(row, column);
//		SubMatrix vector = (SubMatrix)parts.get(infosLocal.partNumber);
//		return vector.get(infosLocal.row, infosLocal.column);
//	}
//
//	public void set(int row, int column, double value) throws Exception{
//		LocalPartInfo infosLocal = getlocalPartInfo(row, column);
//		SubMatrix subMatrix = (SubMatrix)dataParts.get(infosLocal.partNumber);
//		subMatrix.set(infosLocal.row, infosLocal.column, value);
//	}

	private LocalPartInfo getlocalPartInfo(int row, int column) throws Exception{
		if (row >= rowSize || row <= 0 || column >= columnSize || column <= 0 ) throw new Exception();
		int rowPartnumber = (int)Math.ceil(row / (double)rowPartSize);
		int columnPartnumber = (int)Math.ceil(column / (double)columnPartSize);
		int nbColumnParts = columnSize%columnPartSize == 0 ?
				columnSize / columnPartSize : columnSize / columnPartSize  + 1;
		int partNumber = columnPartnumber + (rowPartnumber-1) * nbColumnParts;
		int rowLocal = row - ((rowPartnumber-1) * rowPartSize);
		int columnLocal = column - ((columnPartnumber-1) * columnPartSize);
		LogUtil.debug("l'élément [" + row + "," + column + "] se trouve dans la partie " +
				"[" + partNumber + "] à l'index [" + rowLocal + "," + columnLocal + "]", getClass());
		return new LocalPartInfo(partNumber, rowLocal, columnLocal);
	}


	public Neighborhood getNeighborhood(int part){
		logger.fine("Part [" + part + "] neighborhood :");
		int nbColumnParts = columnSize%columnPartSize == 0 ?
				columnSize / columnPartSize : columnSize / columnPartSize  + 1;
		int nbRowParts = rowSize%rowPartSize == 0 ?
				rowSize / rowPartSize : rowSize / rowPartSize  + 1;
		int nbParts = nbColumnParts * nbRowParts;
		Neighborhood neighborhood = new Neighborhood();
		int numPartNorth = (part - nbColumnParts) < 1 ? -1 : part - nbColumnParts ;
		int numPartSouth = (part + nbColumnParts) > nbParts  ? -1 : part + nbColumnParts ;
		int row = (int)Math.ceil(part / (double)nbColumnParts);
		int numStartRow = (row - 1) * nbColumnParts + 1;
		int numEndRow = row * nbColumnParts;
		int numPartWest = (part - 1) < numStartRow  ? -1 : part - 1 ;
		int numPartEast = (part + 1) > numEndRow  ? -1 : part + 1 ;
		neighborhood.setNorth(numPartNorth == -1 ? null : (SubMatrix)getDataPart(numPartNorth));
		neighborhood.setSouth(numPartSouth == -1 ? null : (SubMatrix)getDataPart(numPartSouth));
		neighborhood.setEast(numPartEast == -1 ? null : (SubMatrix)getDataPart(numPartEast));
		neighborhood.setWest(numPartWest == -1 ? null : (SubMatrix)getDataPart(numPartWest));
		logger.fine("\t North neighbor : Part [" + numPartNorth + "]");
		logger.fine("\t South neighbor : Part [" + numPartSouth + "]");
		logger.fine("\t West neighbor : Part [" + numPartWest + "]");
		logger.fine("\t East neighbor : Part [" + numPartEast + "]");
		return neighborhood;

	}


	@Override
	protected void storeProperties(Element node) {
		node.setAttribute("nbRows", this.rowSize.toString());		
		node.setAttribute("nbColumns", this.columnSize.toString());
		node.setAttribute("rowPartSize", rowPartSize.toString());
		node.setAttribute("columnPartSize", columnPartSize.toString());
	}

	@Override
	protected void parseProperties(NamedNodeMap attributes) {
		rowSize = Integer.valueOf(attributes.getNamedItem("nbRows").getNodeValue());
		columnSize = Integer.valueOf(attributes.getNamedItem("nbColumns").getNodeValue());
		rowPartSize = Integer.valueOf(attributes.getNamedItem("rowPartSize").getNodeValue());
		columnPartSize = Integer.valueOf(attributes.getNamedItem("columnPartSize").getNodeValue());
	}

	
	public Dimension getPartDimension(){
		return new Dimension(rowPartSize, columnPartSize);
	}
	
	@Override
	protected DataPartLocal generatePart(Object values) throws Exception {
		return new SubMatrixLocal((double[][])values);
	}
	
	@Override
	protected DataPartRemote generateRemotePart(int part,JavaSpace05 space) throws Exception {
		return new SubMatrixRemote(part,space);
	}
	
	@Override
	public int getNbParts() {
		int nbColumnParts = columnSize%columnPartSize == 0 ?
				columnSize / columnPartSize : columnSize / columnPartSize  + 1;
		int nbRowParts = rowSize%rowPartSize == 0 ?
				rowSize / rowPartSize : rowSize / rowPartSize  + 1;
		return nbColumnParts * nbRowParts;
	}

	private double[][] getPart(int i0, int i1, int j0, int j1){
		double[][] part = Arrays.copyOfRange(values, i0, i1);
		for (int i = 0; i < part.length; i++) {
			part[i] = Arrays.copyOfRange(part[i], j0, j1);
		}

		return part;
	}

	@Override
	protected void deployPart(int part, ComputationCase cc,
			DataHandlerFactory factory) throws MCASpaceException {

		int nbColumnParts = columnSize%columnPartSize == 0 ?
				columnSize / columnPartSize : columnSize / columnPartSize  + 1;

		int rowPart = (int)Math.ceil((double)part / nbColumnParts);
		int row = ( rowPart -1) * rowPartSize;
		int columnPart = part%nbColumnParts == 0 ? 
				nbColumnParts : part%nbColumnParts;
		int column = (columnPart - 1) * columnPartSize;	
		File file = generatePartFile(part);
		try {
			DataHandler dh = factory.getDataHandler(file,name,part);
			file.createNewFile();
			if(values != null){
				double[][] partValues = 
					getPart(row, row + rowPartSize, column, column + columnPartSize);
				format.format(partValues, file);
			}
			InputStream input = new FileInputStream(file);
			dh.upload(input);
			input.close();
			cc.addDataHandler(dh);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}		
	}

	public void setRowPartSize(Integer rowPartSize) {
		this.rowPartSize = rowPartSize;
	}

	public void setColumnPartSize(Integer columnPartSize) {
		this.columnPartSize = columnPartSize;
	}

	public void setRowSize(Integer rowSize) {
		this.rowSize = rowSize;
	}

	public void setColumnSize(Integer columnSize) {
		this.columnSize = columnSize;
	}


}
