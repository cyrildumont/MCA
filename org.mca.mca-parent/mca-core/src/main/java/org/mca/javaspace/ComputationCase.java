package org.mca.javaspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Collection;

import net.jini.core.event.RemoteEventListener;

import org.mca.core.ComponentInfo;
import org.mca.entry.DataHandler;
import org.mca.entry.MCAProperty;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.Data;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

public interface ComputationCase extends Serializable {

	/**
	 * 
	 * @param property
	 * @throws MCASpaceException
	 */
	public void addProperty(MCAProperty property) throws MCASpaceException;

	public File downloadData(String name, String property) throws MCASpaceException;

	public String getName() throws MCASpaceException;

	public Collection<MCAProperty> getProperties() throws MCASpaceException;

	public void addTask(Task task) throws MCASpaceException;

	public void addData(Data<?> data) throws MCASpaceException;

	public void addDataHandler(DataHandler entry) throws MCASpaceException;

	public DataHandler removeDataHandler(String name) throws MCASpaceException;

	public DataHandler getDataHandler(String value) throws MCASpaceException;

	public void uploadData(String dataHandlerName, FileInputStream fis) throws MCASpaceException;

	public void start() throws MCASpaceException;
	
	public void stop() throws MCASpaceException;

	public String getState() throws MCASpaceException;

	public String getDescription();

	public void updateTask(Task taskInProgress) throws MCASpaceException;

	public Task getTask(TaskState waitForCompute) throws MCASpaceException;
	
	public Task getTask(String name) throws MCASpaceException;

	void join(RemoteEventListener listener) throws MCASpaceException;
	
}