package org.mca.math;

import java.io.File;


public abstract class DataAbstractFactory<E> {

	public final Data<E> create(File file){
		return createData(file);
	}
	
	protected abstract Data<E> createData(File file);
	
}
