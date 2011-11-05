package org.mca.math;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.math.format.DataFormat;
import org.mca.math.format.DoubleVectorFormat;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class DistributedVector<E> extends DistributedData<E> {

	private static final long serialVersionUID = 1L;

	private static final DataFormat DEFAULT_VECTOR_FORMAT = new DoubleVectorFormat();

	public Integer size;
	public Integer partSize;

	private E[] values;

	/** Constructor for JavaSpaces specification */
	public DistributedVector() {}

	public DistributedVector(DataFormat<E> format, E[] values, int partSize) {
		this(format, values.length, partSize);
		
		this.values = values;
	}

	public DistributedVector(E[] values, int partSize) {
		this(DEFAULT_VECTOR_FORMAT, values, partSize);
	}

	public DistributedVector(DataFormat<E> format, int size, int partSize) {
		super(format);
		this.size = size;
		this.partSize = partSize;
	}

	public Integer getSize() {
		return size;
	}

	public Integer getPartSize() {
		return partSize;
	}

	public E get(int index) throws Exception{
		if (index >= size ) throw new Exception();
		int num = index / partSize;
		int indexLocal = index - (num * partSize)  ;
		LogUtil.debug("l'élément [" + index + "] se trouve dans la partie [" + num + "] à l'index [" + indexLocal + "]", getClass());
		SubVector<E> vector = (SubVector<E>)dataParts.get(num);
		return vector.get(indexLocal);
	}

	public void set(int index, E value) throws Exception{
		if (index >= size ) throw new Exception();
		int num = index / partSize;
		int indexLocal = index - (num * partSize)  ;
		LogUtil.debug("l'élément [" + index + "] se trouve dans la partie [" + num + "] à l'index [" + indexLocal + "]", getClass());
		SubVector<E> vector = (SubVector<E>)dataParts.get(num);
		vector.set(indexLocal, value);
	}

	@Override
	protected void storeProperties(Element node) {
		node.setAttribute("size", this.size.toString());
		node.setAttribute("partSize", this.partSize.toString());
	}

	@Override
	protected void parseProperties(NamedNodeMap attributes) {
		size = Integer.valueOf(attributes.getNamedItem("size").getNodeValue());
		partSize = Integer.valueOf(attributes.getNamedItem("partSize").getNodeValue());
	}

	@Override
	public int getNbParts() {
		return size % partSize == 0 
						? size / partSize : size / partSize + 1;
	}

	@Override
	protected DataPart<E> generatePart(Object values) {
		return new SubVectorImpl<E>((E[])values);
	}
	/**
	 * 
	 * @param i
	 * @param cc
	 * @param factory
	 * @throws MCASpaceException
	 */
	protected void deployPart(int part, ComputationCase cc,
			DataHandlerFactory factory) throws MCASpaceException {

		try {
			File file = generatePartFile(part);
			file.createNewFile();
			if(values != null){
				int start = partSize * (part-1);
				int end = part < getNbParts() ? partSize * part : size;
				List<E> partValues = Arrays.asList(Arrays.copyOfRange(values, start, end));
				format.format(partValues, file);
			}
			DataHandler dh = factory.getDataHandler(file);
			FileInputStream input = new FileInputStream(file);
			dh.upload(input);
			input.close();
			cc.addDataHandler(dh);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}
	}	
}
