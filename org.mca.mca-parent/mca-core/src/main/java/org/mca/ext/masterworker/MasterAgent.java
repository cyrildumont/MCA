package org.mca.ext.masterworker;

import static org.mca.ext.masterworker.MWConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.mca.agent.AbstractComputeAgent;
import org.mca.javaspace.exceptions.MCASpaceException;

/**
 * Abstract class for Master agent to a Master-Worker computation case
 * 
 * @author Cyril Dumont
 *
 */
public abstract class MasterAgent<R> extends AbstractComputeAgent<Object> {

	private static final String COMPONENT_NAME = "org.mca.agent.MasterAgent";

	protected static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * abstract class represents a thread that collects worker tasks results 
	 * 
	 * @author Cyril
	 *
	 */
	private class ResultCollector implements Callable<Void>{

		public ResultCollector() {
			//super("result collector thread");
		}

		@Override
		public Void call() {
			String computationCaseName = computationCase.getName();
			while(!caseFinish){
				try {
					Collection<R> results = computationCase.recoverResults(maxMasterResultsRecovered);
					logger.fine("[" + computationCaseName + "] " + results.size() +  " results recovered.");
					nbRecoveredResult = nbRecoveredResult + results.size();
					if(performResults(results)){
						finalizeCase();
					}
				} catch (Exception e) {
					logger.warning("MasterAgent - ResultCollector on error");
					logger.throwing("ResultCollector", "run", e);
					return null;
				}
			}
			return null;
		}

//		@Override
//		public void interrupt() {
//			logger.info("MasterAgent - ResultCollector interrupt");
//			super.interrupt();
//		}
	}
	
	/**
	 * 
	 * @author cyril
	 *
	 */
	private class TaskDeployer implements Callable<Void>{
		
		public TaskDeployer() {
			//super("task deployer thread");
		}
		
		
		
		@Override
		public Void call() {
			try {
				startMasterProcess();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private int maxMasterResultsRecovered;

	private ResultCollector resultCollector;

	protected int nbAddedTasks;

	protected int nbRecoveredResult;

	private String workerAgentUrl;

	private boolean caseFinish;
	
	private List<WorkerTask<?>> tasksToAdd = new ArrayList<WorkerTask<?>>();
	
	public MasterAgent(String workerAgentUrl, int maxMasterResultsRecovered) {
		this.workerAgentUrl = workerAgentUrl;
		this.maxMasterResultsRecovered = maxMasterResultsRecovered;
	}

	/**
	 * 
	 * @param parameters
	 * @throws MCASpaceException
	 */
	public void addWorkerTask(Object[] parameters) throws MCASpaceException{
		WorkerTask<Boolean> task = new WorkerTask<Boolean>(WORKER_TASK_NAME + "-" +  ++nbAddedTasks);
		task.parameters = parameters;
		task.compute_agent_url = workerAgentUrl;
		tasksToAdd.add(task);
		if(tasksToAdd.size() == maxMasterResultsRecovered){
			computationCase.addTasks(tasksToAdd);
			logger.fine("MasterAgent -- [" + computationCase.getName() + "] " +
					"" + tasksToAdd.size() + " tasks added.");
			tasksToAdd.clear();
		}
	}

	@Override
	final protected Object execute() throws Exception {
		resultCollector = new ResultCollector();
//		resultCollector.start();
		TaskDeployer taskDeployer = new TaskDeployer();
		
		ExecutorService es = Executors.newFixedThreadPool(2);
		es.submit(resultCollector);
		es.submit(taskDeployer);
		
		while(!caseFinish){
			Thread.sleep(1000);
		}
		return null;
	}

	private void finalizeCase() throws Exception {
		computationCase.finish();
		caseFinish = true;
		performPostTreatment();
	}

	public void setMaxMasterResultsRecovered(int maxMasterResultsRecovered) {
		this.maxMasterResultsRecovered = maxMasterResultsRecovered;
	}
	
	protected abstract boolean performResults(Collection<R> results);

	protected abstract void startMasterProcess() throws Exception;

	protected abstract void performPostTreatment() throws Exception;
}
