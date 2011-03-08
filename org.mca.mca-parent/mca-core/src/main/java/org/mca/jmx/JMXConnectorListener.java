package org.mca.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;

import javax.management.remote.JMXConnector;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceEvent;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JMXConnectorListener extends Observable implements RemoteEventListener,Runnable {

	/** Log */
	private final static Log LOG = LogFactory.getLog(JMXConnectorListener.class);

	protected ServiceRegistrar registrar;

	protected final int transitions = ServiceRegistrar.TRANSITION_MATCH_NOMATCH |
	ServiceRegistrar.TRANSITION_NOMATCH_MATCH |
	ServiceRegistrar.TRANSITION_MATCH_MATCH;

	private final Class[] classes = new Class[] {JMXConnector.class};
	ServiceTemplate template = new ServiceTemplate(null, classes, null);

	public JMXConnectorListener(Observer observer) {
		addObserver(observer);
	}
	/**
	 * 
	 */
	public void run() {
		try {
			ServiceRegistrar registrar = locateLookup();
			ServiceMatches matches = registrar.lookup(template, Integer.MAX_VALUE);
			for (int i = 0; i < matches.totalMatches; i++) {
				if (matches.items[i].service != null) {
					JMXConnector connector = (JMXConnector)(matches.items[i].service);
					connectorFound(connector);
				}
			}
			addRegistrar(registrar);

			while(true){
				Thread.sleep(Long.MAX_VALUE);
			}
		} catch(java.lang.InterruptedException e) {
			LOG.error("fin du thread");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void connectorFound(JMXConnector connector) {
		setChanged();
		notifyObservers(connector);
		LOG.debug("Found a JMXConnector: " + connector);
	}

	/**
	 * 
	 * @return
	 */
	private ServiceRegistrar locateLookup() {
		try {
			LookupLocator ll = new LookupLocator("jini://localhost");
			ServiceRegistrar registrar = ll.getRegistrar();
			LOG.debug("Registrar found : " + registrar);
			return registrar;
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			return locateLookup();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	/**
	 * On ajoute un registrar à observer
	 * 
	 * @param registrar
	 * @throws RemoteException
	 */
	public void addRegistrar(ServiceRegistrar registrar) {


		RemoteEventListener proxy;
		this.registrar = registrar;
		Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),new BasicILFactory());

		try {

			// export an object of this class

			proxy = (RemoteEventListener) exporter.export(this);
			EventRegistration reg = null;

			reg = registrar.notify(template,
					transitions,
					proxy,
					null,
					Lease.FOREVER);
			LOG.debug("Enregistrement de l'EventRegistration avec l'ID " + reg.getID());
		} catch(RemoteException e) {
			LOG.error("Probleme durant l'ajout du registrar à observer");
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * 
	 */
	public void notify(RemoteEvent evt) throws RemoteException, UnknownEventException {
		try {
			ServiceEvent sevt = (ServiceEvent) evt;
			int transition = sevt.getTransition();
			switch (transition) {
			case ServiceRegistrar.TRANSITION_NOMATCH_MATCH:
				LOG.info("Un JMXConnector est apparu sur le lookup observe ...");
				JMXConnector connector = (JMXConnector)sevt.getServiceItem().service;
				connectorFound(connector);
				break;
			case ServiceRegistrar.TRANSITION_MATCH_MATCH:
				break;
			case ServiceRegistrar.TRANSITION_MATCH_NOMATCH:
				LOG.info("Un JMXConnector a disparu sur le lookup observe ...");
				break;
			}


		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
