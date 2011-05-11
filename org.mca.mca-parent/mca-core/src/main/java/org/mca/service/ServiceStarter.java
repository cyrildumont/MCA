/**
 * 
 */
package org.mca.service;

import java.rmi.RMISecurityManager;
import java.rmi.Remote;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.discovery.DiscoveryGroupManagement;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.sun.jini.start.NonActivatableServiceDescriptor;
import com.sun.jini.start.ServiceDescriptor;
import com.sun.jini.start.NonActivatableServiceDescriptor.Created;

/**
 * @author Cyril
 *
 */
public class ServiceStarter implements ServiceIDListener{

	/** Log */
	private final static Log LOG = LogFactory.getLog(ServiceStarter.class);


	private ServiceConfigurator config;
	private Object impl;
	private Remote proxy;


	private boolean needProxy;


	/**
	 * Constaructeur en parametre le fichier de config
	 * 
	 * @param args
	 */
	public ServiceStarter(ServiceConfigurator config) {
		this.config = config;
	}

	public Object startWithoutAdvertise(){
		needProxy = false;
		Object impl = startService();
		return impl;
	}

	public void start(){
		needProxy = true;
		startService();
		advertiseService();
	}

	private Object startService() {
		
		//System.setProperty("java.rmi.server.codebase", "https://localhost/mca/codebase/reggie-dl.jar https://localhost/mca/codebase/jsk-dl.jar https://localhost/mca/codebase/mahalo-dl.jar https://localhost/mca/codebase/mca-server.jar");
		String codebase = config.getCodebaseFormate();
		String policy = config.getPolicy();
		String classpath = config.getClasspathFormate();
		String implClass = config.getImplClass();
		String[] serverConfigArgs = config.getServerConfigArgs();
		// Create the new service descriptor
		ServiceDescriptor desc = 
			new NonActivatableServiceDescriptor(codebase,
					policy,
					classpath,
					implClass,
					serverConfigArgs);
		
		// and create the service and its proxy
		Created created = null;
		try {
			created = (Created) desc.create(config);
		} catch(Exception e) {
			LOG.error(e.getClass() + " : " + e.getMessage());
			System.exit(1);
		}
		impl = created.impl;
		if (needProxy) 
			proxy = (Remote) created.proxy;
		return impl;

	}	

	private void advertiseService() {
		Entry[] entries = null;
		LookupLocator[] unicastLocators = null;
		String[] groups = null;

		try {
			unicastLocators = config.getLookupLocators();
			entries = config.getEntries();
			groups = new String[]{"coucou"};
		} catch(Exception e) {
			System.err.println(e.getClass().getName() + " : " + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
		JoinManager joinMgr = null;
		try {
			LookupDiscoveryManager mgr = 
				new LookupDiscoveryManager(groups, unicastLocators, null);
			joinMgr = new JoinManager(proxy, entries, this, mgr, new LeaseRenewalManager());

		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * 
	 */
	public void serviceIDNotify(ServiceID serviceID) {
		System.out.println(serviceID);
	}

	public static void main(String[] args) {

		
		String file = args[0];
		String service = args[1];
		
		System.out.println("Lancement du service [" + service + "] à partir du fichier [" + file + "]");
		
		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + file);
		ServiceConfigurator serviceConfigurator = context.getBean(service,ServiceConfigurator.class);

		RMISecurityManager securityManager = new RMISecurityManager();
		System.setProperty("java.rmi.server.codebase", serviceConfigurator.getCodebaseFormate());
		System.setProperty("java.security.policy", serviceConfigurator.getPolicy());


		System.setSecurityManager(securityManager);
		ServiceStarter starter = new ServiceStarter(serviceConfigurator);
		starter.startWithoutAdvertise();
		while( true ) {
            try {
                Thread.sleep( 100000 );
            } catch( InterruptedException ex ) {
            }
        }		
	}
}
