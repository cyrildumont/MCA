package org.mca.server;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.swing.event.EventListenerList;

import net.jini.admin.Administrable;
import net.jini.admin.JoinAdmin;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupLocatorDiscovery;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import net.jini.lookup.entry.ServiceInfo;
import net.jini.space.JavaSpace05;

import org.mca.entry.ComputationCaseState;
import org.mca.entry.State;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.ComputationCaseInfo;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.MCASpaceEvent;
import org.mca.javaspace.MCASpaceEventListener;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.lookup.Lookup;
import org.mca.service.ServiceConfigurator;
import org.mca.service.ServiceStarter;
import org.mca.util.MCAUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.sun.jini.admin.DestroyAdmin;
import com.sun.jini.config.Config;
import com.sun.jini.lookup.entry.BasicServiceType;
import com.sun.jini.start.LifeCycle;

public class MCASpaceServerImpl implements MCASpaceServer, ServiceIDListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3354819183243663600L;


	private static final String SERVICE_JAVASPACE = "javaspace";
	private static final String FILE_JAVASPACE = System.getProperty("mca.home") + "/conf/javaspace.xml";


	public final static String COMPONENT_NAME = "org.mca.server.MCASpaceServer";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private final LoginContext loginContext;

	private Exporter exporter;

	private JoinManager mgr;

	private MCASpaceProxy proxy;

	private MCASpaceServer remoteRef;

	private LookupLocator locators[];

	private EventListenerList listeners;

	private long seqNum;

	private TransactionManager transactionManager;

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
		locators = new LookupLocator[]{new LookupLocator(MCAUtils.getIP(),4160)};
		DiscoveryManagement dm = new LookupLocatorDiscovery(locators);
		mgr = new JoinManager(proxy, getAttributes(), this,dm,null, config);
		Lookup finder = new Lookup(net.jini.core.transaction.server.TransactionManager.class);
		transactionManager =
				(net.jini.core.transaction.server.TransactionManager) finder.getService("localhost");
		logger.fine("MCASpaceServerImpl -- Transaction Manager : [" + transactionManager + "]");
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
		try {
			if (findSpace(name) != null) 
				throw new MCASpaceException("[" + name + "] computation case with the same name exists.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}
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
		Administrable admin = (Administrable) o;
		JoinAdmin jadmin = (JoinAdmin)admin.getAdmin();
		ComputationCaseInfo info = new ComputationCaseInfo(name,description);
		jadmin.addLookupAttributes(new Entry[]{info});
		JavaSpace05 space = (JavaSpace05)o;
		State state = new State();
		state.state = ComputationCaseState.STARTED;
		try {
			space.write(state, null, Long.MAX_VALUE);
		} catch (TransactionException e) {
			e.printStackTrace();
		}

		logger.fine("MCASpaceServerImpl -- JavaSpace [" + name + "] started.");

		ComputationCase computationCase = new ComputationCaseImpl(space, transactionManager);
		logger.fine("MCASpaceServerImpl -- Case [" + name + "] added.");
		fireNotify(MCASpace.ADD_CASE, computationCase);
		return computationCase;

	}

	/**
	 * 
	 * @see org.mca.server.MCASpaceServer#removeCase(java.lang.String)
	 */
	@Override
	public void removeCase(String name) throws RemoteException,MCASpaceException {
		JavaSpace05 space = null;
		try {
			space = findSpace(name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}
		if(space == null)
			throw new MCASpaceException("[" + name + "] computation Case not exists.");
		Administrable admin = (Administrable)space;
		DestroyAdmin dadmin = (DestroyAdmin)admin.getAdmin();
		dadmin.destroy();
		fireNotify(MCASpace.REMOVE_CASE,null);
	}

	/**
	 * 
	 * @see org.mca.server.MCASpaceServer#getCases()
	 */
	@Override
	public Collection<ComputationCase> getCases() throws RemoteException{
		Collection<ComputationCase> cases = new ArrayList<ComputationCase>();
//		for (Map.Entry<String, JavaSpace05> entry : this.cases.entrySet()) {
//			JavaSpace05 space = entry.getValue();
//			ComputationCase c = new ComputationCaseImpl(space,transactionManager);	
//			cases.add(c);
//		}
		return cases;
	}

	/**
	 * 
	 * @see org.mca.server.MCASpaceServer#getCase(java.lang.String)
	 */
	@Override
	public ComputationCase getCase(String name) throws RemoteException,MCASpaceException{
		JavaSpace05 space = null;
		try {
			space = findSpace(name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}
		if(space == null)
			throw new MCASpaceException("[" + name + "] computation Case not exists.");
		return new ComputationCaseImpl(space, transactionManager);
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private JavaSpace05 findSpace(String name) throws Exception {
		LookupLocator ll = new LookupLocator("jini://localhost");
		ServiceRegistrar registrar = ll.getRegistrar();
		Entry[] entries = new Entry[]{new ComputationCaseInfo(name, null)};
		Class<?>[] classes = new Class<?>[]{JavaSpace05.class};	
		ServiceTemplate template = new ServiceTemplate(null, classes,entries);
		JavaSpace05 space = (JavaSpace05)registrar.lookup(template);
		return space;
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private JavaSpace05 findSpace() throws Exception {
		LookupLocator ll = new LookupLocator("jini://localhost");
		ServiceRegistrar registrar = ll.getRegistrar();
		Class<?>[] classes = new Class<?>[]{JavaSpace05.class};	
		ServiceTemplate template = new ServiceTemplate(null, classes, null);
		JavaSpace05 space = (JavaSpace05)registrar.lookup(template);
		return space;
	}
	
	@Override
	public ComputationCase getCase() throws RemoteException,MCASpaceException{
		JavaSpace05 server = null;
		try {
			server = findSpace();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		} 
		if(server == null) return null;
		return new ComputationCaseImpl(server,transactionManager);
	}

	@Override
	public Object getServiceProxy() throws RemoteException {
		return proxy;
	}

	@Override
	public void serviceIDNotify(ServiceID serviceID) {
		logger.fine("MCASpaceServerImpl -- MCASpace register on Lookup with ID [" + serviceID + "]");
	}

	@Override
	public EventRegistration register(MCASpaceEventListener listener)
	throws RemoteException {
		listeners.add(MCASpaceEventListener.class, listener);
		logger.fine("MCASpaceServerImpl -- new listener registered : " + listener);
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
