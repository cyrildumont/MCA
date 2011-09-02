package org.mca.server;

import java.rmi.RMISecurityManager;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.core.discovery.LookupLocator;

import org.mca.core.MCAComponent;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.mca.util.MCAUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Server extends MCAComponent {

	private static final long serialVersionUID = 1L;
	
	private static final String SERVICE_REGGIE = "reggie";
	private static final String SERVICE_TRANSACTION = "transaction";

	private static final String MCASPACE_AGENT = "MCASpace";

	private static final String FILE_SERVICES = System.getProperty("mca.home") + "/conf/services.xml";
	
	private ApplicationContext context;
	private LoginContext loginContext;
	
	public void start() throws Exception{
		loginContext = new LoginContext("org.mca.Server");
		loginContext.login();
		try {
			Subject.doAsPrivileged(
					loginContext.getSubject(),
					new PrivilegedExceptionAction(){
						public Object run() throws Exception {
							init();
							return null;
						}


					},
					null);
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	private void init() {
		 if (System.getSecurityManager() == null) {
	            System.setSecurityManager(new RMISecurityManager());
	        }

	        context = new FileSystemXmlApplicationContext("file:" + FILE_SERVICES);
	        startService(SERVICE_REGGIE);
	        try {
	        	LookupLocator locator = new LookupLocator(MCAUtils.getIP(),4160);
				startAllServices();
			} catch (Exception e) {
				e.printStackTrace();
			}	
	}
	
	protected void startAllServices() {
		startService(SERVICE_TRANSACTION);
		startService(MCASPACE_AGENT);
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
