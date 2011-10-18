package org.mca.masterworker;

import org.mca.scheduler.Task;

public class MasterTask extends Task<Object> {

	private static final long serialVersionUID = 1L;
	
	public MasterTask() {}
	
	public MasterTask(String name, String computeAgentURL){
		super(name, computeAgentURL);
	}

}
