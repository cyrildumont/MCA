package org.mca.server;

import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import net.jini.core.entry.Entry;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;

import org.mca.entry.ComputationCaseState;
import org.mca.entry.DataHandler;
import org.mca.entry.MCAProperty;
import org.mca.entry.State;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.ComputationCaseInfo;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.math.Data;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

import com.sun.jini.outrigger.MCAOutriggerServerWrapper;


class ComputationCaseImpl extends JavaSpaceParticipant implements ComputationCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3127097490816772998L;
	
	private String name;
	private String description;

	ComputationCaseImpl(MCAOutriggerServerWrapper w) throws RemoteException {
		setSpace((JavaSpace05)w.space());
		Entry[] entries = w.getLookupAttributes();
		for (Entry entry : entries) {
			if (entry instanceof ComputationCaseInfo) {
				ComputationCaseInfo infos = (ComputationCaseInfo)entry;
				name = infos.name;
				description = infos.description;
				break;
			}
		}
	}

	@Override
	public void addProperty(MCAProperty property) throws MCASpaceException {
		MCAProperty template = new MCAProperty();
		template.name = property.name;

		takeEntry(template,null);
		LogUtil.debug("MCAProperty [" + template.name + "] exists.", getClass());
		writeEntry(property,  null);
		LogUtil.debug("MCAProperty [" + template.name + "] updated.", getClass());

	}

	@Override
	public File downloadData(String name, String property)
	throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() throws MCASpaceException {
		return this.name;
	}

	@Override
	public Collection<MCAProperty> getProperties() throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTask(Task task) throws MCASpaceException {
		writeEntry(task, null);
	}

	@Override
	public void addData(Data<?> data) throws MCASpaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDataHandler(DataHandler entry) throws MCASpaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public DataHandler removeDataHandler(String name) throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataHandler getDataHandler(String value) throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void uploadData(String dataHandlerName, FileInputStream fis)
	throws MCASpaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws MCASpaceException {

		State state = new State();
		state = (State)takeEntry(state, null);
		state.state = ComputationCaseState.STARTED;
		writeEntry(state, null);


	}

	@Override
	public void stop() throws MCASpaceException {
		State state = new State();
		state = (State)takeEntry(state, null);
		state.state = ComputationCaseState.PAUSED;
		writeEntry(state, null);


	}

	@Override
	public String getState() throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public void updateTask(Task task) throws MCASpaceException {
		getTask(task.name);
		addTask(task);
		LogUtil.debug("[" + this.name + "] Task [" + task.name + "] updated.", getClass());
	}

	@Override
	public Task getTask(TaskState state) throws MCASpaceException {
		Task taskTemplate = new Task();
		taskTemplate.state = state;
		return (Task)takeEntry(taskTemplate,null);
	}

	@Override
	public void join(RemoteEventListener listener) throws MCASpaceException {
		Task task = new Task();
		task.state = TaskState.WAIT_FOR_COMPUTE;
		Collection<Task> tasks = new ArrayList<Task>();
		tasks.add(task);
		try {
			space.registerForAvailabilityEvent(tasks, null, true, listener, Long.MAX_VALUE, null);
			//space.notify(task, null, listener, Long.MAX_VALUE, null);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TransactionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + "[name=" + name + ", description=" + description + "]";
	}

	@Override
	public Task getTask(String name) throws MCASpaceException {
		Task taskTemplate = new Task();
		taskTemplate.name = name;
		return (Task)takeEntry(taskTemplate, null);
	}

}
