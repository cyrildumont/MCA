package org.mca.javaspace;

import com.sun.jini.thread.TaskManager;

public class MCATaskManager extends TaskManager {

	@Override
	public synchronized void add(Task task) {
		System.out.println(task);
		super.add(task);
	}
}
