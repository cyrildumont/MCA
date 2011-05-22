package org.mca.agent;

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
import org.mca.log.LogUtil;
import org.mca.service.ServiceConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ComputeAgentDeployer {

	private static final String COMPONENT_NAME = "org.mca.core.deployer.agent";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	/**
	 * 
	 * @param file
	 */
	public void deploy(String file) throws DeployException{
		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + file);
		ServiceConfigurator serviceConfigurator = context.getBean("service",ServiceConfigurator.class);

		deploy(serviceConfigurator);


	}

	/**
	 * 
	 * @param config
	 */
	public void deploy(ServiceConfigurator config){
		try{
			LookupLocator[] llc = config.getLookupLocators();
			Entry[] entries = config.getEntries();
			String sClassAgent = config.getImplClass(); 
			Class<?> classAgent = Class.forName(sClassAgent);
			AbstractComputeAgent agent = (AbstractComputeAgent)classAgent.newInstance();
			if (agent instanceof NativeComputeAgent) {
				LogUtil.debug("Deploy ComputeNativeAgent ...", ComputeAgentDeployer.class);
				NativeComputeAgent nativeAgent = (NativeComputeAgent) agent;
				nativeAgent.setByteCodeHandler(config.getByteCodeHandler());
			}
			ServiceItem item = new ServiceItem(null, agent, entries);
			for (LookupLocator lookup : llc) {
				logger.info("deploy on [" + lookup.getHost() + ":" + lookup.getPort() + "]" );
				ServiceRegistrar registrar = lookup.getRegistrar();
				registrar.register(item, Long.MAX_VALUE);
			}
		
		}catch (Exception e) {
			logger.warning(e.getClass().getName() +" : " + e.getMessage());
			e.printStackTrace();
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
			loginContext = new LoginContext("org.mca.Browser");
			loginContext.login();
		} catch (LoginException e1) {
			e1.printStackTrace();
		}

		try {
			Subject.doAsPrivileged(
					loginContext.getSubject(),
					new PrivilegedExceptionAction(){
						public Object run() throws Exception {
							ApplicationContext context = new FileSystemXmlApplicationContext("file:" + file);
							ServiceConfigurator serviceConfigurator = context.getBean("agent",ServiceConfigurator.class);

							RMISecurityManager securityManager = new RMISecurityManager();
							System.setProperty("java.rmi.server.codebase", serviceConfigurator.getCodebaseFormate());
							System.setProperty("java.security.policy", serviceConfigurator.getPolicy());
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
