package org.mca.skeleton;

import org.mca.agent.AbstractComputeAgent;

public abstract class SkeletonAgent extends AbstractComputeAgent {

	private static final long serialVersionUID = 1L;
	
	protected int rank;
	
	@Override 
	protected final Object execute() throws Exception {
		rank = task.getIntParameter(0);
		return executeSkel();
	}
	
	protected abstract Object executeSkel() throws Exception;
	
	
}
