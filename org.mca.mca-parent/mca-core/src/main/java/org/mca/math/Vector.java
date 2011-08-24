package org.mca.math;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.math.format.DataFormat;
import org.mca.math.format.DoubleVectorFormat;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class Vector<E> extends Data<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -439178253802638286L;
	
	private static final DataFormat DEFAULT_VECTOR_FORMAT = new DoubleVectorFormat();
	
	public Integer size;
	public Integer partSize;
	
	private E[] values;

	/** Constructor for JavaSpaces specification */
	public Vector() {
		super(null,null);
	}
	
	public Vector(String name, DataFormat<E> format, E[] values, int partSize) {
		super(name, format);
		this.size = values.length;
		this.values = values;
		this.partSize = partSize;
	}
	
	public Vector(String name, E[] values, int partSize) {
		this(name, DEFAULT_VECTOR_FORMAT, values, partSize);
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
	protected int getNbParts() {
		return size % partSize == 0 
							? size / partSize : size / partSize + 1;
	}
	
	@Override
	public void deploy(ComputationCase cc, DataHandlerFactory factory) throws MCASpaceException {
		
		int nbParts = getNbParts();
		for (int i = 1; i <= nbParts; i++) {
			deployPart(i,cc,factory);
		}

	}

	/**
	 * 
	 * @param i
	 * @param cc
	 * @param factory
	 * @throws MCASpaceException
	 */
	private void deployPart(int part, ComputationCase cc,
			DataHandlerFactory factory) throws MCASpaceException {
		int start = partSize * (part -1);
		
		int end = part < getNbParts() ? partSize * part - 1 : size - 1;
		String tmpDir = System.getProperty("mca.home") + "/work/" ;
		File file = new File(tmpDir + "/" + name + "-" + part + ".dat");
		try {
			PrintWriter writer = new PrintWriter(file);
			for(int i = start; i <= end;i++){
				writer.println(values[i]);
			}		
			writer.close();
			DataHandler dh = factory.getDataHandler(file);
			FileInputStream input = new FileInputStream(file);
			dh.upload(input);
			input.close();
			cc.addDataHandler(dh);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
