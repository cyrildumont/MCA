package org.mca.agent;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.lookup.entry.Name;

import org.mca.log.LogUtil;
import org.mca.scheduler.Task;


public class TaskNotifierAgentImpl extends UnicastRemoteObject implements TaskNotifierAgent, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -397251081010622364L;


	private static final String TASK_NOTIFIER_AGENT_NAME = "TaskNotifierAgent";

	private List<String> road;

	private Entry[] entries;

	private Task task;
	private ServiceItem item;

	/**
	 * 
	 * @param road
	 * @throws RemoteException
	 */
	public TaskNotifierAgentImpl(List<String> road) throws RemoteException {
		this.road = road;
		generateEntries();
	}

	/**
	 * 
	 */
	private void generateEntries() {
		entries = new Entry[1];
		entries[0] = new Name(TASK_NOTIFIER_AGENT_NAME);

	}

	/**
	 * 
	 */
	public Task getTask() throws RemoteException {
		return this.task;
	}

	/**
	 * 
	 */
	public void next() throws RemoteException{
		if (road.isEmpty()) {
			LogUtil.info("No more lookup on the road.", getClass());
		}else{
			String nextLookup = nextLookup();
			register(nextLookup);			
		}
	}

	/**
	 * 
	 * @return
	 */
	private String nextLookup(){
		int randomIndex = (int) (Math.random() * road.size());
		return road.get(randomIndex);
	}

	/**
	 * 
	 * @param nextLookup
	 */
	private void register(String nextLookup){
		try {
			road.remove(nextLookup);
			LookupLocator locator = new LookupLocator("jini://" + nextLookup);
			ServiceRegistrar registrar = locator.getRegistrar();
			ServiceRegistration registration = registrar.register(this.item, Long.MAX_VALUE);
			LogUtil.debug("registration on " + nextLookup + " OK", getClass());
		}catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.error("erreur de connection", getClass());
			try {
				next();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			} 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public void start(){
		try {
			Uuid uuid = UuidFactory.generate();
			ServiceID serviceID = new ServiceID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
			this.item = new ServiceItem(serviceID, this, entries);
			next();
		}catch (RemoteException e) {
			e.printStackTrace();
		} 
	}

	public void setTask(Task task) {
		this.task = task;
	}

}
