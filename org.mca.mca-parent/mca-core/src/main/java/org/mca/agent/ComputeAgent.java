package org.mca.agent;

import org.mca.javaspace.ComputationCase;
import org.mca.scheduler.Task;

/**
 * 
 * @author Cyril Dumont
 *
 */
public interface ComputeAgent extends MobileAgent {
	
	public Object compute(Task task) throws ComputeAgentException;
	
	public void setCase(ComputationCase computationCase);

}
