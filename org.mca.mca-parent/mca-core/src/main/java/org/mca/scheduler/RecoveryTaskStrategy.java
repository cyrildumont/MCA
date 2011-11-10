package org.mca.scheduler;

import java.util.Collection;

import org.mca.entry.Storable;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.w3c.dom.Node;

/**
 * 
 * @author Cyril Dumont
 *
 */
public class RecoveryTaskStrategy extends Storable{

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_MAX_WORKER_TASKS_RECOVERED = 1;

	public Integer maxWorkerTasksRecovered;

	public RecoveryTaskStrategy() {}

	public RecoveryTaskStrategy(int maxWorkerTasksRecovered) {
		this.maxWorkerTasksRecovered = maxWorkerTasksRecovered;
	}
	
	public Collection<? extends Task<?>> recoverTasksToCompute(
			ComputationCase computationCase) throws MCASpaceException {
		Task template = new Task();
		template.state = TaskState.WAIT_FOR_COMPUTE;
		return computationCase.getTasks(template, maxWorkerTasksRecovered);
	}

	@Override
	public void parse(Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void store(Node parent) {
		// TODO Auto-generated method stub

	}

}
