package org.mca.worker;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.ssl.SslServerEndpoint;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.space.JavaSpace05;

import org.apache.commons.io.FileUtils;
import org.mca.agent.ComputeAgent;
import org.mca.agent.ComputeAgentException;
import org.mca.core.MCAComponent;
import org.mca.entry.ComputationCaseState;
import org.mca.ft.FTContext;
import org.mca.ft.FTManager;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.ComputationCaseListener;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.MCASpaceEvent;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.mca.service.ServiceConfigurator;
import org.mca.util.Constants;
import org.mca.util.MCAUtils;
import org.mca.worker.exception.AgentNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * 
 * 
 * @author Cyril Dumont
 *
 */
@ManagedResource(objectName = "MCA:type=ComputingWorker")
public class ComputingWorker extends MCAComponent {

	private static final long serialVersionUID = 1L;

	private static final int READ_TASK_INTERVAL = 3000;
	
	private static final long LEASE_RENEW_DURATION = 15000;

	private static final String COMPONENT_NAME = "org.mca.worker.Worker";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private static final String TEMP_WORKER_RESULT = "temp.worker.result";

	private static final String TEMP_WORKER_DOWNLOAD = "temp.worker.download";

	public static final String TEMP_WORKER = "temp.worker";

	private static final String[] CONFIG_FILES = 
		new String[]{System.getProperty("mca.home") + "/conf/worker.config"};

	private static final String FILE_SERVICES = System.getProperty("mca.home") + "/conf/worker-services.xml";
	
	private static final String SERVICE_JAVASPACE = "javaspace";

	/** state of the ComputeWorker*/
	private ComputeWorkerState state; 

	/** */
	private Task taskInProgress;

	private transient AgentListener agentListener;

	private int nbTasksComputed;

	private ComputationCase computationCase;

	private transient MCASpaceListener spaceListener;

	private transient CaseStateListener caseListener;

	private transient TaskExecutor taskExecutor;

	private transient FTManager ftManager;

	public boolean signalStop = false;
	public boolean signalFinish = false;

	private transient LeaseRenewalManager leaseManager;
	
	private boolean secure;

	public void setSecure(boolean secure) {
		this.secure = secure;
	}


	/**
	 * 
	 */
	private void init() throws Exception{
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
		System.setProperty(TEMP_WORKER, tmpDir);
		System.setProperty(TEMP_WORKER_DOWNLOAD, dirDownload.getPath());
		System.setProperty(TEMP_WORKER_RESULT, dirResult.getPath());
		startServices();
		setState(ComputeWorkerState.IDLE);
	}

	/**
	 * 
	 */
	public void start() throws Exception{
		init();
		leaseManager = new LeaseRenewalManager();
		agentListener = new AgentListener(secure);	
		spaceListener = new MCASpaceListener();
		spaceListener.start();
		caseListener = new CaseStateListener();
		setState(ComputeWorkerState.STARTED);
	}

	/**
	 * 
	 * Start a local lookup to shared data with another worker
	 * 
	 */
	private void startServices() throws Exception{
		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + FILE_SERVICES);
		ServiceConfigurator config = 
			context.getBean(SERVICE_JAVASPACE, ServiceConfigurator.class);
		JavaSpace05 space = (JavaSpace05)startService(config);
		ftManager = FTManager.getInstance(space);
		FTContext recoveredContext = ftManager.recoverContext();
		logger.fine("MCAWorker -- recovered context : " + recoveredContext);
		Task recoveredTask = ftManager.recoverCurrentTask();
		logger.fine("MCAWorker -- recovered task : " + recoveredTask);
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


	/**
	 * 
	 * @param computationCase
	 * @throws MCASpaceException
	 */
	private void setComputationCase(ComputationCase computationCase) throws MCASpaceException {
		caseListener.addCase(computationCase);
		if (computationCase.getState() == ComputationCaseState.STARTED) {
			this.computationCase = computationCase;
			spaceListener.disconnect();
			setState(ComputeWorkerState.WAITING);
			logger.fine("Worker -- connected on " + computationCase);
			taskExecutor = new TaskExecutor();
			taskExecutor.start();
			FTContext context = new FTContext();
			context.computationCase = computationCase.getName();
			ftManager.saveCurrentContext(context);
		}

	}

