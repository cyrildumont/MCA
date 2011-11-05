package org.mca.ext.spmd.topology;

/**
 * This class represents a two-dimensional topology.
 * 
 * @author Cyril Dumont
 *
 * @param <E>
 */
public class Plan<E> extends Line<E> {


	private static final long serialVersionUID = 1L;

	protected int height;

	public Plan(int rank,  int height, int width) {
		super(rank, width);
		this.height = height;
	}

	public int getHeight() {
		return this.height;
	}

	@Override
	public int getLeft(){
	       if ((rank % width) == 1)
	            return NULL_VALUE;
	       else
	            return rank - 1;
	}

	@Override
	public int getRight() {
		if ((rank % width) == 0) 
			return NULL_VALUE;
		else
			return rank + 1;
	}

	public int getDown() {
		if (rank > ((height - 1) * width))
			return NULL_VALUE;
		else
			return rank + width;

	}

	public int getUp() {
		if (rank <= width)
			return NULL_VALUE;
		else
			return rank - width;

	}

	@Override
	public int[] getNeighbors() {
		int right = getRight();
		int left = getLeft();
		int down = getDown();
		int up = getUp();
		return new int[]{up, down, right, left};
	}

	@Override
	public String toString() {
		return "Plan [height=" + height + ", width=" + width + ", size=" + size
				+ ", rank=" + rank + "]";
	}
	
}
