package org.mca.math;

import java.io.File;


public abstract class DataAbstractFactory<E> {

	public final DistributedData<E> create(File file){
		return createData(file);
	}
	
	protected abstract DistributedData<E> createData(File file);
	
}
