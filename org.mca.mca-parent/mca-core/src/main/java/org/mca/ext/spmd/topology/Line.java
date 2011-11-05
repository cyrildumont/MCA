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
	
	public Line(int rank, int width) {
		super(rank, width);
		this.width = width;
	}

	public int getWidth(){
		return width;
	}
	
	public int getRight(){
		if(rank != width)
			return rank + 1;
		else 
			return NULL_VALUE;
	}
	
	public int getLeft(){
		if(rank != 1)
			return rank - 1;
		else 
			return NULL_VALUE;
	}
	
	@Override
	public int[] getNeighbors() {
		int right = getRight();
		int left = getLeft();
		return new int[]{right, left};
	}
}
