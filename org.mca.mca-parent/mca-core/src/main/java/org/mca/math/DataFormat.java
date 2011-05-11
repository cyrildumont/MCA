package org.mca.math;

import java.io.File;

public abstract class DataFormat<E> {

	public abstract File format(DataPart<E> subVector, File out) throws FormatException;
	
	public abstract DataPart<E> parse(File in) throws FormatException;
	
}
