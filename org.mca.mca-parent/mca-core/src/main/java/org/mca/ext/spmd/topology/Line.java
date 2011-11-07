package org.mca.ext.spmd.topology;

/**
 * 
 * This class represents a one-dimensional topology.
 * 
 * @author Cyril Dumont
 *
 * @param <E>
 */
public class Line<E> extends Topology<E> {

	private static final long serialVersionUID = 1L;

	protected int width;
	
	public Line(int width) {
		super(width);
		this.width = width;
	}

	public int getWidth(int rank){
		return width;
	}
	
	public int getRight(int rank){
		if(rank != width)
			return rank + 1;
		else 
			return NULL_VALUE;
	}
	
	public int getLeft(int rank){
		if(rank != 1)
			return rank - 1;
		else 
			return NULL_VALUE;
	}
	
	@Override
	public Integer[] getNeighbors(int rank) {
		int right = getRight(rank);
		int left = getLeft(rank);
		return new Integer[]{right, left};
	}
}
