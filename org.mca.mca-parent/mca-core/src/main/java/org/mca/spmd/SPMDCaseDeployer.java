package org.mca.spmd;

import org.mca.agent.AgentDescriptor;
import org.mca.deployer.ComputationCaseDeployer;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.DistributedData;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

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
	protected void init() {
		input = getInput();
	}
	
	@Override
	protected void deployTasks() throws MCASpaceException {
		String computeAgentName = program.getURL();
		for (int i = 1; i <= input.getNbParts(); i++) {
			Task t = new Task();
			t.name =  projectName + "-" + i;
			t.state = TaskState.WAIT_FOR_COMPUTE;
			t.parameters = new Object[]{i};
			t.computing_agent_name = computeAgentName;
			addTask(t);
		}
	}

	@Override
	protected void deployData() throws MCASpaceException {
		if (input != null)
		computationCase.addData(input, SPMD.INPUT_NAME, dataHandlerFactory);
	}
	
	protected abstract DistributedData<?> getInput();
}
