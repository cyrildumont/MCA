package org.mca.math;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.math.format.DataFormat;
import org.mca.math.format.DoubleMatrixFormat;
import org.mca.math.format.FormatException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class DistributedMatrix<E> extends DistributedData<E> {

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

	private E[][] values;

	public Integer m;

	public Integer n;

	public Integer rowPartSize;

	public Integer columnPartSize;

	/** Constructor for JavaSpaces specification */
	public DistributedMatrix() {
		super(null,null);

	}

	public DistributedMatrix(String name, DataFormat<E> format,
			Dimension dimension, Dimension partDimension) {
		super(name, format);
		this.m = dimension.getHeight();
		this.n = dimension.getWidth();
		this.rowPartSize = partDimension.getHeight();
		this.columnPartSize = partDimension.getWidth();
	}

	public DistributedMatrix(String name,Dimension dimension, 
			Dimension partDimension) {
		this(name,DEFAULT_MATRIX_FORMAT,dimension, partDimension);
	}

	public DistributedMatrix(String name,E[][] values, 
			Dimension partDimension) {
		super(name,DEFAULT_MATRIX_FORMAT);
		m = values.length;
		n = values[0].length;
		this.rowPartSize = partDimension.getHeight();
		this.columnPartSize = partDimension.getWidth();
		this.values = values;
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
		int rowPartnumber = (int)Math.ceil(row / (double)rowPartSize);
		int columnPartnumber = (int)Math.ceil(column / (double)columnPartSize);
		int nbColumnParts = n%columnPartSize == 0 ?
				n / columnPartSize : n / columnPartSize  + 1;
		int partNumber = columnPartnumber + (rowPartnumber-1) * nbColumnParts;
		int rowLocal = row - ((rowPartnumber-1) * rowPartSize);
		int columnLocal = column - ((columnPartnumber-1) * columnPartSize);
		LogUtil.debug("l'élément [" + row + "," + column + "] se trouve dans la partie " +
				"[" + partNumber + "] à l'index [" + rowLocal + "," + columnLocal + "]", getClass());
		return new LocalPartInfo(partNumber, rowLocal, columnLocal);
	}


	public Neighborhood<E> getNeighborhood(int part){
		LogUtil.debug("Part [" + part + "] neighborhood :", getClass());
		int nbColumnParts = n%columnPartSize == 0 ?
				n / columnPartSize : n / columnPartSize  + 1;
		int nbRowParts = m%rowPartSize == 0 ?
				m / rowPartSize : m / rowPartSize  + 1;
		int nbParts = nbColumnParts * nbRowParts;
		Neighborhood<E> neighborhood = new Neighborhood<E>();

		int numPartNorth = (part - nbColumnParts) < 1 ? -1 : part - nbColumnParts ;
		int numPartSouth = (part + nbColumnParts) > nbParts  ? -1 : part + nbColumnParts ;
		int row = (int)Math.ceil(part / (double)nbColumnParts);
		int numStartRow = (row - 1) * nbColumnParts + 1;
		int numEndRow = row * nbColumnParts;
		int numPartWest = (part - 1) < numStartRow  ? -1 : part - 1 ;
		int numPartEast = (part + 1) > numEndRow  ? -1 : part + 1 ;
		neighborhood.setNorth(numPartNorth == -1 ? null : (SubMatrix<E>)getDataPart(numPartNorth));
		neighborhood.setSouth(numPartSouth == -1 ? null : (SubMatrix<E>)getDataPart(numPartSouth));
		neighborhood.setEast(numPartEast == -1 ? null : (SubMatrix<E>)getDataPart(numPartEast));
		neighborhood.setWest(numPartWest == -1 ? null : (SubMatrix<E>)getDataPart(numPartWest));
		LogUtil.debug("\t North neighbor : Part [" + numPartNorth + "]" , getClass());
		LogUtil.debug("\t South neighbor : Part [" + numPartSouth + "]" , getClass());
		LogUtil.debug("\t West neighbor : Part [" + numPartWest + "]" , getClass());
		LogUtil.debug("\t East neighbor : Part [" + numPartEast + "]" , getClass());
		return neighborhood;

	}


	@Override
	protected void storeProperties(Element node) {
		node.setAttribute("nbRows", this.m.toString());		
		node.setAttribute("nbColumns", this.n.toString());
		node.setAttribute("rowPartSize", rowPartSize.toString());
		node.setAttribute("columnPartSize", columnPartSize.toString());
	}

	@Override
	protected void parseProperties(NamedNodeMap attributes) {
		m = Integer.valueOf(attributes.getNamedItem("nbRows").getNodeValue());
		n = Integer.valueOf(attributes.getNamedItem("nbColumns").getNodeValue());
		rowPartSize = Integer.valueOf(attributes.getNamedItem("rowPartSize").getNodeValue());
		columnPartSize = Integer.valueOf(attributes.getNamedItem("columnPartSize").getNodeValue());
	}

	@Override
	public int getNbParts() {
		int nbColumnParts = n%columnPartSize == 0 ?
				n / columnPartSize : n / columnPartSize  + 1;
		int nbRowParts = m%rowPartSize == 0 ?
				m / rowPartSize : m / rowPartSize  + 1;
		return nbColumnParts * nbRowParts;
	}

	private E[][] getPart(int i0, int i1, int j0, int j1){
		E[][] part = Arrays.copyOfRange(values, i0, i1);
		for (int i = 0; i < part.length; i++) {
			part[i] = Arrays.copyOfRange(part[i], j0, j1);
		}
		return part;
	}
	
	@Override
	protected void deployPart(int part, ComputationCase cc,
			DataHandlerFactory factory) throws MCASpaceException {

		int nbColumnParts = n%columnPartSize == 0 ?
				n / columnPartSize : n / columnPartSize  + 1;

		int rowPart = (int)Math.ceil((double)part / nbColumnParts);
		int row = ( rowPart -1) * rowPartSize;
		int columnPart = part%nbColumnParts == 0 ? 
				nbColumnParts : part%nbColumnParts;
		int column = (columnPart - 1) * columnPartSize;	
		File file = generatePartFile(part);
		E[][] partValues = 
			getPart(row, row + rowPartSize, column, column + columnPartSize);
		try {
			format.format(partValues, file);
			DataHandler dh = factory.getDataHandler(file);
			FileInputStream input = new FileInputStream(file);
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

}
