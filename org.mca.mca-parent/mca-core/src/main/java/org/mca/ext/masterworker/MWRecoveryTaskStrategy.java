package org.mca.ext.masterworker;

import java.util.Collection;
import java.util.Collections;

import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.RecoveryTaskStrategy;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

public class MWRecoveryTaskStrategy extends RecoveryTaskStrategy{

	private static final long serialVersionUID = 1L;
	
	private int maxWorkerTasksRecovered;
	
	public MWRecoveryTaskStrategy(int maxWorkerTasksRecovered) {
		this.maxWorkerTasksRecovered = maxWorkerTasksRecovered;
	}
	
	@Override
	public Collection<? extends Task<?>> recoverTasksToCompute(
			ComputationCase computationCase)
			throws MCASpaceException {
		Collection<? extends Task<?>> tasksToCompute = null;
		Task template = new MasterTask();
		template.state = TaskState.WAIT_FOR_COMPUTE;
		Task<?> masterTask = computationCase.getTask(template);
		if (masterTask != null) {
			tasksToCompute = Collections.singleton(masterTask);
		}else{
			template = new WorkerTask();
			template.state = TaskState.WAIT_FOR_COMPUTE;
			tasksToCompute = 
					computationCase.getTasks(template, maxWorkerTasksRecovered);
		}
		
		return tasksToCompute;
	}

}
