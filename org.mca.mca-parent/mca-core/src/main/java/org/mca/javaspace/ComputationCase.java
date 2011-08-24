package org.mca.javaspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;

import net.jini.core.event.RemoteEventListener;

import org.mca.entry.ComputationCaseState;
import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.entry.Property;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.Data;
import org.mca.math.format.DataFormat;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

public interface ComputationCase extends Serializable {

	/**
	 * 
	 * @param property
	 * @throws MCASpaceException
	 */
	public void addProperty(Property property) throws MCASpaceException;

	public File download(String name, String dir) throws MCASpaceException;

	public String getName();

	public Collection<Property> getProperties() throws MCASpaceException;

	public void addTask(Task task) throws MCASpaceException;

	public void addData(Data<?> data, DataHandlerFactory factory) throws MCASpaceException;

	public void addDataHandler(DataHandler entry) throws MCASpaceException;

	public DataHandler removeDataHandler(String name) throws MCASpaceException;

	public DataHandler getDataHandler(String value) throws MCASpaceException;

	public void upload(String name, InputStream input) throws MCASpaceException;

	public void start() throws MCASpaceException;
	
	public void stop() throws MCASpaceException;
	
	public void finish() throws MCASpaceException;

	public ComputationCaseState getState() throws MCASpaceException;

	public String getDescription();

	public void updateTask(Task taskInProgress) throws MCASpaceException;

	public Task getTask(TaskState waitForCompute) throws MCASpaceException;
	
	public Task getTask(String name) throws MCASpaceException;

	void join(ComputationCaseListener listener) throws MCASpaceException;

	public Task getTaskToCompute(String hostname) throws MCASpaceException;

	public void updateTaskComputed(Task task) throws MCASpaceException;
	
	public <T extends Data<?>> T getData(String name, Class<T> formatClass);
	
}