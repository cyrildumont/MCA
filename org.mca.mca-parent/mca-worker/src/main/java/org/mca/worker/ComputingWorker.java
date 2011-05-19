package org.mca.worker;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.ssl.SslServerEndpoint;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.space.AvailabilityEvent;

import org.apache.commons.io.FileUtils;
import org.mca.agent.ComputeAgent;
import org.mca.agent.TaskNotifierAgent;
import org.mca.agent.WaitForAnotherTaskException;
import org.mca.core.MCAComponent;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.MCASpaceEvent;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.listener.TaskNotifierAgentListener;
import org.mca.log.LogUtil;
import org.mca.model.Lookup;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.mca.util.MCAUtils;
import org.mca.worker.exception.AgentNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@SuppressWarnings("serial")
@ManagedResource(objectName = "MCA:type=ComputingWorker")
public class ComputingWorker extends MCAComponent implements Observer  {

	private static final String COMPONENT_NAME = "org.mca.Worker";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private static final String TEMP_WORKER_RESULT = "temp.worker.result";

	private static final String TEMP_WORKER_DOWNLOAD = "temp.worker.download";

	private static final String FILE_REGGIE = System.getProperty("mca.home") + "/conf/worker-reggie.xml";

	private MCASpace space;

	/** state of the ComputeWorker*/
	private ComputeWorkerState state; 

	private ComputeAgent agent;

	/** */
	private Task taskInProgress;

	private transient AgentListener agentListener;

	private transient Lookup lookup;

	private int nbTasksComputed;

	private ComputationCase computationCase;

	private transient final LoginContext loginContext;

	private transient WorkerMCASpaceEventListener spaceListener;

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
			start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	private void start(){
		setState(ComputeWorkerState.STARTED);
		agentListener = new AgentListener();		
		new Thread(new MCASpaceListener()).start();
		spaceListener = new WorkerMCASpaceEventListener();
		new Thread(spaceListener).start();
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
//		TaskNotifierAgentListener listener = new TaskNotifierAgentListener(this.lookup, this);
//		Thread listenerThread = new Thread(listener);
//		listenerThread.start();
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
		logger.fine("[" + hostname + "] [" + computationCase.getName() + "] check for task to compute.");		
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
			logger.warning("[" + hostname + "] [" + computationCase.getName() + "] No Task found.");
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

			logger.fine("[" + hostname + "] [" + computationCase.getName() + "] [" + taskInProgress.name + "] Computing ....");
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
			taskInProgress.message = e1.getClass().getName() + " -- " + e1.getMessage();
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
		logger.finest("Worker -- state changed : " + this.state + " --> " + state);
		this.state = state;
	}

	public void setSpace(MCASpace space) {
		this.space = space;
	}


	@ManagedAttribute
	public int getNbTasksComputed() {
		return nbTasksComputed;
	}


	private void setComputationCase(ComputationCase computationCase) {
		if (!state.equals(ComputeWorkerState.STARTED)) {
			logger.finest("Worker -- already connected");
		}else{
			this.computationCase = computationCase;
			setState(ComputeWorkerState.CONNECTED);
			logger.finest("Worker -- connected on " + computationCase);
			ComputationCaseListener ccl = new ComputationCaseListener(computationCase);
			new Thread(ccl).start();
			run();
		}
	}


	/**
	 * 
	 * @author cyril
	 *
	 */
	private class MCASpaceListener implements Runnable,ServiceDiscoveryListener{

		private transient LookupCache cache;

		public void serviceAdded(ServiceDiscoveryEvent event) {
			ServiceItem item =  event.getPostEventServiceItem();
			logger.finest("new MCASpace appears -- " + item);
			if(!state.equals(ComputeWorkerState.STARTED)){
				logger.finest("Worker is not able to connect an a MCASpace : [" + state + "]");
			}else{
				MCASpace space = (MCASpace)item.service;
				spaceListener.connect(space);
			}
		}

		public void serviceChanged(ServiceDiscoveryEvent event) {
			// DO NOTHING
		}

		public void serviceRemoved(ServiceDiscoveryEvent event) {
			ServiceItem item =  event.getPreEventServiceItem();
			logger.finest("a MCASpace disappears -- " + item);
		}

		public void run(){
			logger.finest("Worker -- MCASpaceListener started");
			try {
				LookupDiscoveryManager mgr = new LookupDiscoveryManager(LookupDiscovery.ALL_GROUPS, null, null);
				ServiceDiscoveryManager clientMgr = new ServiceDiscoveryManager(mgr, 
						new LeaseRenewalManager());
				Class<?>[] classes = new Class[]{MCASpace.class};
				ServiceTemplate template = new ServiceTemplate(null, classes,null);
				cache = clientMgr.createLookupCache(template, null, this); 
			} catch (IOException e) {
				logger.severe("[" + hostname + "] Error to start the worker");
				e.printStackTrace();
				setState(ComputeWorkerState.IDLE);
			} 
		}
		
		
	}

	/**
	 * 
	 * @author cyril
	 *
	 */
	private class WorkerMCASpaceEventListener implements Runnable, MCASpaceEventListener{

		private Exporter exporter;

		private MCASpaceEventListener proxy;

		public WorkerMCASpaceEventListener() {
			try {
				exporter = 
					new BasicJeriExporter(SslServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
				proxy = (MCASpaceEventListener)exporter.export(this);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}


		public void notify(RemoteEvent event) throws UnknownEventException,RemoteException {
			if (event instanceof MCASpaceEvent) {
				MCASpaceEvent mcaEvt = (MCASpaceEvent) event;
				logger.finest("MCASpaceEvent -- [" + mcaEvt.getID() + ", " + mcaEvt.getCase() + "]");
				setComputationCase(mcaEvt.getCase());
			}

		}

		public void run() {
			logger.finest("Worker -- MCASpaceEventListener started");
		}

		public void connect(MCASpace space){
			try {
				EventRegistration event = space.register(proxy);
				logger.finest("registration succeeded on -- " + event);
				ComputationCase computationCase = space.getCase();
				if (computationCase != null) {
					logger.finest("a case found on the MCASpace : [" + computationCase.getName() + "]");
					setComputationCase(computationCase);
				}else{
					logger.finest("no case found on the MCASpace");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}
	
	
	private class ComputationCaseListener implements RemoteEventListener, Runnable, Serializable{

		private ComputationCase computationCase;
		private Exporter exporter;
		private RemoteEventListener proxy;
		public ComputationCaseListener(ComputationCase computationCase) {
			this.computationCase = computationCase;
			try {
				exporter = 
					new BasicJeriExporter(SslServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
				proxy = (RemoteEventListener)exporter.export(this);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			try {
				computationCase.join(proxy);
			} catch (MCASpaceException e) {
				e.printStackTrace();
			}
			logger.finest("Worker -- ComputationCaseListener started");
		}
		
		public void notify(RemoteEvent event) throws UnknownEventException,
				RemoteException {
			AvailabilityEvent ae = (AvailabilityEvent)event;
			try {
				System.out.println(ae.getEntry());
			} catch (UnusableEntryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