	private void setComputationCaseFinish() throws MCASpaceException{
		computationCase.unjoin();
		computationCase = null;
		setState(ComputeWorkerState.STARTED);
		try {
			spaceListener.listen();
		} catch (ExportException e) {
			throw new MCASpaceException();
		}
	}

	/**
	 * 
	 * @author Cyril Dumont
	 *
	 */
	private class MCASpaceListener extends Thread implements DiscoveryListener{

		private transient LookupCache cache;

		private WorkerMCASpaceEventListener eventListener;

		private boolean interrupted = false;

		public MCASpaceListener() {
			super("mcaspace discovery listener thread");
			setDaemon(true);
		}

		public void listen() throws ExportException{
			eventListener = new WorkerMCASpaceEventListener();
			eventListener.start();
		}

		public void disconnect(){
			eventListener.interrupt();
		}

		public void run(){
			logger.fine("Worker -- MCASpaceListener started");
			try {
				Configuration config = 
					ConfigurationProvider.getInstance(CONFIG_FILES);
				LookupDiscoveryManager mgr = 
					new LookupDiscoveryManager(Constants.MCA_SERVER_GROUPS, null, this,config);
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
				logger.throwing("MCASpaceListener", "run", e);
				System.exit(-1);
			} 
		}

		public synchronized void interrupt(){
			interrupted = true;
			super.interrupt();
		}

		@Override
		public void discarded(DiscoveryEvent event) {
			ServiceRegistrar[] registrars = event.getRegistrars();
			ServiceRegistrar registrar = registrars[0];
			logger.fine("MCASpaceListener -- lookup discarded on [" + registrar + "]");
		}

