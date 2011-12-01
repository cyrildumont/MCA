/**
 * 
 */
package org.mca.core;

import java.io.Serializable;
import java.rmi.RMISecurityManager;

import javax.management.NotificationBroadcasterSupport;
import javax.management.remote.JMXConnector;

import org.mca.mbeans.MBeanUtil;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.mca.util.MCAUtils;
import org.springframework.context.ApplicationContext;

/**
 * @author Cyril
 *
 */
public abstract class MCAComponent extends NotificationBroadcasterSupport implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final String SERVICE_CODEBASE = "codebase";
	private static final String SERVICE_REGGIE = "reggie";
	
	protected int type;
	
	protected String hostname;

	private boolean codebase;
	
	protected JMXConnector connector;
	
	public MCAComponent() {
		this.hostname = MCAUtils.getIP();
		this.connector = MBeanUtil.createJMXConnector();
	}
	
	public void setCodebase(boolean codebase) {
		this.codebase = codebase;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public String getHostname() {
		return hostname;
	}

	public JMXConnector getConnector() {
		return connector;
	}
	
	public ComponentInfo getComponentInto(){
		ComponentInfo infos = new ComponentInfo();
		infos.setConnector(connector);
		infos.setHostname(hostname);
		infos.setType(type);
		return infos;
	}
	
	/**
	 * 
	 * @param service
	 * @return
	 */
	protected Object startService(ServiceConfigurator config) {
		ServiceStarter starter = new ServiceStarter(config);
		Object service = starter.startWithoutAdvertise();
		return service;
	}
	
	void init(ApplicationContext context) throws Exception{
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());
		if(codebase){
			CodebaseServer codebaseServer = context.getBean(SERVICE_CODEBASE, CodebaseServer.class);
			codebaseServer.start();
		}
		ServiceConfigurator reggieConfig = context.getBean(SERVICE_REGGIE, ServiceConfigurator.class);
		startService(reggieConfig);
	}
	
	/**
	 * 
	 */
	public abstract void start() throws Exception;
	
}
