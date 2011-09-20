package org.mca.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.listener.TaskListener;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;


public abstract class MasterAgent extends AbstractComputeAgent{

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.agent.MasterAgent";

	protected static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	private Collection<Task> pendingResults;
	
	private Collection<Task> retrievedResults;
	
	private boolean allResultsRetrieved;
	
	protected Collection<Task> waitResults() throws MCASpaceException{
		logger.fine("MasterAgent -- [" + computationCase.getName() + "] " + pendingResults.size() + " expected results");
		retrievedResults = new ArrayList<Task>();
		computationCase.registerForTasks(pendingResults, new ResultListener());
		while(!allResultsRetrieved){
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		pendingResults = null;
		return retrievedResults;
	}
	
	protected void addPendingResult(String name){
		if (pendingResults == null) pendingResults = new ArrayList<Task>();
		Task task = new Task(name);
		task.state = TaskState.COMPUTED;
		pendingResults.add(task);
	}
	
	
	class ResultListener extends TaskListener{
		@Override
		protected void taskAdded(Task task) {
			logger.warning("MasterAgent - ResultListener - [" + task + "]");
			retrievedResults.add(task);
			if (retrievedResults.size() == pendingResults.size()) {
				allResultsRetrieved = true;
			}
		}
	}

}
