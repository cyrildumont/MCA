package org.mca.spmd;

import org.mca.agent.AgentDescriptor;
import org.mca.deployer.ComputationCaseDeployer;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.DistributedData;
import org.mca.scheduler.Task;

public abstract class SPMDCaseDeployer extends ComputationCaseDeployer {
	
	private AgentDescriptor program;
	
	private DataHandlerFactory dataHandlerFactory;
	
	private DistributedData<?> input;
	
	public void setProgram(AgentDescriptor program) {
		this.program = program;
	}
	
	public void setDataHandlerFactory(DataHandlerFactory dataHandlerFactory) {
		this.dataHandlerFactory = dataHandlerFactory;
	}
	
	@Override
	protected void deployAgents() throws MCASpaceException{
		deployAgent(program);
	}
	
	@Override
	protected void deployTasks() throws MCASpaceException {
		String computeAgentURL = program.getURL();
		for (int i = 1; i <= input.getNbParts(); i++) {
			String name =  projectName + "-" + i;
			Task t = new Task(name, computeAgentURL);
			t.parameters = new Object[]{i};
			addTask(t);
		}
	}

	@Override
	protected void deployData() throws MCASpaceException {
		input = getInput();
		if (input != null)
		computationCase.addData(input, SPMD.INPUT_NAME, dataHandlerFactory);
	}
	
	protected abstract DistributedData<?> getInput();
}
