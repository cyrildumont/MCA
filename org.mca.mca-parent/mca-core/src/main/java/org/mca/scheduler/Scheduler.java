package org.mca.scheduler;

import java.util.Observable;
import java.util.Observer;

import org.mca.javaspace.ComputationCase;

public abstract class Scheduler extends Observable{
	
	public String projectName;

	protected ComputationCase computationCase;
	
	
	public void setComputationCase(ComputationCase computationCase) {
		this.computationCase = computationCase;
	}

	public Scheduler(){}
	
	public Scheduler(Observer o){
		addObserver(o);
	}
	
	public void notifyforNewTaks(Task[] tasks){
		setChanged();
		notifyObservers(tasks);
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public abstract void start();
}
