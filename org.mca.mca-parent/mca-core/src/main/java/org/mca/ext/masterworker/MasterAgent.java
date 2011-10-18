package org.mca.ext.masterworker;

import static org.mca.ext.masterworker.MWConstants.WORKER_TASK_NAME;

import java.util.Collection;

import org.mca.agent.AbstractComputeAgent;
import org.mca.javaspace.exceptions.MCASpaceException;

/**
 * Abstract class for Master agent to a Master-Worker computation case
 * 
 * @author Cyril Dumont
 *
 */
public abstract class MasterAgent<R> extends AbstractComputeAgent<Object> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * abstract class represents a thread that collects worker tasks results 
	 * 
	 * @author Cyril
	 *
	 */
	private class ResultCollector extends Thread{

		public ResultCollector() {
			super("result collector thread");
		}

		@Override
		public void run() {
			while(!caseFinish){
				try {
					Collection<R> results = computationCase.recoverResults(maxMasterResultsRecovered);
					nbRecoveredResult = nbRecoveredResult + results.size();
					if(performResults(results)) finalizeCase();
				} catch (Exception e) {
					logger.warning("MasterAgent - ResultCollector on error");
					logger.throwing("ResultCollector", "run", e);
				}
			}
		}

		@Override
		public void interrupt() {
			logger.info("MasterAgent - ResultCollector interrupt");
			super.interrupt();
		}
	}

	private int maxMasterResultsRecovered;

	private ResultCollector resultCollector;

	protected int nbAddedTasks;

	protected int nbRecoveredResult;

	private String workerAgentUrl;

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
		WorkerTask<Boolean> task = new WorkerTask<Boolean>(WORKER_TASK_NAME + ++nbAddedTasks);
		task.parameters = parameters;
		task.compute_agent_url = workerAgentUrl;
		computationCase.addTask(task);
	}

	private boolean caseFinish;

	@Override
	final protected Object execute() throws Exception {
		resultCollector = new ResultCollector();
		resultCollector.start();
		startMasterProcess();
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
