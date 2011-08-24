package org.mca.math.format;

import java.io.File;
import java.io.Serializable;

import org.mca.math.DataPart;

public abstract class DataFormat<E> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4251472512179730075L;

	public abstract File format(DataPart<E> subVector, File out) throws FormatException;
	
	public abstract DataPart<E> parse(File in) throws FormatException;
	
}
