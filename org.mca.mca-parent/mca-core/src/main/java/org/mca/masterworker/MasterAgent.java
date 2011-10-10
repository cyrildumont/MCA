package org.mca.masterworker;

import java.util.LinkedList;
import java.util.Queue;

import org.mca.agent.AbstractComputeAgent;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.Task;

/**
 * Abstract class for Master agent to a Master-Worker computation case
 * 
 * @author Cyril Dumont
 *
 */
public abstract class MasterAgent<R> extends AbstractComputeAgent<Object> {

	private static final long serialVersionUID = 1L;

	private static final int ADD_TASK_INTERVAL = 1000;

	/**
	 * 
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
					R result = computationCase.recoverResult();
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


	/**
	 * 
	 * @author Cyril Dumont
	 *
	 */
	private class TaskDeployer extends Thread{

		private boolean interrupt = false;

		public TaskDeployer() {
			super("task deployer thread");
		}

		@Override
		public void run() {
			while(!interrupt){
				try {
					Task<R> task = taskToAddQueue.poll();
					if (task != null ) computationCase.addTask(task);
					Thread.sleep(ADD_TASK_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (MCASpaceException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void interrupt() {
			interrupt = true;
			super.interrupt();
		}
	}

	private ResultCollector resultCollector;
	private TaskDeployer taskDeployer;

	private Queue<Task<R>> taskToAddQueue = new LinkedList<Task<R>>();

	protected int nbAddedTasks;

	protected int nbRecoveredResult;

	private String workerAgentUrl;

	public MasterAgent(String workerAgentUrl) {
		this.workerAgentUrl = workerAgentUrl;
	}

	public void addTask(Task<R> task) throws MCASpaceException{
		task.compute_agent_url = workerAgentUrl;
		computationCase.addTask(task);
		//taskToAddQueue.add(task);
	}

	private boolean caseFinish;

	@Override
	final protected Object execute() throws Exception {
		ThreadGroup tg = new ThreadGroup("master agent thread group");
		
		resultCollector = new ResultCollector();
		resultCollector.start();
//		taskDeployer = new TaskDeployer();
//		taskDeployer.start();
		startMasterProcess();
		while(!caseFinish){
			Thread.sleep(1000);
		}
		return null;
	}


	private void finalizeCase() throws Exception {
		caseFinish = true;
		resultCollector.interrupt();
		taskDeployer.interrupt();
		computationCase.finish();
		performPostTreatment();
	}

	protected abstract boolean performResult(R result);

	protected abstract void startMasterProcess() throws Exception;

	protected abstract void performPostTreatment() throws Exception;
}
