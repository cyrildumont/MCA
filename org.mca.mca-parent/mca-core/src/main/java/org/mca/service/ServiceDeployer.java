package org.mca.service;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.util.logging.Logger;

import net.jini.constraint.BasicMethodConstraints;
import net.jini.core.constraint.Integrity;
import net.jini.core.constraint.InvocationConstraint;
import net.jini.core.constraint.InvocationConstraints;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupLocatorDiscovery;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.ssl.SslServerEndpoint;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import net.jini.lookup.entry.Name;
import net.jini.security.AccessPermission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.AbstractComputeAgent;
import org.mca.agent.exception.DeployException;
import org.mca.log.LogUtil;
import org.mca.util.MCAUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ServiceDeployer implements ServiceIDListener {

	private static final String COMPONENT_NAME = "org.mca.core.deployer";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	// utile pour garder une reference sur l'object !
	private static Service agent;

	/**
	 * 
	 * @param file
	 */
	public void deploy(String file) throws DeployException{
		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + file);
		ServiceConfigurator serviceConfigurator = context.getBean("service",ServiceConfigurator.class);
		deploy(serviceConfigurator);
	}

	public void deploy(ServiceConfigurator config){
		deploy(config, null);
	}
	/**
	 * 
	 * @param config
	 */
	public void deploy(ServiceConfigurator config, Class<? extends AccessPermission> permissionClass){
		try{
			LookupLocator[] llc = config.getLookupLocators();
			Entry[] entries = config.getEntries();
			DiscoveryManagement dm = new LookupLocatorDiscovery(llc);
			String sClassAgent = config.getImplClass(); 
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<?> classAgent = loader.loadClass(sClassAgent);
			agent = (Service)classAgent.newInstance();
			Exporter exporter = 
				new BasicJeriExporter(SslServerEndpoint.getInstance(MCAUtils.getIP(),0), 
						new BasicILFactory(new BasicMethodConstraints(
								new InvocationConstraints(
										new InvocationConstraint[]{Integrity.YES}, null)),permissionClass));
			Remote proxy = exporter.export(agent);
			new JoinManager(proxy,entries,this,dm,new LeaseRenewalManager());
		}catch (ClassNotFoundException e) {
			logger.warning(e.getClass().getName() +" : " + e.getMessage());
			e.printStackTrace();
		} catch (InstantiationException e) {
			logger.warning(e.getClass().getName() +" : " + e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.warning(e.getClass().getName() +" : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.warning(e.getClass().getName() +" : " + e.getMessage());
			e.printStackTrace();
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
				Class<?> classAgent = Class.forName(sClassAgent);
				AbstractComputeAgent agent = (AbstractComputeAgent)classAgent.newInstance();
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
	public static void main(String[] args) throws Exception {

		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + args[0]);
		ServiceConfigurator serviceConfigurator = context.getBean("agent",ServiceConfigurator.class);

		RMISecurityManager securityManager = new RMISecurityManager();
		System.setProperty("java.rmi.server.codebase", serviceConfigurator.getCodebaseFormate());
		System.setProperty("java.security.policy", serviceConfigurator.getPolicy());
		System.setSecurityManager(securityManager);
		logger.info("Deploy " + serviceConfigurator.getName() + " Agent.");
		
		ServiceDeployer deployer = new ServiceDeployer();
		deployer.deploy(serviceConfigurator);
	}

	@Override
	public void serviceIDNotify(ServiceID serviceID) {
		LogUtil.debug("Agent [" + serviceID + "] deployed", getClass());
	}

}
