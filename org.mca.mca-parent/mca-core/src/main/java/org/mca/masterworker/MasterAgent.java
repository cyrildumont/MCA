package org.mca.masterworker;

import org.mca.agent.AbstractComputeAgent;
import org.mca.javaspace.exceptions.MCASpaceException;
import static org.mca.masterworker.MWConstants.*;

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
					R result = (R)computationCase.recoverResult();
					nbRecoveredResult++;
					if(performResult(result)) finalizeCase();
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


	private ResultCollector resultCollector;

	protected int nbAddedTasks;

	protected int nbRecoveredResult;

	private String workerAgentUrl;

	public MasterAgent(String workerAgentUrl) {
		this.workerAgentUrl = workerAgentUrl;
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

	protected abstract boolean performResult(R result);

	protected abstract void startMasterProcess() throws Exception;

	protected abstract void performPostTreatment() throws Exception;
}
