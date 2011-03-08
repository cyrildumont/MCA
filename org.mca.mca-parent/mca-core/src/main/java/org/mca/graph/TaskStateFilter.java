package org.mca.graph;

import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

import edu.uci.ics.jung.graph.filters.GeneralVertexAcceptFilter;
import edu.uci.ics.jung.graph.Vertex;

public class TaskStateFilter extends GeneralVertexAcceptFilter{

	private final TaskState state;
	
	public TaskStateFilter(TaskState state) {
		this.state = state;
	}
	
	@Override
	public boolean acceptVertex(Vertex vertex) {
		if (vertex instanceof MCAVertex) {
			MCAVertex MCAVertex = (MCAVertex) vertex;
			Task task = MCAVertex.getTask();
			return task.getState().equals(this.state);
		}else{
			return false;	
		}
		
	}

	public String getName() {
		return null;
	}



}
