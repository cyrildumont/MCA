package org.mca.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.swing.event.EventListenerList;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.TransactionException;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupLocatorDiscovery;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import net.jini.lookup.entry.ServiceInfo;

import org.mca.entry.ComputationCaseState;
import org.mca.entry.State;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.ComputationCaseInfo;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.MCASpaceEvent;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.jmx.JMXConstantes;
import org.mca.log.LogUtil;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.sun.jini.config.Config;
import com.sun.jini.lookup.entry.BasicServiceType;
import com.sun.jini.outrigger.MCAPersistentOutriggerImpl;
import com.sun.jini.start.LifeCycle;

public class MCASpaceServerImpl implements MCASpaceServer, ServiceIDListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3354819183243663600L;

	
	private static final String SERVICE_JAVASPACE = "javaspace";
	private static final String FILE_JAVASPACE = System.getProperty("mca.home") + "/conf/javaspace.xml";

	private Map<String, MCAPersistentOutriggerImpl> cases;

	public final static String COMPONENT_NAME = "org.mca.server";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	private final LoginContext loginContext;

	private Exporter exporter;

	private JoinManager mgr;

	private MCASpaceProxy proxy;

	private MCASpaceServer remoteRef;

	private LookupLocator locators[];

	private EventListenerList listeners;

	private long seqNum;

	public MCASpaceServerImpl(String[] configArgs, LifeCycle lifeCycle) 
	throws IOException,ConfigurationException, LoginException
	{
		try {
			final Configuration config = 
				ConfigurationProvider.getInstance(configArgs,
						getClass().getClassLoader());

			loginContext = (LoginContext) config.getEntry(
					COMPONENT_NAME, "loginContext", LoginContext.class, null);

			if (loginContext == null) {
				init(config);
			}else{
				loginContext.login();
				try {
					Subject.doAsPrivileged(
							loginContext.getSubject(),
							new PrivilegedExceptionAction(){
								public Object run() throws Exception {
									init(config);
									return null;
								}
							},
							null);
				} catch (PrivilegedActionException e) {
					e.printStackTrace();
					throw new Error();
				}
			}
			logger.info("MCASpace started: " + this);
		}catch (ConfigurationException e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	/**
	 * 
	 * @param config
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public void init(Configuration config) throws ConfigurationException, IOException {

		listeners = new EventListenerList();
		final Exporter basicExporter = 
			new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
					new BasicILFactory(), false, true);
		exporter = (Exporter)Config.getNonNullEntry(config,
				COMPONENT_NAME,	"serverExporter", Exporter.class,
				basicExporter);
		remoteRef = (MCASpaceServer)exporter.export(this);
		proxy = new MCASpaceProxy(remoteRef);
		cases = new HashMap<String,MCAPersistentOutriggerImpl>();
		locators = new LookupLocator[]{new LookupLocator("jini://localhost:4160")};
		DiscoveryManagement dm = new LookupLocatorDiscovery(locators);
		mgr = new JoinManager(proxy, getAttributes(), this,dm,null, config);
	}

	/**
	 * 
	 * @return
	 */
	private static Entry[] getAttributes() {
		final Entry info = new ServiceInfo("MCASpace", 
				"LACL", "University Paris East",
				"1.0", "", "");

		final Entry type = 
			new BasicServiceType("MCASpace");

		return new Entry[]{info, type};
	}

	/**
	 * 
	 * @see org.mca.server.MCASpaceServer#addCase(java.lang.String, java.lang.String)
	 */
	@Override
	public ComputationCase addCase(String name, String description) throws RemoteException,MCASpaceException {
		logger.fine("MCASpaceServerImpl -- addCase [" + name +", " + description + "]");
		if (cases.containsKey(name)) 
			throw new MCASpaceException("[" + name + "] computation Case with the same name exists.");
		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + FILE_JAVASPACE);
		ServiceConfigurator serviceConfigurator = context.getBean(SERVICE_JAVASPACE,ServiceConfigurator.class);
		
		RMISecurityManager securityManager = new RMISecurityManager();
		System.setProperty("java.rmi.server.codebase", serviceConfigurator.getCodebaseFormate());
		System.setProperty("java.security.policy", serviceConfigurator.getPolicy());
		System.setSecurityManager(securityManager);
		logger.fine("MCASpaceServerImpl -- codebase [" + serviceConfigurator.getCodebaseFormate() + "]");
		ServiceStarter starter = new ServiceStarter(serviceConfigurator);
		logger.fine("MCASpaceServerImpl -- " + starter);
		Object o = starter.startWithoutAdvertise();
		logger.fine("MCASpaceServerImpl -- " + o );
		MCAPersistentOutriggerImpl w = (MCAPersistentOutriggerImpl) o;
		logger.fine("MCASpaceServerImpl -- JavaSpace [" + name + "] started.");
		Entry[] entries = new Entry[]{new ComputationCaseInfo(name,description)};
		try {
			w.addLookupAttributes(entries);			
			State state = new State();
			state.state = ComputationCaseState.STARTED;
			w.space().write(state, null, Long.MAX_VALUE);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		cases.put(name,w);
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName objectName;
		try {
			objectName = new ObjectName(JMXConstantes.JMX_CASE_NAME_PREFIX + name);
			mBeanServer.registerMBean(w, objectName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		} 
		ComputationCase computationCase = new ComputationCaseImpl(w);
		logger.fine("MCASpaceServerImpl -- " + computationCase + " added.");
		fireNotify(MCASpace.ADD_CASE, computationCase);
		return computationCase;
	}

	/**
	 * 
	 * @see org.mca.server.MCASpaceServer#removeCase(java.lang.String)
	 */
	@Override
	public void removeCase(String name) throws RemoteException,MCASpaceException {
		MCAPersistentOutriggerImpl server = cases.get(name);
		server.destroy();
		cases.remove(name);
		fireNotify(MCASpace.REMOVE_CASE,null);
	}

	/**
	 * 
	 * @see org.mca.server.MCASpaceServer#getCases()
	 */
	@Override
	public Collection<ComputationCase> getCases() throws RemoteException{
		Collection<ComputationCase> cases = new ArrayList<ComputationCase>();
		for (Map.Entry<String, MCAPersistentOutriggerImpl> entry : this.cases.entrySet()) {
			MCAPersistentOutriggerImpl server = entry.getValue();
			ComputationCase c = new ComputationCaseImpl(server);	
			cases.add(c);
		}
		return cases;
	}

	/**
	 * 
	 * @see org.mca.server.MCASpaceServer#getCase(java.lang.String)
	 */
	@Override
	public ComputationCase getCase(String name) throws RemoteException,MCASpaceException{
		MCAPersistentOutriggerImpl w = cases.get(name);
		if(w == null)
			throw new MCASpaceException("[" + name + "] computation Case not exists.");
		return new ComputationCaseImpl(w);
	}

	@Override
	public ComputationCase getCase() throws RemoteException,MCASpaceException{
		MCAPersistentOutriggerImpl server = null;
		Iterator<MCAPersistentOutriggerImpl> iter = cases.values().iterator();
		if (iter.hasNext()) server = iter.next();
		if(server == null) return null;
		return new ComputationCaseImpl(server);
	}

	@Override
	public Object getServiceProxy() throws RemoteException {
		return proxy;
	}

	@Override
	public void serviceIDNotify(ServiceID serviceID) {
		LogUtil.info("MCASpace register on Lookup with ID [" + serviceID + "]", getClass());
	}

	@Override
	public EventRegistration register(MCASpaceEventListener listener)
	throws RemoteException {
		listeners.add(MCASpaceEventListener.class, listener);
		System.out.println("add new listener : " + listener);
		return new EventRegistration(0, proxy, null, 0);
	}

	/**
	 * 
	 * @param eventID
	 */
	protected void fireNotify(long eventID, ComputationCase computationCase){
		RemoteEventListener[] listeners = 
			this.listeners.getListeners(MCASpaceEventListener.class);
		for (RemoteEventListener listener : listeners) {

			MCASpaceEvent event = new MCASpaceEvent(proxy, eventID, seqNum++, null, computationCase);
			try {
				listener.notify(event);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (UnknownEventException e) {
				e.printStackTrace();
			}
		}
	}
}
