package org.mca.worker;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.space.AvailabilityEvent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.ComputeAgentInterface;
import org.mca.agent.TaskNotifierAgent;
import org.mca.agent.WaitForAnotherTaskException;
import org.mca.core.MCAComponent;
import org.mca.entry.ComputationCase;
import org.mca.entry.MCAProperty;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.exceptions.CaseNotFoundException;
import org.mca.javaspace.exceptions.EntryNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.javaspace.exceptions.NoJavaSpaceFoundException;
import org.mca.listener.TaskNotifierAgentListener;
import org.mca.log.LogUtil;
import org.mca.model.Lookup;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.mca.worker.exception.AgentNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@SuppressWarnings("serial")
@ManagedResource(objectName = "MCA:type=ComputingWorker")
public class ComputingWorker  extends MCAComponent implements Observer  {

	private static final String TEMP_WORKER_RESULT = "temp.worker.result";

	private static final String TEMP_WORKER_DOWNLOAD = "temp.worker.download";

	/** Log */
	private final static Log LOG = LogFactory.getLog(ComputingWorker.class);

	private static final String FILE_REGGIE = System.getProperty("mca.home") + "/conf/reggie.xml";

	private MCASpace space;

	private MCAPropertiesListener propertiesListener;

	/** state of the ComputeWorker*/
	private ComputeWorkerState state; 

	private ComputeAgentInterface agent;

	/** */
	private Task taskInProgress;

	private AgentListener agentListener;

	private Map<String, String> properties;

	private Lookup lookup;

	private int nbTasksComputed;

	private ComputationCase computationCase;

	public ComputingWorker() {
		init();
		startLookup();
		setState(ComputeWorkerState.IDLE);
	}

