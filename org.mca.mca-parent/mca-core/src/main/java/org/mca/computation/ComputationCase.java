package org.mca.computation;

import java.io.File;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.AgentDeployer;
import org.mca.computation.exception.NoProjectFoundException;
import org.mca.deployer.TaskDeployer;
import org.mca.entry.EntryGenerator;
import org.mca.files.FileGenerator;
import org.mca.files.FileGeneratorException;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.exceptions.CaseNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.javaspace.exceptions.NoJavaSpaceFoundException;
import org.mca.jmx.JMXComponent;
import org.mca.jmx.JMXConstantes;
import org.mca.log.LogUtil;
import org.mca.scheduler.Scheduler;
import org.mca.scheduler.SchedulerGenerator;
import org.mca.scheduler.xml.XMLScheduler;
import org.mca.service.ServiceConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;

public class ComputationCase {

	/** Log */
	private final static Log LOG = LogFactory.getLog(ComputationCase.class);

	private EntryGenerator entryGenerator;

	private FileGenerator fileGenerator;

	private SchedulerGenerator schedulerGenerator;

	private TaskDeployer taskDeployer;

	private Map<String, String> properties;

	private MCASpace space;

	private Scheduler scheduler;

	private ArrayList<String> agents;

	private String projectName;

	private String hostOfMaster;



	private String description;

	private org.mca.entry.ComputationCase computationCase;

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	private void initSpace(String hostOfSpace){
		try {
			space = new MCASpace(hostOfSpace);
		} catch (NoJavaSpaceFoundException e) {
			e.printStackTrace();
		}
	}

	@ManagedOperation
	public void prepare(){
		String mcaHome = System.getProperty("mca.home");
		if (mcaHome == "") {
			LOG.error("you must define a MCA HOME");
		}

		LOG.info(" *************** PREPARE COMPUTATION CASE *********************");
		LOG.info(" *************** Generate part files *********************");

		if (fileGenerator != null) {
			try {
				fileGenerator.setProperties(properties);	
				fileGenerator.generate();
			} catch (FileGeneratorException e) {
				e.printStackTrace();
				return;
			}

		}
		else{
			LOG.info("No FileGenerator");
		}
		LOG.info(" *************** Generate scheduler file *********************");
		if (schedulerGenerator != null) {
			schedulerGenerator.setDestFile(mcaHome + "/cases/" + projectName + "/work/scheduler.xml");
			schedulerGenerator.generate();
		}
		else{
			LOG.info("No SchedulerGenerator");
		}
	}

	@ManagedOperation
	public org.mca.entry.ComputationCase deploy(String hostOfSpace){

		LOG.info(" *************** DEPLOY COMPUTATION CASE *********************");
		initSpace(hostOfSpace);
		deployCase();
		deployProperties();
		generateEntries();
		//deployAgents();
		deployTasks();
		return computationCase;
	}


	private void deployTasks() {
		taskDeployer.setComputationCase(computationCase);
		taskDeployer.setProperties(properties);	
		try {
			taskDeployer.deploy();
		} catch (MCASpaceException e) {
			LOG.error("Error during task deploiement");
		}
	}

	private void deployCase() {
		try {
			computationCase = space.addCase(projectName,description);
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void deployAgents() {

		LOG.info(" *************** Deploy Agents *********************");
		String mcaHome = System.getProperty("mca.home");
		if (mcaHome == "") {
			LOG.error("you must define a MCA HOME");
		}
		String agentDir = mcaHome + "/cases/" + projectName + "/agents";
		File dir = new File(agentDir);
		String[] agents = dir.list();
		LOG.info(agents.length + " must be deployed.");
		for (String agent : agents) {
			deployAgent(agentDir + "/" + agent);
		}
	}

	/**
	 * 
	 * @param configFile
	 */
	private void deployAgent(String configFile){

		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + configFile);
		ServiceConfigurator serviceConfigurator = context.getBean("service",ServiceConfigurator.class);
		RMISecurityManager securityManager = new RMISecurityManager();
		System.setProperty("java.rmi.server.codebase", "http://localhost:6089/fft.jar http://localhost:6089/commons-math-2.1.jar ");
		System.setProperty("java.security.policy", "/home/cyril/MCA/conf/policy");
		System.setSecurityManager(securityManager);
		LOG.info("Deploy " + serviceConfigurator.getName() + " Agent.");
		AgentDeployer deployer = new AgentDeployer();
		deployer.deploy(serviceConfigurator);

	}


	/**
	 * 
	 */
	@ManagedOperation
	public void start(){
		try {
			JMXComponent component = new JMXComponent(JMXConstantes.JMX_MASTER_NAME, hostOfMaster, JMXConstantes.JMX_MASTER_PORT);

			String[] signature = new String[]{"java.lang.String"};
			Object[] params = new Object[]{projectName};
			component.invoke("start", params, signature);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void generateEntries() {
		LOG.info(" *************** Deploy DataHandler *********************");
		if (entryGenerator != null) {
			entryGenerator.setComputationCase(computationCase);
			//new Thread(entryGenerator).start();
			ExecutorService es = Executors.newFixedThreadPool(1);
			es.execute(entryGenerator);
			es.shutdown();
		}
		else{
			LOG.info("No EntryGenerator");
		}
	}

	/**
	 * 
	 */
	private void deployProperties() {
		LOG.info(" *************** Deploy MCAProperties *********************");
		try {
			org.mca.entry.ComputationCase computationCase = space.getCase(projectName);
			for (Map.Entry<String, String> property : properties.entrySet()) {
				String key = property.getKey();
				String value = property.getValue();
				computationCase.addProperty(key, value);
				LogUtil.debug("[" + space.getHost() +"][" + projectName + "]" +
						" Property [" + key + " = " + value +"] added.", getClass());
			}
		} catch (MCASpaceException e) {
			LogUtil.error(" Error on Space [" + space.getHost() + "].", getClass());
		} catch (CaseNotFoundException e) {
			LogUtil.error("[" + space.getHost() + "] Case [" + projectName + "] not found.", getClass());
		}
	}

	public void setEntryGenerator(EntryGenerator entryGenerator) {
		this.entryGenerator = entryGenerator;
	}


	public void setSpace(MCASpace space) {
		this.space = space;
	}

	public void setScheduler(XMLScheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void setFileGenerator(FileGenerator fileGenerator) {
		this.fileGenerator = fileGenerator;
	}

	public void setSchedulerGenerator(SchedulerGenerator schedulerGenerator) {
		this.schedulerGenerator = schedulerGenerator;
	}

	public void setAgents(ArrayList<String> agents) {
		this.agents = agents;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setTaskDeployer(TaskDeployer taskDeployer) {
		this.taskDeployer = taskDeployer;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setHostOfMaster(String hostOfMaster) {
		this.hostOfMaster = hostOfMaster;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 2){
			LOG.error("ComputationCase <ProjectName> <action> [options]");
			System.exit(-1);
		}

		String projectName = args[0];
		String action = args[1];
		ComputationCaseFactory factory = new ComputationCaseFactory();
		ComputationCase computationCase = null;
		try {
			computationCase = factory.getComputationCase(projectName);
			computationCase.setProjectName(projectName);
		} catch (NoProjectFoundException e) {
			LOG.error("The project " + projectName + " in not found.");
			throw new Error();
		}

		if (action.equals("deploy")) {
			String hostOfSpace = args[2];
			computationCase.deploy(hostOfSpace);
		}else if(action.equals("prepare")){
			computationCase.prepare();
		}else{
			LOG.error("command not found.");
			throw new Error();
		}

	}

}
