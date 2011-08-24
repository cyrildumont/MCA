package org.mca.deployer;

import java.util.Map;

import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.Task;

public abstract class TaskDeployer {

	protected ComputationCase computationCase;
	
	protected Map<String, String> properties;
	
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public void setComputationCase(ComputationCase computationCase){
		this.computationCase = computationCase;
	}
	
	protected void addTask(Task task) throws MCASpaceException{
		computationCase.addTask(task);
	}
	
	public abstract void deploy() throws MCASpaceException;
}
