package org.mca.server;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import net.jini.space.JavaSpace05;

import org.mca.core.ComponentInfo;
import org.mca.entry.ComputationCaseState;
import org.mca.entry.DataHandler;
import org.mca.entry.MCAProperty;
import org.mca.entry.State;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.EntryNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.math.Data;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

@SuppressWarnings("serial")
public class ComputationCaseImpl extends JavaSpaceParticipant implements ComputationCase{

	public ComputationCaseImpl(JavaSpace05 space) {
		setSpace(space);
	}
	
	@Override
	public void addProperty(MCAProperty property) throws MCASpaceException {
		MCAProperty template = new MCAProperty();
		template.name = property.name;
		try {
			takeEntry(template,null);
			LogUtil.debug("MCAProperty [" + template.name + "] exists.", getClass());
			writeEntry(property,  null);
			LogUtil.debug("MCAProperty [" + template.name + "] updated.", getClass());
		} catch (EntryNotFoundException e) {
			writeEntry(property, null);
			LogUtil.debug("MCAProperty [" + template.name + "] added.", getClass());
		}	
	}

	@Override
	public File downloadData(String name, String property)
			throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<MCAProperty> getProperties() throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTask(Task task) throws MCASpaceException {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws MCASpaceException {
		try {
			State state = new State();
			state = (State)takeEntry(state, null);
			state.state = ComputationCaseState.PAUSED;
			writeEntry(state, null);
		} catch (EntryNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String getState() throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateTask(Task taskInProgress) throws MCASpaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task getTask(TaskState waitForCompute) throws MCASpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void join(ComponentInfo componentInto) throws MCASpaceException {
		// TODO Auto-generated method stub
		
	}
	
	

}