		@Override
		public void discovered(DiscoveryEvent event) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			ServiceRegistrar[] registrars = event.getRegistrars();
			ServiceRegistrar registrar = registrars[0];
			try {
				LookupLocator locator = registrar.getLocator();
				logger.fine("MCASpaceListener -- lookup discovered on [" + locator.getHost() + ":" + locator.getPort() + "]");
				Class<?>[] classes = new Class[]{MCASpace.class};
				ServiceTemplate template = new ServiceTemplate(null, classes,null);
				MCASpace space = (MCASpace)registrar.lookup(template);
				eventListener.connect(space);
			} catch (RemoteException e) {
				logger.warning("Worker - MCASpaceListener - error to get the MCASpace on [" + registrar + "]");
				logger.throwing("MCASpaceListener", "discovered", e);
			}
		}
	}

	private Object getSSLProxy(Remote remote) throws ExportException{
		Exporter exporter = 
			new BasicJeriExporter(SslServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
		return exporter.export(remote);
	}

	/**
	 * 
	 * @author Cyril Dumont
	 *
	 */
	private class WorkerMCASpaceEventListener extends Thread implements MCASpaceEventListener{

		private MCASpaceEventListener proxy;

		private boolean interrupted = false;

		public WorkerMCASpaceEventListener() throws ExportException {
			super("mcaspace event listener thread");
			setDaemon(true);
			proxy = (MCASpaceEventListener)getSSLProxy(this);
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
					logger.fine("Worker -- computation case [" + computationCase + "] disappears on a MCASpace: ");
					break;
				default:
					logger.fine("Worker -- WorkerMCASpaceEventListener : unknow event [" + mcaEvt + "]");
					break;
				}
			}
		}

		public void run() {
			logger.fine("Worker -- MCASpaceEventListener started");
			while (!interrupted) {
				try{
					Thread.sleep(1000);
				}catch (InterruptedException e) {}
			}
		}

		public void connect(MCASpace space){
			try {
				EventRegistration event = space.register(proxy, LEASE_RENEW_DURATION);
				Lease lease = event.getLease();
				leaseManager.renewFor(lease, Lease.FOREVER, LEASE_RENEW_DURATION,null);
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
				logger.warning("Worker -- Impossible to connect to [" + space + "]");
				logger.throwing("WorkerMCASpaceEventListener", "connect", e);
			}
		}

		public synchronized void interrupt(){
			interrupted = true;
			super.interrupt();
		}
	}

	/**
	 * 
	 * @author Cyril Dumont
	 *
	 */
	private class TaskExecutor extends Thread{

		public TaskExecutor() {
			super("taskexecutor thread");
			setDaemon(true);
		}

		public synchronized void run() {
			logger.fine("Worker -- TaskExecutor started");
			signalStop = false;
			try {
				while(!signalStop && !signalFinish){
					Collection<Task<?>> tasksToCompute = getTasksToCompute();
					if (tasksToCompute == null || tasksToCompute.isEmpty()){
						logger.finest("Worker -- No task to compute: [" + computationCase.getName() + "]");	
						Thread.sleep(READ_TASK_INTERVAL);
					}else{
						executeTasks(tasksToCompute);
					}
				}
				if(signalFinish){
					setComputationCaseFinish();
				}
			}catch (Exception e) {
				logger.warning("Worker -- error during TaskExecutor execution : " + e.getMessage() );
				logger.throwing("TaskExecutor", "run", e);
			}
		}

		/**
		 * 
		 * @throws MCASpaceException
		 * @throws RemoteException
		 */
		private Collection<Task<?>> getTasksToCompute() {
			logger.finest("Worker -- check tasks to compute: [" + computationCase.getName() + "]");	
			try {
				return (Collection<Task<?>>)computationCase.getTaskToCompute();
			} catch (MCASpaceException e) {
				logger.fine("Worker -- Impossible to get new tasks to compute [" + computationCase.getName() + "]");	
				logger.throwing("TaskExecutor", "getTasksToCompute", e);
				try {
					setComputationCaseFinish();
				} catch (MCASpaceException e2) {
					e2.printStackTrace();
				}
				return null;
			}
		}


		/**
		 * 
		 * 
		 * @param tasksToCompute
		 * @throws Exception
		 */
		private void executeTasks(Collection<Task<?>> tasksToCompute) throws Exception{
			setState(ComputeWorkerState.COMPUTING);
			List<Task<?>> tasksComputed = new ArrayList<Task<?>>();
			for (Task<?> task : tasksToCompute) {
				ftManager.saveCurrentTask(task);
				executeTask(task);
				tasksComputed.add(task);
				ftManager.removeCurrentTask();
			}
			try {
				computationCase.updateTaskComputed(tasksComputed);
				setState(ComputeWorkerState.RUNNING);
			}catch (MCASpaceException e) {
				logger.warning("Worker -- Error during update of  the task in progress :" + e.getMessage());
				logger.throwing("TaskExecutor", "executeTask", e);
			}
		}
		/**
		 * execute a task of the current computation case
		 * 
		 * @param task
		 */
		private void executeTask(Task task){
			taskInProgress = task;	
			try {
				ComputeAgent agent = agentListener.getAgent(task.compute_agent_url);
				agent.setCase(computationCase);
				logger.fine("Worker -- [" + computationCase.getName() + "] [" + task.name + "] Computing ....");
				task.result = agent.compute(task);
				logger.fine("Worker -- task [" + taskInProgress.name + "] computed.");
				task.setState(TaskState.COMPUTED);
				nbTasksComputed++;
			} catch (ComputeAgentException e) {
				logger.warning("Worker -- ComputeAgent execution error : " + e.getMessage());
				logger.throwing("TaskExecutor", "executeTask", e);
				task.message = e.getMessage();
				task.setState(TaskState.ON_ERROR);
			}catch (AgentNotFoundException anfe) {
				logger.warning("Agent [" + taskInProgress.compute_agent_url + "] not found");
				logger.throwing("TaskExecutor", "executeTask", anfe);
				task.setState(TaskState.ON_ERROR);
				task.message = anfe.getMessage();
			}
		}
	}

	/**
	 * 
	 * @author Cyril Dumont
	 *
	 */
	private class CaseStateListener implements ComputationCaseListener{


		public void addCase(ComputationCase computationCase) throws MCASpaceException{
			logger.fine("Worker - CaseStateListener -- listen new case : " + computationCase);
			computationCase.join(this);
		}

		@Override
		public void caseStart() {
			taskExecutor = new TaskExecutor();
			taskExecutor.start();
		}

		@Override
		public void caseStop() {
			logger.fine("Worker -- receive STOP CASE signal");
			signalStop = true;
		}

		@Override
		public void caseFinish() {
			logger.fine("Worker -- receive FINISH CASE signal");
			signalFinish = true;
		}
	}
}
