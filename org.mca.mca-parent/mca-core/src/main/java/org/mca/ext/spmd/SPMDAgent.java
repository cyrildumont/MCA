package org.mca.ext.spmd;

import org.mca.agent.AbstractComputeAgent;
import org.mca.ext.spmd.topology.Topology;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.Neighborhood;

/**
 * This class represents a SPMD ComputeAgent. 
 * This agent needs a rank and a size.
 * 
 * @author Cyril Dumont
 * 
 *
 */
public abstract class SPMDAgent extends AbstractComputeAgent<Object> {

	private static final long serialVersionUID = 1L;

	protected int rank;
	protected int size;

	protected Topology<?> topology;

	protected Integer[] neighbors;

	@Override
	final protected Object execute() throws Exception {
		if(!(task instanceof SPMDTask))
			throw new MCASpaceException("the task is not a SPMDTask");
		rank = ((SPMDTask)task).rank;
		size = ((SPMDTask)task).size;
		loadData();
		topology = getTopology();
		neighbors = topology.getNeighbors(rank);
		logger.fine("SPMDAgent -- [rank=" + rank + "]");
		Object result = program();
		unloadData();
		return result;
	}

	protected abstract void unloadData() throws Exception;

	protected abstract void loadData() throws Exception;

	protected abstract Topology<?> getTopology();

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract Object program() throws Exception;

	/**
	 * 
	 * @param name
	 * @throws MCASpaceException
	 */
	protected void barrierNeighbor(String name) throws MCASpaceException{
		computationCase.barrier(name, rank, neighbors);
	}

	/**
	 * 
	 * @param name
	 * @throws MCASpaceException
	 */
	protected void barrier(String name) throws MCASpaceException{
		if (rank == 1)
			computationCase.createBarrier(name, size);
		computationCase.barrier(name);
	}

}
