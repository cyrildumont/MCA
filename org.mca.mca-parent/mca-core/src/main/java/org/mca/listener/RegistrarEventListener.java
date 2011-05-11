package org.mca.listener;

import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceEvent;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.entry.Name;

import org.mca.log.LogUtil;
import org.mca.model.Lookup;
import org.mca.util.MCAUtils;

public abstract class RegistrarEventListener<T> extends Observable implements RemoteEventListener, Runnable {

	protected ServiceRegistrar registrar;
	protected Lookup lookup;
	protected Entry[] entries;
	protected Class<?>[] classes;

	protected final int transitions = 
		ServiceRegistrar.TRANSITION_MATCH_NOMATCH |
		ServiceRegistrar.TRANSITION_NOMATCH_MATCH | ServiceRegistrar.TRANSITION_MATCH_MATCH;

	
	/**
	 * 
	 * @param lookup
	 */
	public RegistrarEventListener(Lookup lookup,Class<?> clazz, String name){
		this(lookup, clazz, name, null);
	}

	/**
	 * 
	 * @param lookup
	 */
	public RegistrarEventListener(Lookup lookup,Class<?> clazz, String name, Observer observer){
		this(lookup.getServiceRegistrar(), clazz, name, observer);
	}

	/**
	 * 
	 * @param lookup
	 */
	public RegistrarEventListener(String host, Class<?> clazz, String name, Observer observer){

		try {
			LookupLocator ll = new LookupLocator("jini://" + host);
			this.registrar = ll.getRegistrar();
			if (name != null) entries = new Entry[]{new Name(name)};
			if (clazz != null) classes = new Class[]{clazz};
			if(observer != null) addObserver(observer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param lookup
	 */
	public RegistrarEventListener(ServiceRegistrar registrar,Class<?> clazz, String name, Observer observer){
		this.registrar = registrar;
		if (name != null) entries = new Entry[]{new Name(name)};
		if (clazz != null) classes = new Class[]{clazz};
		if(observer != null) addObserver(observer);
	}


	/**
	 * On ajoute un registrar à observer
	 * 
	 * @param registrar
	 * @throws RemoteException
	 */
	private void listen() {
		RemoteEventListener proxy;
		Exporter exporter = 
			new BasicJeriExporter(TcpServerEndpoint.getInstance(MCAUtils.getIP(),0),new BasicILFactory());
		try {
			proxy = (RemoteEventListener) exporter.export(this);
			ServiceTemplate templ = new ServiceTemplate(null, classes, entries);
			EventRegistration reg = 
				registrar.notify(templ,	transitions, proxy, null, Lease.FOREVER);
			LogUtil.debug("Enregistrement de l'EventRegistration avec l'ID " + reg.getID(), getClass());
		} catch(RemoteException e) {
			LogUtil.error("Probleme durant l'ajout du registrar à observer", getClass());
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * 
	 * Methode appelée lorsqu'un service apparait sur le lookup
	 */
	public void notify(RemoteEvent evt) throws RemoteException, UnknownEventException {
		try {
			ServiceEvent sevt = (ServiceEvent) evt;
			ServiceID uuid = sevt.getServiceID();
			int transition = sevt.getTransition();
			ServiceItem item = sevt.getServiceItem();
			T o = null;
			RegistrarEventType type = null;
			switch (transition) {
			case ServiceRegistrar.TRANSITION_NOMATCH_MATCH:
				LogUtil.debug("Un service est apparu sur le lookup observe ...",getClass());
				o = register(uuid, item);
				type = RegistrarEventType.NEW;
				break;
			case ServiceRegistrar.TRANSITION_MATCH_MATCH:
				LogUtil.debug("TRANSITION_MATCH_MATCH",getClass());
				o =  update(uuid, item);
				type = RegistrarEventType.UPDATE;
				break;
			case ServiceRegistrar.TRANSITION_MATCH_NOMATCH:
				LogUtil.debug("Un service a disparu sur le lookup observe ... ", getClass());
				o = unregister(uuid);
				type = RegistrarEventType.REMOVE;
				break;
			}
			setChanged();
			RegistrarEvent<T> event = new RegistrarEvent<T>(type,o);
			notifyObservers(event);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	protected abstract T unregister(ServiceID uuid);

	protected abstract T update(ServiceID uuid, ServiceItem item);

	protected abstract T register(ServiceID uuid, ServiceItem item);

	/**
	 * 
	 */
	public void run() {
		listen();
		try {
			while(true){
				Thread.sleep(Long.MAX_VALUE);
			}
		} catch(java.lang.InterruptedException e) {
			LogUtil.error("fin du thread", getClass());
		}
	}
}
