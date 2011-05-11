package org.mca.listener;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.space.AvailabilityEvent;

import org.mca.entry.DataHandler;
import org.mca.entry.MCAProperty;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.javaspace.exceptions.NoJavaSpaceFoundException;
import org.mca.log.LogUtil;
import org.mca.scheduler.Task;

/**
 * 
 * @author cyril
 *
 */
public class ComputationCaseListener implements RemoteEventListener {

	private ComputationCase computationCase;
	
	private Map<String, Task> tasks;
	private Map<String, MCAProperty> properties;
	private Map<String, DataHandler> dataHandlers;
	
	public ComputationCaseListener(ComputationCase computationCase){
		this.computationCase = computationCase;
		tasks = new HashMap<String, Task>();
		properties = new HashMap<String, MCAProperty>();
		dataHandlers = new HashMap<String, DataHandler>();
	}
	
	public void start() throws MCASpaceException{
//		computationCase.registerForUpdates(this);
	}
	
	/**
	 * 
	 * @param computationCase
	 */
	private void add(Task task) {
		this.tasks.put(task.name, task);
		LogUtil.debug("Task [" + task.name + "] added",getClass());
	}

	private void add(Entry entry){
		LogUtil.error("Type of entry unknown [" + entry  + "]", getClass());
	}
	
	@Override
	public void notify(RemoteEvent event) throws UnknownEventException,
			RemoteException {
		AvailabilityEvent availabilityEvent = (AvailabilityEvent)event;
		try {
			Entry entry = availabilityEvent.getEntry();
			add(entry);
		} catch (UnusableEntryException e) {
			e.printStackTrace();
		}
	}

}
