package org.mca.listener;

import java.util.Observer;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;

import org.mca.agent.TaskNotifierAgent;
import org.mca.log.LogUtil;
import org.mca.model.Lookup;

public class TaskNotifierAgentListener extends RegistrarEventListener {

	/**
	 * 
	 * @param lookup
	 * @param observer
	 */
	public TaskNotifierAgentListener(Lookup lookup, Observer observer) {
		super(lookup,TaskNotifierAgent.class, null, observer);
	}

	/**
	 * 
	 */
	@Override
	protected Object register(ServiceID uuid, ServiceItem item) {
		Object service = item.service;
		if (service instanceof TaskNotifierAgent) {
			TaskNotifierAgent notifierAgent = (TaskNotifierAgent)service;
			return notifierAgent;
		}else{
			LogUtil.info("Type of agent unknown : [" + service.getClass().getName() +"]", getClass());
			return null;
		}
	}

	@Override
	protected Object unregister(ServiceID uuid) {
		return null;
	}

	@Override
	protected Object update(ServiceID uuid, ServiceItem item) {
		return null;
	}

}
