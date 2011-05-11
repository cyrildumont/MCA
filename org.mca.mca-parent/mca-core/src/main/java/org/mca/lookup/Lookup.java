package org.mca.lookup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Lookup implements DiscoveryListener {

	/** Log */
	private final static Log LOG = LogFactory.getLog(Lookup.class);

	private ServiceTemplate theTemplate;
	private LookupDiscovery theDiscoverer;

	private Object theProxy;

	/**
	 * 
	 * @param aServiceInterface
	 */
	public Lookup(Class<?> aServiceInterface) {
		Class<?>[] myServiceTypes = new Class[] {aServiceInterface};
		theTemplate = new ServiceTemplate(null, myServiceTypes, null);
	}


	public Object getService(String host){
		return getService(host, null);
	}
	
	public Object getService(String host, Entry[] entries){
		try{
			LookupLocator locators = new LookupLocator("jini://" + host);	
			ServiceRegistrar registrar = locators.getRegistrar();	
			theTemplate.attributeSetTemplates = entries;
			return registrar.lookup(theTemplate);
		}catch (MalformedURLException e) {
			LOG.error(e.getClass().getName() + " : " + e.getMessage());
			return null;
		} catch (IOException e) {
			LOG.error(e.getClass().getName() + " : " + e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			LOG.error(e.getClass().getName() + " : " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Object getService() {
		synchronized(this) {
			if (theDiscoverer == null) {

				try {
					theDiscoverer =
						new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
					theDiscoverer.addDiscoveryListener(this);
				} catch (IOException anIOE) {
					System.err.println("Failed to init lookup");
					anIOE.printStackTrace(System.err);
				}
			}
		}

		return waitForProxy();
	}

	/**
	 * 
	 * @return
	 */
	private Object waitForProxy() {
		synchronized(this) {
			while (theProxy == null) {
				try {
					wait();
				} catch (InterruptedException anIE) {
				}
			}
			return theProxy;
		}
	}

	/**
	 * 
	 *
	 */
	void terminate() {
		synchronized(this) {
			if (theDiscoverer != null)
				theDiscoverer.terminate();
		}
	}


	/**
	 * 
	 * @param aProxy
	 */
	private void signalGotProxy(Object aProxy) {
		synchronized(this) {
			if (theProxy == null) {
				theProxy = aProxy;
				notify();
			}
		}
	}


	/**
	 * 
	 */
	public void discovered(DiscoveryEvent anEvent) {

		synchronized(this) {
			if (theProxy != null)
				return;
		}

		ServiceRegistrar[] myRegs = anEvent.getRegistrars();

		for (int i = 0; i < myRegs.length; i++) {
			ServiceRegistrar myReg = myRegs[i];

			Object myProxy = null;

			try {
				myProxy = myReg.lookup(theTemplate);

				if (myProxy != null) {
					signalGotProxy(myProxy);
					break;
				}

			} catch (RemoteException anRE) {
				System.err.println("ServiceRegistrar barfed");
				anRE.printStackTrace(System.err);
			}
		}
	}


	/**
	 * 
	 */
	public void discarded(DiscoveryEvent anEvent) {
	}
}

