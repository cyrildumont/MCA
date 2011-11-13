package org.mca.ext.spmd.topology;

import java.util.ArrayList;
import java.util.List;

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

	public Plan(int height, int width) {
		super(width);
		this.height = height;
	}

	public int getHeight() {
		return this.height;
	}

	@Override
	public int getLeft(int rank){
	       if ((width == 1) || ((rank % width) == 1))
	            return NULL_VALUE;
	       else
	            return rank - 1;
	}

	@Override
	public int getRight(int rank) {
		if ((rank % width) == 0) 
			return NULL_VALUE;
		else
			return rank + 1;
	}

	public int getDown(int rank) {
		if (rank > ((height - 1) * width))
			return NULL_VALUE;
		else
			return rank + width;

	}

	public int getUp(int rank) {
		if (rank <= width)
			return NULL_VALUE;
		else
			return rank - width;

	}

	@Override
	public Integer[] getNeighbors(int rank) {
		List<Integer> neighbors = new ArrayList<Integer>();
		if(getRight(rank) != Topology.NULL_VALUE)
			neighbors.add(getRight(rank));
		if(getLeft(rank) != Topology.NULL_VALUE)
			neighbors.add(getLeft(rank));
		if(getUp(rank) != Topology.NULL_VALUE)
			neighbors.add(getUp(rank));
		if(getDown(rank) != Topology.NULL_VALUE)
			neighbors.add(getDown(rank));
		return neighbors.toArray(new Integer[neighbors.size()]);
	}

	@Override
	public String toString() {
		return "Plan [height=" + height + ", width=" + width + ", size=" + size + "]";
	}
	
}
