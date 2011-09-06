package org.mca.spmd;

import org.mca.agent.AbstractComputeAgent;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.DataPart;
import org.mca.math.DistributedData;

public abstract class SPMDAgent<T extends DistributedData<?>, P extends DataPart<?>> extends AbstractComputeAgent {

	private static final long serialVersionUID = 1L;
	
	protected int rank;
	protected int size;
	
	protected T input;
	protected P part;
	
	@Override
	final protected Object execute() throws Exception {
		rank = (Integer)parameters[0];
		input = computationCase.getData(SPMD.INPUT_NAME);
		size = input.getNbParts();
		part = (P)input.load(rank);
		Object result = program();
		input.unload();
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
	protected void barrier(String name) throws MCASpaceException{
		System.out.println("barrier [" + name + "]");
		if (rank == 1)
			computationCase.createBarrier(name);
		computationCase.barrier(name, size);
		if (rank == 1)
			computationCase.removeBarrier(name);
	}
	
}
