package org.mca.ext.masterworker;

import javax.validation.constraints.NotNull;

import org.mca.agent.AgentDescriptor;
import org.mca.agent.ComputeAgent;
import org.mca.deployer.ComputationCaseDeployer;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.RecoveryTaskStrategy;
import org.mca.scheduler.Task;

import static org.mca.ext.masterworker.MWConstants.*;

public class MWCaseDeployer extends ComputationCaseDeployer {

	@NotNull
	private AgentDescriptor masterAgent;
	
	@NotNull
	private AgentDescriptor workerAgent;
	
	@NotNull
	private int maxWorkerTasksRecovered;
	
	@NotNull
	private int maxMasterResultsRecovered; 
	
	@Override
	protected void deployAgents() throws MCASpaceException {
		Object[] params = new Object[]{workerAgent.getURL(), maxMasterResultsRecovered};
		deployAgent(masterAgent, params);
		deployAgent(workerAgent);
	}

	@Override
	protected void deployTasks() throws MCASpaceException {
		String masterAgentURL = masterAgent.getURL();
		Task<?> masterTask = new MasterTask(MASTER_TASK_NAME, masterAgentURL);
		addTask(masterTask);
	}

	@Override
	protected void deployData() throws MCASpaceException {
	}
	
	@Override
	protected RecoveryTaskStrategy getStrategy() {
		return new MWRecoveryTaskStrategy(maxWorkerTasksRecovered);
	}

	public void setMasterAgent(AgentDescriptor masterAgent) {
		this.masterAgent = masterAgent;
	}
	
	public void setWorkerAgent(AgentDescriptor workerAgent) {
		this.workerAgent = workerAgent;
	}
	
	public void setMaxWorkerTasksRecovered(int maxWorkerTasksRecovered) {
		this.maxWorkerTasksRecovered = maxWorkerTasksRecovered;
	}
	
	public void setMaxMasterResultsRecovered(int maxMasterResultsRecovered) {
		this.maxMasterResultsRecovered = maxMasterResultsRecovered;
	}
}
