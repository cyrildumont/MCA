package org.mca.agent;

import java.rmi.RemoteException;

import org.mca.javaspace.ComputationCase;
import org.mca.scheduler.Task;

public interface ComputeAgent extends MobileAgent {
	
	public Object compute(Task task) throws Exception;
	
	public void setCase(ComputationCase computationCase) throws RemoteException;

}
