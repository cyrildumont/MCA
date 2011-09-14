package org.mca.deployer;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
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
		init();
		initSpace(hostOfSpace);
		deployCase();
		try {
			
			deployProperties();
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
		return computationCase;
	}

	
	protected void init(){
		logger.fine("no initialization needed.");
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

		final String configFile = 
			cmd.getOptionValue("f") != null ? cmd.getOptionValue("f") : DEFAULT_CONFIG_FILE;


		final String hostOfSpace = 
			cmd.getOptionValue("h") != null ? cmd.getOptionValue("h") : DEFAULT_HOST_SPACE;
		
		try {
			LoginContext loginContext = new LoginContext("org.mca.User");
			loginContext.login();
			Subject.doAsPrivileged(loginContext.getSubject(), 	
					new PrivilegedExceptionAction() {
				@Override
				public Object run() throws Exception {
					deploy(configFile,hostOfSpace);
					return null;
				}
			}, null);
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
		}
		


	}
	
	private static void deploy(String configFile, String hostOfSpace) {
		System.setSecurityManager(new RMISecurityManager());
		ApplicationContext context = new  FileSystemXmlApplicationContext("file:" + configFile);
		
		Map<String, AgentDescriptor> agents = context.getBeansOfType(AgentDescriptor.class);
		deployAgents(agents.values());
		ComputationCaseDeployer deployer = (ComputationCaseDeployer)context.getBean("deployer");
		try {
			deployer.deploy(hostOfSpace);
		} catch (MCASpaceException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	private static void deployAgents(Collection<AgentDescriptor> descriptors) {
		ComputeAgentDeployer deployer = new ComputeAgentDeployer();
		for (AgentDescriptor descriptor : descriptors) {
			deployer.deploy(descriptor);
			logger.info("[" + descriptor.getName() + "] Agent deployed.");
		}
	}


}
