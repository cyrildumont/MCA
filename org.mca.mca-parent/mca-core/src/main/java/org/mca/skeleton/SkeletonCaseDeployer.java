package org.mca.skeleton;

import java.util.HashMap;
import java.util.Map;

import org.mca.agent.AgentDescriptor;
import org.mca.agent.exception.DeployException;
import org.mca.deployer.ComputationCaseDeployer;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.Task;

public abstract class SkeletonCaseDeployer extends ComputationCaseDeployer{

	
	public final static String MASTER_TASK_NAME = "Master";
	public static final String MASTER_AGENT_NAME = "MasterAgent";
	
	private Map<String, String> agents = new HashMap<String, String>();

	private AgentDescriptor agentDescriptor;
	
	protected DataHandlerFactory dataHandlerFactory;
	
	private String masterAgentURL;
	
	public void setDataHandlerFactory(DataHandlerFactory dataHandlerFactory) {
		this.dataHandlerFactory = dataHandlerFactory;
	}
	
	public void setAgentDescriptor(AgentDescriptor agentDescriptor) {
		this.agentDescriptor = agentDescriptor;
	}
	
	@Override
	final protected void deployAgents() throws MCASpaceException {
		deployMasterAgent();
	}
	
	final protected void deployAgent(String name, SkeletonAgent agent) throws DeployException{
		agentDescriptor.setName(name);
		agentDeployer.deploy(agentDescriptor, agent);
		agents.put(name, agentDescriptor.getURL());
	}

	private void deployMasterAgent() throws DeployException {
		SkeletonMasterAgent masterAgent = getMasterAgent();
		masterAgent.setAgentDescriptor(agentDescriptor);
		masterAgent.setDataHandlerFactory(dataHandlerFactory);
		agentDescriptor.setName(MASTER_AGENT_NAME);
		agentDeployer.deploy(agentDescriptor, masterAgent);
		masterAgentURL = agentDescriptor.getURL();
	}

	protected void addAgent(String name, String url){
		agents.put(name, url);

	}
	
	@Override
	protected void deployTasks() throws MCASpaceException {
		Task task = new Task(MASTER_TASK_NAME, masterAgentURL);
		addTask(task);
	}
	
	
	protected abstract SkeletonMasterAgent getMasterAgent();

}
