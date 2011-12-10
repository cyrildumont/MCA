package org.mca.javaspace;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.jini.core.event.RemoteEventListener;

import org.mca.data.DDataStructure;
import org.mca.entry.ComputationCaseState;
import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.entry.Property;
import org.mca.ft.Checkpoint;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.listener.TaskListener;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

public interface ComputationCase extends Serializable {
	
	public void addProperty(Property property) throws MCASpaceException;

	public String getName();

	public Collection<Property> getProperties() throws MCASpaceException;

	public void addTask(Task task) throws MCASpaceException;

	
	public Checkpoint getLastCheckpoint();
	
	public void checkpoint(Checkpoint checkpoint) throws MCASpaceException;
	
	/**
	 * Add a list of tasks
	 * 
	 * @param task
	 * @throws MCASpaceException
	 */
	public void addTasks(List<? extends Task<?>> task) throws MCASpaceException;
	
	public void addData(DDataStructure<?> data,String name, DataHandlerFactory factory) throws MCASpaceException;

	public void addDataHandler(DataHandler entry) throws MCASpaceException;

	public DataHandler removeDataHandler(String name) throws MCASpaceException;
	
	public DataHandler removeDataHandler(String name, int part) throws MCASpaceException;

	public DataHandler getDataHandler(String name) throws MCASpaceException;
	
	public DataHandler getDataHandler(String name, int part) throws MCASpaceException;

	public void upload(String name, InputStream input) throws MCASpaceException;

	public void upload(String name, int part, InputStream input) throws MCASpaceException;

	public File download(String name, String dir) throws MCASpaceException;

	public File download(String name, int part, String dir) throws MCASpaceException;
	
	public void start() throws MCASpaceException;
	
	public void stop() throws MCASpaceException;
	
	public void finish() throws MCASpaceException;

	public ComputationCaseState getState() throws MCASpaceException;

	public String getDescription();

	public void updateTask(Task task) throws MCASpaceException;

	public Task getTask(TaskState state) throws MCASpaceException;
	
	public Collection<Task<?>> getTasks(TaskState state, int maxTasks) throws MCASpaceException;
	
	public Task getTask(String name) throws MCASpaceException;

	public void join(ComputationCaseListener listener) throws MCASpaceException;

	public void unjoin() throws MCASpaceException;
	
	/**
	 * take WAIT_FOR_COMPUTE state tasks
	 * 
	 * @return
	 * @throws MCASpaceException
	 */
	public Collection<? extends Task<?>> getTaskToCompute() throws MCASpaceException;
	
	/**
	 * update a collection of task
	 * 
	 * @param task
	 * @throws MCASpaceException
	 */
	public void updateTaskComputed(List<Task<?>> task) throws MCASpaceException;
	
	public <T extends DDataStructure<?>> T getData(String name) throws MCASpaceException;

	public void barrier(String name) throws MCASpaceException;
	
	/**
	 * 
	 * @param name
	 * @param rank
	 * @param neighbors
	 * @throws MCASpaceException
	 */
	public void barrier(String name, Integer rank, Integer[] neighbors) throws MCASpaceException;
	
	public void createBarrier(String name, int size) throws MCASpaceException;
	
	public void removeBarrier(String name) throws MCASpaceException;
	
	public void registerForTasks(Collection<Task> pendingTasks, TaskListener listener) throws MCASpaceException;
	
	/**
	 * Recover one result 
	 * state of the corresponding task : COMPUTED --> RECOVERED 
	 * @throws MCASpaceException
	 */
	public <R> R recoverResult() throws MCASpaceException;

	/**
	 * Recover at most maxResults result 
	 * state of corresponding tasks : COMPUTED --> RECOVERED 
	 * @throws MCASpaceException
	 */
	public <R> Collection<R> recoverResults(int maxResults) throws MCASpaceException;
	
	/**
	 * return a task corresponded to the template
	 * 
	 * @param template
	 * @param transaction 
	 * @return
	 */
	public Task<?> getTask(Task<?> template)throws MCASpaceException;
	
	/**
	 * 
	 * @param template
	 * @param maxTasks
	 * @return
	 * @throws MCASpaceException
	 */
	public Collection<Task<?>> getTasks(Task<?> template, int maxTasks) throws MCASpaceException;
	
	/**
	 * 
	 * @param name
	 * @param listener
	 * @return
	 * @throws MCASpaceException
	 */
	public Collection<DataHandler> listenDataPart(String name, RemoteEventListener listener) throws MCASpaceException;
	
}