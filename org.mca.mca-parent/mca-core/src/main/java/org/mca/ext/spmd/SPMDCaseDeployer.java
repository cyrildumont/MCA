package org.mca.ext.spmd;

import javax.validation.constraints.NotNull;

import org.mca.agent.AgentDescriptor;
import org.mca.data.DDataStructure;
import org.mca.deployer.ComputationCaseDeployer;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.exceptions.MCASpaceException;

public abstract class SPMDCaseDeployer extends ComputationCaseDeployer {
	
	@NotNull
	private AgentDescriptor program;
	
	@NotNull
	private DataHandlerFactory dataHandlerFactory;
	
	@NotNull
	private DDataStructure<?> input;
	
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
		int size = input.getNbParts();
		for (int rank = 1; rank <= size; rank++) {
			String name =  projectName + "-" + rank;
			SPMDTask t = new SPMDTask(name, computeAgentURL,rank, size);
			addTask(t);
		}
	}

	@Override
	protected void deployData() throws MCASpaceException {
		if (input != null)
			computationCase.addData(input, SPMDConstants.INPUT_NAME, dataHandlerFactory);
	}
	
	public void setInput(DDataStructure<?> input) {
		this.input = input;
	}
}
