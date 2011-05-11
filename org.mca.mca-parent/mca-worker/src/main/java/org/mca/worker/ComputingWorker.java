package org.mca.worker;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.ssl.SslServerEndpoint;
import net.jini.security.BasicProxyPreparer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.ComputeAgentInterface;
import org.mca.agent.TaskNotifierAgent;
import org.mca.agent.WaitForAnotherTaskException;
import org.mca.core.MCAComponent;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.MCASpaceEvent;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.CaseNotFoundException;
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
public class ComputingWorker  extends MCAComponent implements Observer, MCASpaceEventListener  {

	private static final String TEMP_WORKER_RESULT = "temp.worker.result";

	private static final String TEMP_WORKER_DOWNLOAD = "temp.worker.download";

	/** Log */
	private final static Log LOG = LogFactory.getLog(ComputingWorker.class);

	private static final String FILE_REGGIE = System.getProperty("mca.home") + "/conf/worker-reggie.xml";

	private MCASpace space;

	/** state of the ComputeWorker*/
	private ComputeWorkerState state; 

	private ComputeAgentInterface agent;

	/** */
	private Task taskInProgress;

	private AgentListener agentListener;

	private Map<String, String> properties;

	private transient Lookup lookup;

	private int nbTasksComputed;

	private ComputationCase computationCase;

	private transient final LoginContext loginContext;
	
	public ComputingWorker() throws Exception {
		loginContext = new LoginContext("org.mca.Worker");
		loginContext.login();
		try {
			Subject.doAsPrivileged(
					loginContext.getSubject(),
					new PrivilegedExceptionAction(){
						public Object run() throws Exception {
							init();
							return null;
						}
					},
					null);
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
			throw new Error();
		}
			
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
			
			startLookup();
			setState(ComputeWorkerState.IDLE);
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
			try {
				LookupLocator ll = new LookupLocator("jini://" + hostOfSpace);
				ServiceRegistrar registrar = ll.getRegistrar();
				Class<?>[] classes = new Class<?>[]{MCASpace.class};
				ServiceTemplate template = new ServiceTemplate(null, classes,null);
				space = (MCASpace)registrar.lookup(template);
				BasicProxyPreparer preparer = new BasicProxyPreparer(false, null);
				space = (MCASpace)preparer.prepareProxy(space);
			} catch (Exception e) {
				e.printStackTrace();
				setState(ComputeWorkerState.IDLE);
			}
			LogUtil.debug("[" + hostname + "] Connexion on MCASpace [" + hostOfSpace + "] : OK", getClass());
			ComputationCase computationCase = space.getCase(computationCaseName);
			this.computationCase = computationCase;
			LogUtil.debug("[" + hostname + "] ComputationCase [" + computationCase.getName() + "][" + computationCase.getDescription() + "]" +
					"[" + computationCase.getState() + "] joined", getClass());	
			computationCase.join(getComponentInto());
			LogUtil.debug("[" + hostname + "] [" + computationCase.getName() + "] Worker registered.", getClass());	
			agentListener = new AgentListener();
			setState(ComputeWorkerState.WAITING);
			run();	
		}catch(NoJavaSpaceFoundException e){
			LOG.error("[" + hostname + "] : MCASpace [" + hostOfSpace + "] not found.");
			setState(ComputeWorkerState.IDLE);
		}catch(CaseNotFoundException e){
			LOG.error("[" + hostname + "] : Case [" + computationCaseName + "]  not found " +
					"on MCASpace [" + hostOfSpace + "]");
			setState(ComputeWorkerState.IDLE);
		}catch (MCASpaceException e) {
			LOG.error("[" + hostname + "] Error to start the worker");
			setState(ComputeWorkerState.IDLE);
		} catch (RemoteException e) {
			LOG.error("[" + hostname + "] Error to start the worker");
			setState(ComputeWorkerState.IDLE);
		}
	}

	@ManagedOperation(description="connect to a MCASpace")
	public void connect(String hostOfSpace) throws CaseNotFoundException {
		System.out.println("connect");
		LogUtil.info("[" + hostname + "] join a MCASpace [" + hostOfSpace + "]", getClass());
		setState(ComputeWorkerState.STARTING);
		try {
			LogUtil.debug("[" + hostname + "] Connecting on MCASpace [" + hostOfSpace + "]", getClass());
			try {
				LookupLocator ll = new LookupLocator("jini://" + hostOfSpace);
				ServiceRegistrar registrar = ll.getRegistrar();
				Class<?>[] classes = new Class<?>[]{MCASpace.class};
				ServiceTemplate template = new ServiceTemplate(null, classes,null);
				space = (MCASpace)registrar.lookup(template);
				BasicProxyPreparer preparer = new BasicProxyPreparer(false, null);
				space = (MCASpace)preparer.prepareProxy(space);
			} catch (Exception e) {
				e.printStackTrace();
				setState(ComputeWorkerState.IDLE);
			}
			LogUtil.debug("[" + hostname + "] Connexion on MCASpace [" + hostOfSpace + "] : OK", getClass());
			Exporter exporter = new BasicJeriExporter(SslServerEndpoint.getInstance(0), new BasicILFactory());
			MCASpaceEventListener listener = (MCASpaceEventListener)exporter.export(this);
			EventRegistration event = space.register(listener);
		}catch(NoJavaSpaceFoundException e){
			LOG.error("[" + hostname + "] : MCASpace [" + hostOfSpace + "] not found.");
			setState(ComputeWorkerState.IDLE);
			e.printStackTrace();
		}catch (MCASpaceException e) {
			LOG.error("[" + hostname + "] Error to start the worker");
			setState(ComputeWorkerState.IDLE);
			e.printStackTrace();
		} catch (RemoteException e) {
			LOG.error("[" + hostname + "] Error to start the worker");
			setState(ComputeWorkerState.IDLE);
			e.printStackTrace();
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
		LogUtil.debug("[" + hostname + "] [" + computationCase.getName() + "] Check for Task to compute." , getClass());		
		Task task;
		try {
			task = this.computationCase.getTask(TaskState.WAIT_FOR_COMPUTE);
			this.taskInProgress = task;
			taskInProgress.setState(TaskState.ON_COMPUTING);
			taskInProgress.worker = hostname;
			this.computationCase.addTask(taskInProgress);
			return task;
		} catch (MCASpaceException e) {
			this.taskInProgress = null;
			LogUtil.debug("[" + hostname + "] [" + computationCase.getName() + "] No Task found.", getClass());
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
				LogUtil.debug("[" + hostname + "] [" + computationCase.getName() + "] New task available.", getClass());
				agent = (TaskNotifierAgent) object;
				if (isAvailable()) checkTask();
				else agent.next();
			}

		} catch (MCASpaceException e) {
			e.printStackTrace();
		}catch (RemoteException e) {
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

			LogUtil.info("[" + hostname + "] [" + computationCase.getName() + "] [" + taskInProgress.name + "] Computing ...." , getClass());
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
		}catch (MCASpaceException e) {
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
	 * @param state
	 */
	public void setState(ComputeWorkerState state) {
		LogUtil.debug("State changed : " + this.state + " --> " + state, getClass());
		this.state = state;
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

	public void notify(RemoteEvent event) throws UnknownEventException,
			RemoteException {
		if (event instanceof MCASpaceEvent) {
			MCASpaceEvent mcaEvt = (MCASpaceEvent) event;
		}
	}

	
}
