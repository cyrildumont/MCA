package org.mca.deployer;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.mca.agent.AgentDescriptor;
import org.mca.agent.ComputeAgentDeployer;
import org.mca.entry.Property;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.exceptions.CaseNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.Task;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;

public abstract class ComputationCaseDeployer {

	private static final String COMPONENT_NAME = "org.mca.deployer.ComputationCaseDeployer";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private static final String DEFAULT_HOST_SPACE = "localhost";

	private static final String DEFAULT_CONFIG_FILE = "mca.xml";

	private Map<String, String> properties;

	private MCASpace space;

	protected String projectName;

	private String description;

	protected ComputationCase computationCase;

	private String hostOfSpace;

	private List<AgentDescriptor> agents;

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public void setAgents(List<AgentDescriptor> agents) {
		this.agents = agents;
	}

	private void initSpace(String hostOfSpace){
		try {
			LookupLocator ll = new LookupLocator("jini://" + hostOfSpace);
			ServiceRegistrar registrar = ll.getRegistrar();
			Class<?>[] classes = new Class<?>[]{MCASpace.class};
			ServiceTemplate template = new ServiceTemplate(null, classes,null);
			space = (MCASpace)registrar.lookup(template);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@ManagedOperation
	public ComputationCase deploy(String hostOfSpace) throws MCASpaceException{
		logger.info(" *************** DEPLOY COMPUTATION CASE *********************");
		this.hostOfSpace = hostOfSpace;
		System.setSecurityManager(new RMISecurityManager());
		try {
			LoginContext loginContext = new LoginContext("org.mca.Master");
			loginContext.login();
			Subject.doAsPrivileged(loginContext.getSubject(), 	
					new PrivilegedExceptionAction() {
				@Override
				public Object run() throws Exception {
					deploy();
					return null;
				}
			}, null);
		} catch (LoginException e) {
			e.printStackTrace();
			throw new MCASpaceException();
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}

		return computationCase;
	}

	
	protected void init(){
		logger.fine("no initialization needed.");
	}
	
	private void deploy() throws MCASpaceException{
		init();
		initSpace(hostOfSpace);
		deployCase();
		try {
			deployProperties();
			deployAgents();
			deployData();
			deployTasks();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				space.removeCase(projectName);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
	}	



	private void deployAgents() {
		ComputeAgentDeployer deployer = new ComputeAgentDeployer();
		for (AgentDescriptor descriptor : agents) {
			changeCodebase(descriptor.getCodebaseFormate());
			deployer.deploy(descriptor);
			logger.info("[" + descriptor.getName() + "] Agent deployed.");
		}
	}

	protected abstract void deployTasks() throws MCASpaceException;
	protected abstract void deployData() throws MCASpaceException;

	protected void addTask(Task task) throws MCASpaceException {
		computationCase.addTask(task);
	}

	private void deployCase() throws MCASpaceException {
		try {
			computationCase = space.addCase(projectName, description);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}
	}

	/**
	 * 
	 */
	private void deployProperties() {
		logger.info(" *************** Deploy MCAProperties *********************");
		try {
			for (Map.Entry<String, String> property : properties.entrySet()) {
				String key = property.getKey();
				String value = property.getValue();
				computationCase.addProperty(new Property(key, value));
			}
		} catch (CaseNotFoundException e) {
			logger.warning(" Case [" + projectName + "] not found.");
		}catch (MCASpaceException e) {
			e.printStackTrace();
		} 
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}


	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("f", true, "descriptor file");
		options.addOption("h", true, "host of the MCASpace");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String configFile = cmd.getOptionValue("f");
		if (configFile == null)
			configFile = DEFAULT_CONFIG_FILE;


		String hostOfSpace = cmd.getOptionValue("h");
		if (hostOfSpace == null)
			hostOfSpace = DEFAULT_HOST_SPACE;

		ApplicationContext context = new  FileSystemXmlApplicationContext("file:" + configFile);
		ComputationCaseDeployer deployer = (ComputationCaseDeployer)context.getBean("deployer");
		try {
			deployer.deploy(hostOfSpace);
		} catch (MCASpaceException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
	
	private static void changeCodebase(String codebase){
		System.setProperty("java.rmi.server.codebase",codebase);
		System.setSecurityManager(new RMISecurityManager());
	}

}