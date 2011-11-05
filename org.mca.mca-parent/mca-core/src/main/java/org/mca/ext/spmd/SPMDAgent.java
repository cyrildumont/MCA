package org.mca.ext.spmd;

import org.mca.agent.AbstractComputeAgent;
import org.mca.ext.spmd.topology.Topology;
import org.mca.javaspace.exceptions.MCASpaceException;

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
	
	@Override
	final protected Object execute() throws Exception {
		if(task instanceof SPMDTask){
			rank = ((SPMDTask)task).rank;
			size = ((SPMDTask)task).size;
		}else
			throw new MCASpaceException("the task is not a SPMDTask");
		Object result = program();
		return result;
	}
	
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
		computationCase.barrier(name, rank, topology.getNeighbors());
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
