package org.mca.agent;

import org.mca.javaspace.ComputationCase;
import org.mca.scheduler.Task;

/**
 * 
 * @author Cyril Dumont
 *
 */
public interface ComputeAgent<R> extends MobileAgent {
	
	public R compute(Task<R> task) throws ComputeAgentException;
	
	public void setCase(ComputationCase computationCase);

}
