package org.mca.math;

import java.io.File;

import org.mca.data.DDataStructure;


public abstract class DataAbstractFactory<E> {

	public final DDataStructure<E> create(File file){
		return createData(file);
	}
	
	protected abstract DDataStructure<E> createData(File file);
	
}
