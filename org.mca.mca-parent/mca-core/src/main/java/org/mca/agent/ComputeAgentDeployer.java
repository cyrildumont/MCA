package org.mca.agent;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.rmi.RMISecurityManager;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;

import org.mca.agent.exception.DeployException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ComputeAgentDeployer implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.core.deployer.agent";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	/**
	 * 
	 * @param file
	 */
	public void deploy(String file) throws DeployException{
		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + file);
		AgentDescriptor serviceConfigurator = context.getBean("service",AgentDescriptor.class);
		deploy(serviceConfigurator);
	}

	/**
	 * 
	 * @param config
	 */
	public void deploy(AgentDescriptor config) throws DeployException{
		try{
			String sClassAgent = config.getImplClass(); 
			Class<?> classAgent = Class.forName(sClassAgent);
			ComputeAgent<?> agent = (ComputeAgent<?>)classAgent.newInstance();
			deploy(config, agent);
		}catch (Exception e) {
			logger.warning(e.getClass().getName() +" : " + e.getMessage());
			e.printStackTrace();
			throw new DeployException();
		} 
	}

	/**
	 * 
	 * @param descriptor
	 * @param agentParams
	 */
	public void deploy(AgentDescriptor descriptor, Object[] agentParams) throws DeployException{
		try{
			String sClassAgent = descriptor.getImplClass(); 
			Class<?> classAgent = Class.forName(sClassAgent);
			Class<?>[] paramsClass = new Class<?>[agentParams.length];
			for (int i = 0; i < agentParams.length; i++) paramsClass[i] = agentParams[i].getClass();
			Constructor<?> constructor = classAgent.getConstructor(paramsClass); 
			ComputeAgent<?> agent = (ComputeAgent<?>)constructor.newInstance(agentParams);
			deploy(descriptor, agent);
		}catch (Exception e) {
			logger.warning(e.getClass().getName() +" : " + e.getMessage());
			e.printStackTrace();
			throw new DeployException();
		} 
	}
	
	/**
	 * 
	 * @param config
	 * @param agent
	 */
	public void deploy(AgentDescriptor config, ComputeAgent<?> agent) throws DeployException{
		try{
			System.setProperty("java.rmi.server.codebase",config.getCodebaseFormate());
			LookupLocator lookup = config.getLookupLocator();
			Entry[] entries = config.getEntries();
			if (agent instanceof NativeComputeAgent) {
				logger.info("Deploy ComputeNativeAgent ...");
				NativeComputeAgent nativeAgent = (NativeComputeAgent) agent;
				nativeAgent.setByteCode(config.getByteCode());
			}
			ServiceItem item = new ServiceItem(null, agent, entries);
			logger.info("deploy on [" + lookup.getHost() + ":" + lookup.getPort() + "]" );
			ServiceRegistrar registrar = lookup.getRegistrar();
			registrar.register(item, Long.MAX_VALUE);

		}catch (Exception e) {
			logger.warning(e.getClass().getName() +" : " + e.getMessage());
			e.printStackTrace();
			throw new DeployException();
		} 
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		final String file = args[0];
		LoginContext loginContext = null;
		try {
			loginContext = new LoginContext("org.mca.Server");
			loginContext.login();
		} catch (LoginException e1) {
			e1.printStackTrace();
		}

		try {
			Subject.doAsPrivileged(
					loginContext.getSubject(),
					new PrivilegedExceptionAction<Object>(){
						public Object run() throws Exception {
							ApplicationContext context = new FileSystemXmlApplicationContext("file:" + file);
							AgentDescriptor serviceConfigurator = context.getBean("agent",AgentDescriptor.class);

							RMISecurityManager securityManager = new RMISecurityManager();
							System.setProperty("java.rmi.server.codebase", serviceConfigurator.getCodebaseFormate());
							System.setSecurityManager(securityManager);
							logger.info("Deploy " + serviceConfigurator.getName() + " Agent.");

							ComputeAgentDeployer deployer = new ComputeAgentDeployer();
							deployer.deploy(serviceConfigurator);
							logger.info(serviceConfigurator.getName() + " Agent deployed.");
							return null;
						}
					},
					null);
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
		}

	}

}
