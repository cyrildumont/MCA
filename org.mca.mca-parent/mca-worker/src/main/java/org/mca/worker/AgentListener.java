package org.mca.worker;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.logging.Logger;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupLocatorDiscovery;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.entry.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.ComputeAgent;
import org.mca.worker.exception.AgentNotFoundException;

/**
 * 
 * @author Cyril
 *
 */
public class AgentListener  {

	private static final String COMPONENT_NAME = "org.mca.worker.AgentListener";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private final static Integer SECOND_TO_WAIT = 3;

	private Boolean serviceFind;

	private ComputeAgent agent;

	private ServiceTemplate template;

	private Hashtable<String, ComputeAgent> agents;

	public AgentListener() {
		logger.finest("Worker -- AgentListener started");
		agents = new Hashtable<String, ComputeAgent>();
	}

	
	/**
	 * 
	 * @param serviceID
	 * @return
	 */
	public ComputeAgent getAgent(String url) throws AgentNotFoundException{

		int i = url.lastIndexOf("/");
		String host = url.substring(0, i);
		String name = url.substring(i+1);

		agent = this.agents.get(name);
		if (agent != null) {
			return agent;
		}
		serviceFind = false;
		logger.finest("Search for the ComputingAgent [" + name +"] on [" + host + "]");	
		Name entry = new Name(name);
		template = new ServiceTemplate(null, null, new Entry[]{entry});
		return null;

	}

	public void discarded(DiscoveryEvent event) {

	}

	public void discovered(DiscoveryEvent event) {
		try {

			ServiceRegistrar[] registrars = event.getRegistrars();
			for (ServiceRegistrar registrar : registrars) {
				ComputeAgent agent = (ComputeAgent)registrar.lookup(template);
				if(agent != null){
					this.agent = agent;
					serviceFind = true;
					break;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

}
