package org.mca.agent;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import org.mca.entry.ComputationCase;
import org.mca.scheduler.Task;

public interface ComputeAgentInterface extends Remote {
	
	public Object compute(Task task) throws Exception;
	
	public void setCase(ComputationCase computationCase) throws RemoteException;

}
