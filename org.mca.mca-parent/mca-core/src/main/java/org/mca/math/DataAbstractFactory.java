package org.mca.math;

import java.io.File;


public abstract class DataAbstractFactory<E> {

	public final DData<E> create(File file){
		return createData(file);
	}
	
	protected abstract DData<E> createData(File file);
	
}
