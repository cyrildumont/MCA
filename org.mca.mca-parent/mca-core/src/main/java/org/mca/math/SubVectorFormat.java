package org.mca.math;

import java.io.File;

public abstract class SubVectorFormat<E>{

	public abstract File format(SubVector<E> subVector, File out) throws FormatException;
	
	public abstract SubVector<E> parse(File in) throws FormatException;
	
}
