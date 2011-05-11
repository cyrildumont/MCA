package org.mca.result;

import java.util.ArrayList;
import java.util.HashMap;

import org.mca.agent.WaitForAnotherTaskException;
import org.mca.javaspace.ComputationCase;
import org.mca.log.LogUtil;
import org.mca.scheduler.Task;

/**
 * 
 * @author cyril
 *
 */
public class TaskVerifier {

	private ComputationCase computationCase;

	private ArrayList<Task> tasks;

	private HashMap<String, Object> results;

	public TaskVerifier(ComputationCase computationCase, ArrayList<Task> tasks) {
		this.computationCase = computationCase;
		this.tasks = tasks;
		results = new HashMap<String, Object>();
	}


	/**
	 * 
	 */
	public HashMap<String, Object> checkResults() throws WaitForAnotherTaskException{
		LogUtil.debug("Checking space for results ...", getClass());
		Task[] iterator = tasks.toArray(new Task[tasks.size()]); 
		for (Task task: iterator) {
			checkResultIntoSpace(task);
		}
		if (tasks.size() != 0) {
			throw new WaitForAnotherTaskException(tasks);
		}
		return results;
	}

	/**
	 * 
	 * @param task
	 */

	private void checkResultIntoSpace(Task task) {
		//		try {
		//			Task t = readTask(task.name, null);
		//			if (t != null) {
		//				LogUtil.debug("Checking " + task.name + " result ...", getClass());
		//				if ((TaskState.COMPUTED).equals(t.state)){
		//					LogUtil.debug("Result " + task.name + " found.", getClass());
		//					tasks.remove(task);
		//					results.put(t.name,t.result);
		//				}
		//			}else{
		//				task.state = TaskState.WAIT_FOR_COMPUTE;
		//				addTask(task, null);
		//			}
		//			} catch (MCASpaceException e) {
		//				e.printStackTrace();
		//			}
	}


}
