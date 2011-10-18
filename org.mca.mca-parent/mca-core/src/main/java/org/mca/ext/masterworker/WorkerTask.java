package org.mca.ext.masterworker;

import org.mca.scheduler.Task;

public class WorkerTask<R> extends Task<R> {

	private static final long serialVersionUID = 1L;
	
	public WorkerTask() {}

	public WorkerTask(String name){
		super(name);
	}
	
	public WorkerTask(String name, String computeAgentURL){
		super(name, computeAgentURL);
	}

}