	/**
	 * 
	 */
	private void init() {
		try {
			String tmpDir = System.getProperty("mca.home") + "/work/" + hostname;
			System.setProperty("mca.worker.dir", tmpDir);
			File dirResult = new File(tmpDir + "/result");
			File dirDownload = new File(tmpDir + "/download");
			if(!dirResult.exists()){
				FileUtils.forceMkdir(dirResult);
			}else {
				FileUtils.cleanDirectory(dirResult);
			}
			if(!dirDownload.exists()){
				FileUtils.forceMkdir(dirDownload);
			}else {
				FileUtils.cleanDirectory(dirDownload);
			}
			System.setProperty(TEMP_WORKER_DOWNLOAD, dirDownload.getPath());
			System.setProperty(TEMP_WORKER_RESULT, dirResult.getPath());

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@ManagedOperation(description="join a computation case on the worker")
	public void start(String hostOfSpace,String computationCaseName) throws CaseNotFoundException {
		LogUtil.info("[" + hostname + "] join a case [" + computationCaseName + "] on MCASpace [" + hostOfSpace + "]", getClass());
		setState(ComputeWorkerState.STARTING);
		try {
			LogUtil.debug("[" + hostname + "] Connecting on MCASpace [" + hostOfSpace + "]", getClass());
			space = new MCASpace(hostOfSpace);
			LogUtil.debug("[" + hostname + "] Connexion on MCASpace [" + hostOfSpace + "] : OK", getClass());
			ComputationCase computationCase = space.getCase(computationCaseName);
			this.computationCase = computationCase;
			LogUtil.debug("[" + hostname + "] ComputationCase [" + computationCase.name + "][" + computationCase.description + "]" +
					"[" + computationCase.state + "] joined", getClass());	
			computationCase.registerWorker(getComponentInto());
			LogUtil.debug("[" + hostname + "] [" + computationCase.name + "] Worker registered.", getClass());	
			agentListener = new AgentListener();
			setState(ComputeWorkerState.WAITING);
			run();	
		}catch(NoJavaSpaceFoundException e){
			LOG.error("[" + hostname + "] : MCASpace [" + hostOfSpace + "] not found.");
			setState(ComputeWorkerState.IDLE);
		}
		catch(CaseNotFoundException e){
			LOG.error("[" + hostname + "] : Case [" + computationCaseName + "]  not found " +
					"on MCASpace [" + hostOfSpace + "]");
			setState(ComputeWorkerState.IDLE);
		}catch (MCASpaceException e) {
			LOG.error("[" + hostname + "] Error to start the worker");
			setState(ComputeWorkerState.IDLE);
		}
	}

	/**
	 * 
	 * 
	 */
	private void startLookup() {

		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + FILE_REGGIE);
		ServiceConfigurator serviceConfigurator = 
			context.getBean("reggie", ServiceConfigurator.class);
		ServiceStarter starter = new ServiceStarter(serviceConfigurator);
		Object registrar = starter.startWithoutAdvertise();
		this.lookup = new Lookup(registrar);
		TaskNotifierAgentListener listener = new TaskNotifierAgentListener(this.lookup, this);
		Thread listenerThread = new Thread(listener);
		listenerThread.start();
	}

	/**
	 * 
	 *
	 */
	private void run() {
		try {
			setState(ComputeWorkerState.RUNNING);
			while(checkTask() != null){
				makeTask();
			}
			setState(ComputeWorkerState.WAITING);
		}catch (MCASpaceException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
	}


	/**
	 * 
	 * @param task
	 * @throws MCASpaceException 
	 */

	private Task checkTask() throws MCASpaceException {
		LogUtil.debug("[" + hostname + "] [" + computationCase.name + "] Check for Task to compute on [" + computationCase.host + "]" , getClass());		
		Task task;
		try {
			task = this.computationCase.getTask(TaskState.WAIT_FOR_COMPUTE);
			this.taskInProgress = task;
			taskInProgress.setState(TaskState.ON_COMPUTING);
			taskInProgress.worker = hostname;
			this.computationCase.addTask(taskInProgress);
			return task;
		} catch (EntryNotFoundException e1) {
			this.taskInProgress = null;
			LogUtil.debug("[" + hostname + "] [" + computationCase.name + "] No Task found on [" + computationCase.host + "]", getClass());
			return null;
		}

	}

	/**
	 * 
	 */
	public void update(Observable observable, Object object) {
		try {

			TaskNotifierAgent agent = null;
			if (object instanceof TaskNotifierAgent) {
				LogUtil.debug("[" + hostname + "] [" + computationCase.name + "] New task available.", getClass());
				agent = (TaskNotifierAgent) object;
				if (isAvailable()) checkTask();
				else agent.next();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * @throws MCASpaceException
	 * @throws RemoteException
	 */
	private void makeTask() throws RemoteException {

		try {
			agent = agentListener.getAgent(taskInProgress.computing_agent_name);
			agent.setCase(computationCase);

			LogUtil.info("[" + hostname + "] [" + computationCase.name + "] [" + taskInProgress.name + "] Computing ...." , getClass());
			try {
				taskInProgress.result = agent.compute(taskInProgress);
				LogUtil.info(taskInProgress.name + " computed.", getClass());
				taskInProgress.setState(TaskState.COMPUTED);
				nbTasksComputed++;
			} catch (WaitForAnotherTaskException e) {
				LogUtil.debug("The task [" + taskInProgress.name + "] can't be computed.", getClass());	
				for (Task task : e.getTasks()) {
					taskInProgress.addParentTask(task.name);	
				}
				taskInProgress.setState(TaskState.WAIT_FOR_ANOTHER_TASK);
			} catch (Exception e) {
				e.printStackTrace();
				taskInProgress.message = e.getMessage();
				taskInProgress.setState(TaskState.ON_ERROR);

			}
		}catch (AgentNotFoundException e1) {
			LogUtil.error("Agent [" + taskInProgress.computing_agent_name + "] not found", getClass());
			taskInProgress.setState(TaskState.ON_ERROR);
		}
		try {
			this.computationCase.updateTask(taskInProgress);
		} catch (EntryNotFoundException e) {
			e.printStackTrace();
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	@ManagedAttribute
	public String getTaskInProgress(){
		return taskInProgress != null ? taskInProgress.name : "no task";
	}

	/**
	 * @return the state
	 */
	@ManagedAttribute
	public String getState() {
		return state.toString();
	}

	public boolean isAvailable(){
		return state.equals(ComputeWorkerState.WAITING);
	}

	/**
	 * 
	 * @author Cyril
	 *
	 */
	private class MCAPropertiesListener implements RemoteEventListener{


		public MCAPropertiesListener(ComputationCase computationCase) {
			try {
				computationCase.registerForMCAProperties(this);
			} catch (MCASpaceException e) {
				e.printStackTrace();
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("MCAPropertiesListener started ...");
			}
		}

		/**
		 * 
		 */
		public void notify(RemoteEvent event) throws UnknownEventException, RemoteException {
			AvailabilityEvent availabilityEvent = (AvailabilityEvent)event;
			try {
				MCAProperty property =  (MCAProperty) availabilityEvent.getEntry();
				if (properties != null ) {
					addProperty(property.name, property.value);
				}
			} catch (UnusableEntryException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param state
	 */
	public void setState(ComputeWorkerState state) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("State changed : " + this.state + " --> " + state);
		}
		this.state = state;
	}

	public void addProperty(String name, String value) {
		properties.put(name, value);
		LogUtil.debug("properties updated", getClass());
	}

	public void setSpace(MCASpace space) {
		this.space = space;
	}


	@ManagedAttribute
	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
		LogUtil.debug("MCAProperties updated", getClass());

	}


	@ManagedAttribute
	public int getNbTasksComputed() {
		return nbTasksComputed;
	}


}
