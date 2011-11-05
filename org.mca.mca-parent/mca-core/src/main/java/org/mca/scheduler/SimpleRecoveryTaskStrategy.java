package org.mca.scheduler;

import java.util.Collection;

import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;

public class SimpleRecoveryTaskStrategy implements RecoveryTaskStrategy{

	private static final long serialVersionUID = 1L;

	private int maxWorkerTasksRecovered;
	
	public SimpleRecoveryTaskStrategy(int maxWorkerTasksRecovered) {
		this.maxWorkerTasksRecovered = maxWorkerTasksRecovered;
	}
	
	@Override
	public Collection<? extends Task<?>> recoverTasksToCompute(
			ComputationCase computationCase) throws MCASpaceException {
		Task template = new Task();
		template.state = TaskState.WAIT_FOR_COMPUTE;
		return computationCase.getTasks(template, maxWorkerTasksRecovered);
	}

}
