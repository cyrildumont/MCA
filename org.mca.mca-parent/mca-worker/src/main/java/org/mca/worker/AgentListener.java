package org.mca.worker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.logging.Logger;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;

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
		try {
			LookupLocator ll = new LookupLocator(host);
			ServiceRegistrar registrar = ll.getRegistrar();
			ComputeAgent item = (ComputeAgent)registrar.lookup(template);
			if(item == null) throw new AgentNotFoundException();
			return item;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new AgentNotFoundException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new AgentNotFoundException();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new AgentNotFoundException();
		} 

	}

}
