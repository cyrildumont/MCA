package org.mca.agent;

import java.io.IOException;
import java.rmi.RMISecurityManager;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupLocatorDiscovery;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.exception.DeployException;
import org.mca.log.LogUtil;
import org.mca.service.ServiceDeployer;
import org.mca.service.ServiceConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ComputeAgentDeployer extends ServiceDeployer{

	/** Log */
	private final static Log LOG = LogFactory.getLog(ComputeAgentDeployer.class);


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
			DiscoveryManagement dm = new LookupLocatorDiscovery(llc);
			String sClassAgent = config.getImplClass(); 
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<?> classAgent = loader.loadClass(sClassAgent);
			AbstractComputeAgent agent = (AbstractComputeAgent)classAgent.newInstance();
			if (agent instanceof NativeComputeAgent) {
				LogUtil.debug("Deploy ComputeNativeAgent ...", ComputeAgentDeployer.class);
				NativeComputeAgent nativeAgent = (NativeComputeAgent) agent;
				nativeAgent.setByteCodeHandler(config.getByteCodeHandler());
			}
			JoinManager myManager = new JoinManager(agent,entries,agent,dm,new LeaseRenewalManager());
		}catch (ClassNotFoundException e) {
			LOG.error(e.getClass().getName() +" : " + e.getMessage());
		} catch (InstantiationException e) {
			LOG.error(e.getClass().getName() +" : " + e.getMessage());
		} catch (IllegalAccessException e) {
			LOG.error(e.getClass().getName() +" : " + e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getClass().getName() +" : " + e.getMessage());
		}
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + args[0]);
		ServiceConfigurator serviceConfigurator = context.getBean("agent",ServiceConfigurator.class);

		RMISecurityManager securityManager = new RMISecurityManager();
		System.setProperty("java.rmi.server.codebase", serviceConfigurator.getCodebaseFormate());
		System.setProperty("java.security.policy", serviceConfigurator.getPolicy());
		System.setSecurityManager(securityManager);
		LOG.info("Deploy " + serviceConfigurator.getName() + " Agent.");
		
		ComputeAgentDeployer deployer = new ComputeAgentDeployer();
		deployer.deploy(serviceConfigurator);
	}

}
