package org.mca.ext.spmd;

import org.mca.scheduler.Task;

public class SPMDTask extends Task<Object> {

	private static final long serialVersionUID = 1L;

	public Integer rank;
	
	public Integer size;
	
	public SPMDTask(){}
	
	public SPMDTask(String name, int rank, int size){
		super(name);
		this.rank = rank;
		this.size = size;
	}
	
	public SPMDTask(String name, String computeAgentURL, int rank, int size){
		super(name, computeAgentURL);
		this.rank = rank;
		this.size = size;
	}

}
