package org.mca.worker;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Hashtable;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import net.jini.lookup.entry.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.agent.ComputeAgentInterface;
import org.mca.worker.exception.AgentNotFoundException;

/**
 * 
 * @author Cyril
 *
 */
public class AgentListener implements DiscoveryListener {

	private final static Integer SECOND_TO_WAIT = 3;
	
	/** Log */
	private final static Log LOG = LogFactory.getLog(AgentListener.class);

	private Boolean serviceFind;

	private ComputeAgentInterface agent;

	private ServiceTemplate template;

	private Hashtable<String, ComputeAgentInterface> agents;

	public AgentListener() {
		LOG.debug("AgentListener started ...");
		agents = new Hashtable<String, ComputeAgentInterface>();
	}

	/**
	 * 
	 * @param serviceID
	 * @return
	 */
	public ComputeAgentInterface getAgent(ServiceID serviceID){

		try{
			serviceFind = false;
			if (LOG.isDebugEnabled()) {
				LOG.debug("Search for the ComputingAgent [" + serviceID +"]");
			}
			template = new ServiceTemplate(serviceID, null, null);
			LookupDiscovery ld = new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
			ld.addDiscoveryListener(this);
			while(!serviceFind){
				Thread.sleep(1000);
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("ComputingAgent [" + serviceID +"] found.");
			}
			ld.removeDiscoveryListener(this);
			return agent;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param serviceID
	 * @return
	 */
	public ComputeAgentInterface getAgent(String name) throws AgentNotFoundException{

		try{
			agent = this.agents.get(name);
			if (agent != null) {
				return agent;
			}
			serviceFind = false;
			if (LOG.isDebugEnabled()) {
				LOG.debug("Search for the ComputingAgent [" + name +"]");	
			}
			Name entry = new Name(name);
			template = new ServiceTemplate(null, null, new Entry[]{entry});
			LookupDiscovery ld = new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
			ld.addDiscoveryListener(this);
			int seconds = 0;
			while(!serviceFind && seconds <= SECOND_TO_WAIT){
				Thread.sleep(1000);
				seconds++;
			}
			ld.removeDiscoveryListener(this);
			if (!serviceFind) {
				throw new AgentNotFoundException();
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("ComputingAgent [" + name +"] found.");			
				LOG.debug("Type : " + agent.getClass());
			}
			this.agents.put(name, this.agent);
			return agent;
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new AgentNotFoundException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new AgentNotFoundException();
		}
	}

	public void discarded(DiscoveryEvent event) {

	}

	public void discovered(DiscoveryEvent event) {
		try {

			ServiceRegistrar[] registrars = event.getRegistrars();
			for (ServiceRegistrar registrar : registrars) {
				ComputeAgentInterface agent = (ComputeAgentInterface)registrar.lookup(template);
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
