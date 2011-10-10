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

	private ArrayList<Task<?>> tasks;

	private HashMap<String, Object> results;

	public TaskVerifier(ComputationCase computationCase, ArrayList<Task<?>> tasks) {
		this.computationCase = computationCase;
		this.tasks = tasks;
		results = new HashMap<String, Object>();
	}



}
