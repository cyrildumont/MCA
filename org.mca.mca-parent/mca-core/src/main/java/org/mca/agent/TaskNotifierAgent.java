package org.mca.agent;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.mca.scheduler.Task;

public interface TaskNotifierAgent extends Remote{
	
	public void next() throws RemoteException;
	public Task getTask() throws RemoteException;
}


