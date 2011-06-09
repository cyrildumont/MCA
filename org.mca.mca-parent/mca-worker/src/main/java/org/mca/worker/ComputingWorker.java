package org.mca.worker;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
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

import org.apache.commons.io.FileUtils;
import org.mca.agent.ComputeAgent;
import org.mca.agent.ComputeAgentException;
import org.mca.agent.WaitForAnotherTaskException;
import org.mca.core.MCAComponent;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.MCASpaceEvent;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.MCASpaceException;
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
public class ComputingWorker extends MCAComponent {

	private static final String COMPONENT_NAME = "org.mca.worker.Worker";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private static final String TEMP_WORKER_RESULT = "temp.worker.result";

	private static final String TEMP_WORKER_DOWNLOAD = "temp.worker.download";

	private static final String FILE_REGGIE = 
		System.getProperty("mca.home") + "/conf/worker-reggie.xml";

	private static final String[] CONFIG_FILES = 
		new String[]{System.getProperty("mca.home") + "/conf/worker.config"};

	private final Configuration config = 
		ConfigurationProvider.getInstance(CONFIG_FILES);

	/** state of the ComputeWorker*/
	private ComputeWorkerState state; 

	/** */
	private Task taskInProgress;

	private transient AgentListener agentListener;


	private int nbTasksComputed;

	private ComputationCase computationCase;

	private transient final LoginContext loginContext;

