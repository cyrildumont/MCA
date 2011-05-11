package org.mca.server;

import java.rmi.RMISecurityManager;

import org.mca.core.MCAComponent;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Server extends MCAComponent {

	private static final String SERVICE_REGGIE = "reggie";
	private static final String SERVICE_TRANSACTION = "transaction";

	private static final String MCASPACE_AGENT = "MCASpace";

	private static final String FILE_SERVICES = System.getProperty("mca.home") + "/conf/services.xml";

	private ApplicationContext context;

	public void start() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
		startAllServices();


	}

	protected void startAllServices() {
		context = new FileSystemXmlApplicationContext("file:" + FILE_SERVICES);
		Object registrar = startService(SERVICE_REGGIE);
		startService(SERVICE_TRANSACTION);
		startService(MCASPACE_AGENT);
		//		ServiceConfigurator agentConfig = context.getBean(MCASPACE_AGENT, ServiceConfigurator.class);
		//		ServiceDeployer agentDeployer = new ServiceDeployer();
		//		agentDeployer.deploy(agentConfig,MCASpacePermission.class);

		//Lookup lookup = new Lookup(registrar);

		while( true ) {
			try {
				Thread.sleep( 100000 );
			} catch( InterruptedException ex ) {
			}
		}
	}

	/**
	 * 
	 * @param service
	 * @return
	 */
	private Object startService(String config) {
		ServiceConfigurator reggieConfig = context.getBean(config, ServiceConfigurator.class);
		ServiceStarter starter = new ServiceStarter(reggieConfig);
		Object service = starter.startWithoutAdvertise();
		return service;
	}

}
