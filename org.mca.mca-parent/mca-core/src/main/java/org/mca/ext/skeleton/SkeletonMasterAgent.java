package org.mca.ext.skeleton;

import org.mca.agent.AgentDescriptor;
import org.mca.agent.ComputeAgentDeployer;
import org.mca.agent.MasterAgent;
import org.mca.agent.exception.DeployException;
import org.mca.entry.DataHandlerFactory;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.DistributedVector;
import org.mca.math.format.DataFormat;
import org.mca.scheduler.Task;

public abstract class SkeletonMasterAgent extends MasterAgent{

	private static final long serialVersionUID = 1L;

	private AgentDescriptor agentDescriptor;
	
	protected DataHandlerFactory dataHandlerFactory;
	
	protected ComputeAgentDeployer agentDeployer = new ComputeAgentDeployer();
	
	public void setDataHandlerFactory(DataHandlerFactory dataHandlerFactory) {
		this.dataHandlerFactory = dataHandlerFactory;
	}

	public void setAgentDescriptor(AgentDescriptor agentDescriptor) {
		this.agentDescriptor = agentDescriptor;
	}
	
	protected <I,O> DistributedVector<O> map(String name,
			DistributedVector<I> input, Function<I, O> function, DataFormat<O> outputFormat) throws MCASpaceException {
		int size = input.getSize();
		int partSize = input.getPartSize();
		DistributedVector<O> output =
			new DistributedVector<O>(outputFormat, size, partSize);
		computationCase.addData(output, name + "-result", dataHandlerFactory);
		MapAgent<I,O> mapAgent = 
			new MapAgent<I,O>(function, input.getName(), output.getName());
		String url = deployAgent(name, mapAgent);
		int nbPart = input.getNbParts();
		for(int i=1;i <= nbPart;i++){
			Task t = new Task(name + "-" + i);
			t.compute_agent_url = url;
			t.parameters=new Object[]{i} ;
			computationCase.addTask(t);
			addPendingResult(t.name);
		}
		waitResults();
		return output;
		
	}
	
	protected <I,O> DistributedVector<O> mapidx(String name,
			DistributedVector<I> input, BinFunction<Integer, I ,O> function, 
				DataFormat<O> outputFormat) throws MCASpaceException {
		int size = input.getSize();
		int partSize = input.getPartSize();
		DistributedVector<O> output =
			new DistributedVector<O>(outputFormat, size, partSize);
		computationCase.addData(output, name + "-result", dataHandlerFactory);
		MapIdxAgent<I,O> mapAgent = 
			new MapIdxAgent<I,O>(function, input.getName(), output.getName());
		String url = deployAgent(name, mapAgent);
		
		int nbPart = input.getNbParts();
		for(int i=1;i <= nbPart;i++){
			Task t = new Task(name + "-" + i);
			t.compute_agent_url = url;
			t.parameters=new Object[]{i} ;
			computationCase.addTask(t);
			addPendingResult(t.name);
		}
		waitResults();
		return output;
	}
	
	
	/**
	 * 
	 * @param <O>
	 * @param value
	 * @param n
	 * @param ouput
	 * @return
	 * @throws MCASpaceException 
	 */
	protected <O> DistributedVector<O> repl(String name, O value, 
			int n, DataFormat<O> outputFormat, int partSize) throws MCASpaceException{
		DistributedVector<O> output =
			new DistributedVector<O>(outputFormat, n, partSize);
		computationCase.addData(output, name + "-result", dataHandlerFactory);
		ReplAgent<O> agent = new ReplAgent<O>(value, output.getName());
		String url = deployAgent(name, agent);
		int nbPart = output.getNbParts();
		for(int i=1;i <= nbPart;i++){
			Task t = new Task(name + "-" + i);
			t.compute_agent_url = url;
			t.parameters=new Object[]{i} ;
			computationCase.addTask(t);
			addPendingResult(t.name);
		}	
		waitResults();
		return output;
	}

	/**
	 * 
	 * @param <T>
	 * @param input
	 * @param output
	 * @param oplus
	 * @return
	 */
	protected <T> T reduce(String input, String output, BinOperator<T> oplus){
		
		return null;
	}

	/**
	 * 
	 * @param <T>
	 * @param input
	 * @param output
	 * @param oplus
	 * @return
	 */
	protected <T> T scan(String input, String output, BinOperator<T> oplus){
		return null;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param input
	 * @param output
	 * @param oplus
	 * @param otimes
	 * @return
	 */
	protected <T> T dh(String input, String output, BinOperator<T> oplus, BinOperator<T> otimes){
		
		return null;
	}
	
	private String deployAgent(String name, SkeletonAgent agent) throws DeployException{
		agentDescriptor.setName(name);
		agentDeployer.deploy(agentDescriptor, agent);
		return agentDescriptor.getURL();
	}
}
