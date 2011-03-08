package org.mca.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.swing.event.EventListenerList;

import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jini.start.LifeCycle;
import com.sun.jini.start.ServiceProxyAccessor;

public abstract class JiniService implements ServiceProxyAccessor, Remote{

	private final static Log LOG = LogFactory.getLog(JiniService.class);
	
	Remote proxy;

    /**
     * Listeners for change events
     */
    protected EventListenerList listenerList = new EventListenerList();

    protected long seqNum = 0L;
    
    /**
     * 
     * @param configArgs
     * @param lifeCycle
     * @throws RemoteException
     */
	public JiniService(String[] configArgs,LifeCycle lifeCycle) throws RemoteException {

			Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                    new BasicILFactory()); 
			proxy = exporter.export(this);
	}

	/**
	 * 
	 * @param listener
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	public EventRegistration addRemoteListener(RemoteEventListener listener)
		throws java.rmi.RemoteException {
		listenerList.add(RemoteEventListener.class, listener);
		if (LOG.isDebugEnabled()) {
			LOG.debug("add a RemoteEventLister : " + listener);
		}
		return new EventRegistration(0, proxy, null, 0);
	}

	/**
	 * 
	 * @param eventID
	 */
	protected void fireNotify(long eventID) {
		RemoteEvent remoteEvent = null;

		
		Object[] listeners = listenerList.getListenerList();


		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == RemoteEventListener.class) {
				RemoteEventListener listener = (RemoteEventListener) listeners[i+1];
				if (remoteEvent == null) {
					remoteEvent = new RemoteEvent(proxy, eventID, 
							seqNum++, null);
				}
				try {
					listener.notify(remoteEvent);
				} catch(UnknownEventException e) {
					e.printStackTrace();
				} catch(RemoteException e) {
					// Remove this listener from the list due to failure
					listenerList.remove(RemoteEventListener.class, listener);
					LOG.error("notification failed, listener removed");
				}
			}
		}
	}    

	/**
	 * 
	 */
	public Object getServiceProxy() throws RemoteException {
		
		return proxy;
	}


}
