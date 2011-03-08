package org.mca.server;

import org.mca.model.Lookup;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Server extends Thread {
	
	private static final String SERVICE_REGGIE = "reggie";
	private static final String SERVICE_JAVASPACE = "javaspace";
	private static final String SERVICE_TRANSACTION = "transaction";

	private static final String FILE_SERVICES = System.getProperty("mca.home") + "/conf/services.xml";
	
	private ApplicationContext context;
	
	public void start() {
		
			context = new FileSystemXmlApplicationContext("file:" + FILE_SERVICES);
			CodebaseServer codebaseServer = context.getBean("codebase", CodebaseServer.class);
			codebaseServer.start();
			Object registrar = startService(SERVICE_REGGIE);
			startService(SERVICE_JAVASPACE);
			startService(SERVICE_TRANSACTION);
			
			Lookup lookup = new Lookup(registrar);
			
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
