package org.mca.deployer;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.mca.agent.AgentDescriptor;
import org.mca.agent.ComputeAgent;
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

	@NotNull
	protected String projectName;

	@NotNull
	private String description;

	protected ComputationCase computationCase;

	protected ComputeAgentDeployer agentDeployer;

	public ComputationCaseDeployer() {
		agentDeployer = new ComputeAgentDeployer();
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
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
		deployAgents();
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


	protected abstract void deployAgents() throws MCASpaceException;
	protected abstract void deployTasks() throws MCASpaceException;
	protected abstract void deployData() throws MCASpaceException;

	protected void addTask(Task<?> task) throws MCASpaceException {
		computationCase.addTask(task);
	}

	protected void deployAgent(AgentDescriptor descriptor) throws MCASpaceException {
		agentDeployer.deploy(descriptor);
		logger.info("[" + descriptor.getName() + "] Agent deployed.");
	}
	
	protected void deployAgent(AgentDescriptor descriptor, ComputeAgent<?> agent) throws MCASpaceException {
		agentDeployer.deploy(descriptor, agent);
		logger.info("[" + descriptor.getName() + "] Agent deployed.");
	}
	
	protected void deployAgent(AgentDescriptor descriptor, Object[] agentParams) throws MCASpaceException {
		agentDeployer.deploy(descriptor, agentParams);
		logger.info("[" + descriptor.getName() + "] Agent deployed.");
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
		logger.info(" *************** Deploy Properties *********************");
		if (properties == null || properties.size() == 0) {
			logger.info("[" + projectName + "] -- No preperty to deploy" );
			return;
		}
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
							new PrivilegedExceptionAction<Object>() {
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
		ComputationCaseDeployer deployer = (ComputationCaseDeployer)context.getBean("deployer");
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<ComputationCaseDeployer>> constraintViolations = validator.validate(deployer);
		
		if (!constraintViolations.isEmpty()) {
			for (ConstraintViolation<ComputationCaseDeployer> cv : constraintViolations) {
				System.err.println("la propriété [" + cv.getPropertyPath() + "] " + cv.getMessage());
			}
			System.exit(-1);
		}
		try {
			deployer.deploy(hostOfSpace);
		} catch (MCASpaceException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
}
