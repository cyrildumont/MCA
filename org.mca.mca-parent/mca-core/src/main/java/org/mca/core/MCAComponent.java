/**
 * 
 */
package org.mca.core;

import java.io.Serializable;

import javax.management.NotificationBroadcasterSupport;
import javax.management.remote.JMXConnector;

import org.mca.mbeans.MBeanUtil;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.mca.util.MCAUtils;

/**
 * @author Cyril
 *
 */
@SuppressWarnings("serial")
public abstract class MCAComponent extends NotificationBroadcasterSupport implements Serializable{

	protected int type;
	
	protected String hostname;
	
	protected JMXConnector connector;
	
	public MCAComponent() {
		this.hostname = MCAUtils.getIP();
		this.connector = MBeanUtil.createJMXConnector();
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
	
}
