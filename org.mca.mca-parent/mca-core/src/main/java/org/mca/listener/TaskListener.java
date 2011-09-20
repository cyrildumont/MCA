package org.mca.listener;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.space.AvailabilityEvent;

import org.mca.scheduler.Task;

public abstract class TaskListener implements RemoteEventListener, Remote{

	private static final String COMPONENT_NAME = "org.mca.agent.ComputeAgent";

	protected static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	/**
	 * 
	 */
	public void notify(RemoteEvent event) throws UnknownEventException,
	RemoteException {
		AvailabilityEvent availabilityEvent = (AvailabilityEvent)event;
		try {
			Task task = (Task)availabilityEvent.getEntry();
			taskAdded(task);
		} catch (UnusableEntryException e) {
			logger.warning(e.getMessage());
		}
	}

	protected abstract void taskAdded(Task task);

}
