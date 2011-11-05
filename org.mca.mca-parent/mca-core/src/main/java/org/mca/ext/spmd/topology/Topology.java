package org.mca.ext.spmd.topology;

import java.io.Serializable;

/**
 * 
 * @author Cyril Dumont
 *
 * @param <E>
 */
public abstract class Topology<E> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final int NULL_VALUE = -1;

	protected int size;
	
	protected int rank;
	
	public Topology(int rank, int size) {
		this.size = size;
		this.rank = rank;
	}
	
	public abstract int[] getNeighbors();
	
}
