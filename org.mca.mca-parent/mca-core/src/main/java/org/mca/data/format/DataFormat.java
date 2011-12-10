package org.mca.data.format;

import java.io.File;
import java.io.Serializable;

import org.mca.data.DataPartLocal;

public abstract class DataFormat<E> implements Serializable{

	private static final long serialVersionUID = 1L;

	public abstract File format(Object data, File out) throws FormatException;
	
	public abstract DataPartLocal parse(File in) throws FormatException;
	
}
