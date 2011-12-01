package org.mca.server;

import org.mca.core.MCAComponent;
import org.mca.service.ServiceConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * @author Cyril Dumont
 *
 */
public class Server extends MCAComponent {

	private static final long serialVersionUID = 1L;

	private static final String SERVICE_TRANSACTION = "transaction";

	private static final String MCASPACE_AGENT = "MCASpace";

	private static final String FILE_SERVICES = System.getProperty("mca.home") + "/conf/services.xml";

	private ApplicationContext context;

	public void start() throws Exception{
		context = new FileSystemXmlApplicationContext("file:" + FILE_SERVICES);
		startAllServices();
	}

	protected void startAllServices() {
		ServiceConfigurator config = context.getBean(SERVICE_TRANSACTION, ServiceConfigurator.class);
		startService(config);
		config = context.getBean(MCASPACE_AGENT, ServiceConfigurator.class);
		startService(config);
	}

}
