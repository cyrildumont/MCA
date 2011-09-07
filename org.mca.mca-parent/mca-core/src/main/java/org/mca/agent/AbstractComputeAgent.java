/**
 * 
 */
package org.mca.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.jini.core.lookup.ServiceID;

import org.mca.entry.Property;
import org.mca.javaspace.ComputationCase;
import org.mca.log.LogUtil;
import org.mca.result.TaskVerifier;
import org.mca.scheduler.Task;

/**
 * Classe abstraite définissant un ComputeAgent
 * 
 * @author Cyril
 * 
 *
 */
public abstract class AbstractComputeAgent implements ComputeAgent{

	private static final long serialVersionUID = 1L;

	protected ComputationCase computationCase;

	protected Task task;

	protected Map<String, String> properties = new HashMap<String, String>();

	protected Object[] parameters;

	private ArrayList<Task> tasksToCheck = new ArrayList<Task>();

	protected HashMap<String, Object> results;
	
	public AbstractComputeAgent() {
	}
	
	final public Object compute(Task task) 	throws ComputeAgentException{
		try{
			this.task = task;
			this.parameters = task.parameters;
			Collection<Property> props = computationCase.getProperties(); 
			for (Property mcaProperty : props) {
				properties.put(mcaProperty.name, mcaProperty.value);
			}
			LogUtil.debug("[JacobiAgent][" + computationCase.getName() + "] ", getClass());
			LogUtil.debug("[JacobiAgent]" +
					"[" + computationCase.getName() + "] : " + properties.size() + " properties", getClass());
			for (Map.Entry<String, String> property : properties.entrySet()) {
				LogUtil.debug(" \t " + property.getKey() + " = " + property.getValue(), getClass());
			}
			preCompute();
			if (tasksToCheck.size() != 0) {
				checkTasks();	
			}
			Object result= execute();
			return result;
		}catch(Exception e){
			e.printStackTrace();
			throw new ComputeAgentException();
		}
	}


	final public void serviceIDNotify(ServiceID serviceID) {
		LogUtil.info("Agent [ID=" + serviceID.toString() + "] is deployed", getClass());
		System.exit(1);
	}

	final public void setCase(ComputationCase computationCase){
		this.computationCase = computationCase;
	}

	final protected void addTaskToCheck(Task task){
		tasksToCheck.add(task);
	}

	protected abstract Object execute() throws Exception;

	final private void checkTasks() throws WaitForAnotherTaskException{
		TaskVerifier tv = new TaskVerifier(computationCase, tasksToCheck);
		results = tv.checkResults();
	}

	/**
	 * 
	 * @param taskname
	 * @return
	 */
	final protected Object getResult(String taskname){
		return results.get(taskname);
	}

	/**
	 * 
	 */
	protected void preCompute(){
		LogUtil.info("No precompute", getClass());
	}

	protected File getTempFile(String name){
		File file = new File(System.getProperty("temp.worker.result") + "/" + name);
		return file;
	}

}
