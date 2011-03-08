package org.mca.listener;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.management.remote.JMXConnector;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.space.AvailabilityEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.TaskNotifierAgentImpl;
import org.mca.core.ComponentInfo;
import org.mca.core.MCAComponent;
import org.mca.log.LogUtil;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

public class TaskListener  implements RemoteEventListener{

	/** Log */
	private final static Log LOG = LogFactory.getLog(TaskListener.class);

	private TaskState state;

	private ArrayList<String> roadmap;
	private ArrayList<JMXConnector> listeners;


	/**
	 * 
	 * @param state
	 */
	public TaskListener() {
		listeners = new ArrayList<JMXConnector>();
		roadmap = new ArrayList<String>();
	}

	/**
	 * 
	 */
	public void notify(RemoteEvent event) throws UnknownEventException,
	RemoteException {
		AvailabilityEvent availabilityEvent = (AvailabilityEvent)event;
		Task task;
		try {
			task = (Task)availabilityEvent.getEntry();
			TaskNotifierAgentImpl agent;

			agent = new TaskNotifierAgentImpl((ArrayList<String>)roadmap.clone());
			agent.setTask(task);
			agent.start();
		} catch (UnusableEntryException e) {
			LOG.error(e.getMessage());
		} catch (RemoteException e) {
			LOG.error(e.getMessage());
		}
	}

	/**
	 * 
	 * @param address
	 */
	public void addListener(ComponentInfo component){
		String address = component.getHostname();
		LogUtil.debug("add new worker : " + address, getClass());	
		roadmap.add(address);
		listeners.add(component.getConnector());
	}
	
	

}
