package org.mca.graph;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

import edu.uci.ics.jung.graph.impl.SparseVertex;

public class MCAVertex extends SparseVertex{

	/** Log */
	private final static Log LOG = LogFactory.getLog(MCAVertex.class);


	private Task task;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public boolean isReadyToCompute(){
		if (LOG.isDebugEnabled()) {
			LOG.debug("Checking of the Task (" + this.task + ") is ready to compute.");
		}
		TaskState state = task.getState();
		boolean ready= false;
		if (state.equals(TaskState.READY_TO_COMPUTE)) {
			ready = true;
		}
		else if(state.equals(TaskState.WAIT_FOR_ANOTHER_TASK)){
			ready = arePredecessorsComputed();
		}
		if (LOG.isDebugEnabled()) {
			if (ready) {
				LOG.debug("The task " + this.task.name + " is ready to be computed");
			}
		}
		return ready;
	}

	/**
	 * @param ready
	 * @return
	 */
	private boolean arePredecessorsComputed() {

		Set<MCAVertex> predecessors = getPredecessors();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Depends of " + predecessors.size() + " task(s)");
		}
		for (MCAVertex vertex : predecessors) {
			Task task = vertex.getTask();
			if (LOG.isDebugEnabled()) {
				LOG.debug("--> " + task);
			}
			if (!task.state.equals(TaskState.COMPUTED)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("The task (" + task + ") is not COMPUTED");
				}
				return false;
			}
		}
		return true;
	}
}
