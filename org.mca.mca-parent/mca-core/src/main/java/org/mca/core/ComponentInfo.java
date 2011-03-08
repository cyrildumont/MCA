package org.mca.core;

import java.io.Serializable;

import javax.management.remote.JMXConnector;

public class ComponentInfo implements Serializable {
	
	protected int type;
	
	protected String hostname;
	
	protected JMXConnector connector;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public JMXConnector getConnector() {
		return connector;
	}

	public void setConnector(JMXConnector connector) {
		this.connector = connector;
	}	
	
}
