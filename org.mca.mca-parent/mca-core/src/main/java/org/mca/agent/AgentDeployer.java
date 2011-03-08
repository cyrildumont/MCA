package org.mca.agent;

import java.io.IOException;
import java.rmi.RMISecurityManager;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupLocatorDiscovery;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.entry.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.exception.DeployException;
import org.mca.log.LogUtil;
import org.mca.service.ServiceConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class AgentDeployer {

	/** Log */
	private final static Log LOG = LogFactory.getLog(AgentDeployer.class);


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
			Class classAgent = loader.loadClass(sClassAgent);
			ComputeAgent agent = (ComputeAgent)classAgent.newInstance();
			if (agent instanceof ComputeNativeAgent) {
				LogUtil.debug("Deploy ComputeNativeAgent ...", AgentDeployer.class);
				ComputeNativeAgent nativeAgent = (ComputeNativeAgent) agent;
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
	 * Undepoy a Service
	 * 
	 * @param config
	 */
	public static void undeploy(ServiceConfigurator config){
		LookupLocator[] llc = config.getLookupLocators();
		for (LookupLocator ll : llc) {
			try {
				ServiceRegistrar registrar = ll.getRegistrar();
				String name = config.getName();
				Entry[] entries = new Entry[]{new Name(name)};
				ServiceTemplate template = new ServiceTemplate(null, null, entries);
				ServiceMatches matches = registrar.lookup(template,10);
				ServiceItem[] items = matches.items;
				for (ServiceItem item : items) {
					registrar.register(item, 0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * Update a Service
	 * 
	 * @param config
	 */
	public static void update(ServiceConfigurator config){
		LookupLocator[] llc = config.getLookupLocators();
		for (LookupLocator ll : llc) {
			try {
				ServiceRegistrar registrar = ll.getRegistrar();
				String sClassAgent = config.getImplClass(); 
				Class classAgent = Class.forName(sClassAgent);
				ComputeAgent agent = (ComputeAgent)classAgent.newInstance();
				String name = config.getName();
				Entry[] entries = new Entry[]{new Name(name)};
				ServiceTemplate template = new ServiceTemplate(null, null, entries);
				ServiceMatches matches = registrar.lookup(template,10);
				ServiceItem[] items = matches.items;
				for (ServiceItem item : items) {
					item.service = agent;
					registrar.register(item, Lease.FOREVER);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + args[0]);
		ServiceConfigurator serviceConfigurator = context.getBean("service",ServiceConfigurator.class);

		RMISecurityManager securityManager = new RMISecurityManager();
		System.setProperty("java.rmi.server.codebase", serviceConfigurator.getCodebaseFormate());
		System.setProperty("java.security.policy", serviceConfigurator.getPolicy());
		System.setSecurityManager(securityManager);
		LOG.info("Deploy " + serviceConfigurator.getName() + " Agent.");

		AgentDeployer deployer = new AgentDeployer();
		deployer.deploy(serviceConfigurator);
	}

}
