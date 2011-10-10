package org.mca.agent;

import java.util.ArrayList;

import org.mca.scheduler.Task;

public class WaitForAnotherTaskException extends Exception {

	private ArrayList<Task<?>> tasks;
	
	public WaitForAnotherTaskException(ArrayList<Task<?>> tasks) {
		this.tasks = tasks;
	}

	public ArrayList<Task<?>> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<Task<?>> tasks) {
		this.tasks = tasks;
	}

	
}