	private transient MCASpaceListener spaceListener;

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
		spaceListener = new MCASpaceListener();
		spaceListener.start();
		//		spaceListener = new WorkerMCASpaceEventListener();
		//		new Thread(spaceListener).start();
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
		logger.fine("Worker -- state changed : " + this.state + " --> " + state);
		this.state = state;
	}

	@ManagedAttribute
	public int getNbTasksComputed() {
		return nbTasksComputed;
	}

	private void setComputationCase(ComputationCase computationCase) {
		if (!state.equals(ComputeWorkerState.STARTED)) {
			logger.fine("Worker -- already connected");
		}else{
			this.computationCase = computationCase;
			setState(ComputeWorkerState.CONNECTED);
			spaceListener.disconnect();
			logger.fine("Worker -- connected on " + computationCase);
			TaskExecutor te = new TaskExecutor();;
			te.start();
		}
	}

	private void setComputationCaseFinish(){
		computationCase = null;
		setState(ComputeWorkerState.STARTED);
		spaceListener.listen();
	}
	/**
	 * 
	 * @author cyril
	 *
	 */
	private class MCASpaceListener extends Thread implements ServiceDiscoveryListener, DiscoveryListener{

		private transient LookupCache cache;

		private WorkerMCASpaceEventListener eventListener;

		private boolean interrupted = false;

		public MCASpaceListener() {
			super("mcaspace discovery listener thread");
			setDaemon(true);
		}

		public void serviceAdded(ServiceDiscoveryEvent event) {
			ServiceItem item =  event.getPostEventServiceItem();
			logger.fine("Worker -- new MCASpace appears: " + item);
			MCASpace space = (MCASpace)item.service;
			eventListener.connect(space);
		}

		public void serviceChanged(ServiceDiscoveryEvent event) {
			// DO NOTHING
		}

		public void serviceRemoved(ServiceDiscoveryEvent event) {
			ServiceItem item =  event.getPreEventServiceItem();
			logger.fine("Worker -- a MCASpace disappears: " + item);

		}

		public void listen(){
			eventListener = new WorkerMCASpaceEventListener();
			eventListener.start();
			cache.addListener(this);
		}

		public void disconnect(){
			cache.removeListener(this);
			eventListener.interrupt();
		}

		public void run(){
			logger.fine("Worker -- MCASpaceListener started");
			try {
				LookupDiscoveryManager mgr = new LookupDiscoveryManager(LookupDiscovery.ALL_GROUPS, null, this,config);
				ServiceDiscoveryManager clientMgr = new ServiceDiscoveryManager(mgr, 
						new LeaseRenewalManager());
				Class<?>[] classes = new Class[]{MCASpace.class};
				ServiceTemplate template = new ServiceTemplate(null, classes,null);
				cache = clientMgr.createLookupCache(template, null, null); 
				listen();
				while (!interrupted) {
					try{
						Thread.sleep(1000);
					}catch (InterruptedException e) {
						interrupted = true;
					}
				}
			} catch (Exception e) {
				logger.severe("Worker -- error to start: " + e);
				System.exit(-1);
			} 
		}

		public synchronized void interrupt(){
			interrupted = true;
			super.interrupt();
		}

		@Override
		public void discarded(DiscoveryEvent event) {
			logger.fine("lookup discarded : " + event);
		}

		@Override
		public void discovered(DiscoveryEvent event) {
			logger.fine("lookup discovered : " + event);
		}
	}

	/**
	 * 
	 * @author Cyril
	 *
	 */
	private class WorkerMCASpaceEventListener extends Thread implements MCASpaceEventListener{

		private Exporter exporter;

		private MCASpaceEventListener proxy;

		private boolean interrupted = false;

		public WorkerMCASpaceEventListener() {
			super("mcaspace event listener thread");
			setDaemon(true);
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
				long eventID = mcaEvt.getID();
				ComputationCase computationCase = mcaEvt.getCase();
				logger.fine("MCASpaceEvent -- [" + eventID + ", " + computationCase + "]");
				switch ((int)eventID) {
				case MCASpace.ADD_CASE:
					logger.fine("Worker -- new computation case appears on a MCASpace: "+ computationCase);
					if(state.equals(ComputeWorkerState.STARTED)){
						setComputationCase(mcaEvt.getCase());
					}
					break;
				case MCASpace.REMOVE_CASE:
					break;
				default:
					break;
				}
			}
		}

		public void run() {
			logger.finest("Worker -- MCASpaceEventListener started");
			while (!interrupted) {
				try{
					Thread.sleep(1000);
				}catch (InterruptedException e) {}
			}
		}

		public void connect(MCASpace space){
			try {
				EventRegistration event = space.register(proxy);
				logger.fine("Worker -- registration succeeded on [" + space + "]: "  + event);
				if(state.equals(ComputeWorkerState.STARTED)){
					ComputationCase computationCase = space.getCase();
					if (computationCase != null) {
						logger.fine("Worker -- a case found on the MCASpace : [" + computationCase.getName() + "]");
						setComputationCase(computationCase);
					}else{                                                                            
						logger.fine("Worker -- no case found on the MCASpace");
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		public synchronized void interrupt(){
			interrupted = true;
			super.interrupt();
		}
	}

	/**
	 * 
	 * @author Cyril
	 *
	 */
	private class TaskExecutor extends Thread{

		private boolean interrupted;

		public TaskExecutor() {
			super("taskexecutor thread");
			setDaemon(true);
			logger.fine("Worker -- TaskExecutor started");
		}

		public synchronized void run() {
			try {
				setState(ComputeWorkerState.WAITING);
				interrupted = false;
				while(!interrupted){
					executeTask();
					try {
						sleep(3000);
					} catch (InterruptedException e) {
						interrupted= true;
					}
				}
			}catch (Exception e) {
				logger.warning("Worker -- error during TaskExecutor execution : " + e.getMessage() );
				interrupt();
			}

		}

		public void interrupt(){
			super.interrupt();
			interrupted = true;
			logger.fine("Worker -- TaskExecutor stopped");
			setComputationCaseFinish();
		}

		/**
		 * 
		 * @throws MCASpaceException
		 * @throws RemoteException
		 */
		private void executeTask(){
			logger.finest("Worker -- check for task to compute: [" + computationCase.getName() + "]");	
			Task task = computationCase.getTaskToCompute(hostname);
			if (task == null){
				logger.finest("Worker -- No task to compute: [" + computationCase.getName() + "]");	
				return;
			}
			taskInProgress = task;
			
			setState(ComputeWorkerState.RUNNING);	
			try {
				ComputeAgent agent = agentListener.getAgent(taskInProgress.computing_agent_name);
				agent.setCase(computationCase);
				logger.fine("Worker -- [" + computationCase.getName() + "] [" + taskInProgress.name + "] Computing ....");
				taskInProgress.result = agent.compute(taskInProgress);
				logger.fine("Worker -- task [" + taskInProgress.name + "] computed.");
				taskInProgress.setState(TaskState.COMPUTED);
				nbTasksComputed++;
			} catch (ComputeAgentException e) {
				logger.warning("Worker -- ComputeAgent execution error : " + e.getMessage());
				taskInProgress.message = e.getMessage();
				taskInProgress.setState(TaskState.ON_ERROR);
			}catch (AgentNotFoundException e1) {
				logger.warning("Agent [" + taskInProgress.computing_agent_name + "] not found");
				taskInProgress.setState(TaskState.ON_ERROR);
				taskInProgress.message = e1.getMessage();
			}finally{
				try {
					computationCase.updateTaskComputed(taskInProgress);
				}catch (MCASpaceException e) {
					logger.warning("Worker -- Error during update of  the task in progress :" + e.getMessage());
				}
				setState(ComputeWorkerState.WAITING);
			}
		}
	}
}
