package org.mca.model;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.management.remote.JMXConnector;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.lookup.entry.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.listener.LookupListener;
import org.mca.mbeans.MBeanUtil;
import org.mca.util.ClassUtil;
import org.mca.util.MCAUtils;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@SuppressWarnings("serial")
@ManagedResource(objectName = "MCA:type=Lookup")
public class Lookup implements Serializable{

	/** Log */
	private final static Log LOG = LogFactory.getLog(Lookup.class);

	private Object registrar;

	private Class classRegistrar;

	private HashMap serviceByID;

	private String serviceID;

	private int servicesCount;

	private String classLookup;

	private String host;

	private int port;

	private List<Service> services ;

	public Lookup(Object registrar) {
		this.registrar = registrar;
		classRegistrar = registrar.getClass().getSuperclass();
		if (LOG.isTraceEnabled()) {
			LOG.trace("registrar --> " + registrar);
			LOG.trace("classRegistrar --> " + classRegistrar);
		}
		if (LOG.isTraceEnabled()) {
			ClassUtil.logFields(registrar, classRegistrar , LOG);
		}
		JMXConnector jmxConnector = MBeanUtil.createJMXConnector();
		net.jini.core.entry.Entry[] entries = new net.jini.core.entry.Entry[]{new Name("JMXConnector")};
		ServiceItem serviceItem = new ServiceItem(null,jmxConnector,entries);
		
		try {
			getServiceRegistrar().register(serviceItem, Lease.FOREVER);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		services = new ArrayList<Service>();
		refresh();		
	}


	/**
	 * 
	 * @return
	 */
	public ServiceRegistrar getServiceRegistrar(){
		return (ServiceRegistrar)ClassUtil.getValueOfField(registrar, classRegistrar, "proxy");
	}

	/**
	 * @return the host
	 */
	@ManagedAttribute
	public String getHost() {
			return MCAUtils.getIP();
	}

	/**
	 * @return the port
	 */
	@ManagedAttribute
	public int getPort() {
		LookupLocator locator = (LookupLocator)ClassUtil.getValueOfField(registrar, classRegistrar, "myLocator");
		return locator.getPort();
	}

	/**
	 * @return the classLookup
	 */
	@ManagedAttribute
	public String getClassLookup() {
		return registrar.getClass().getName();
	}

	/**
	 * 
	 * @return
	 */
	@ManagedAttribute
	public String getServiceID(){
		ServiceID serviceID = (ServiceID)ClassUtil.getValueOfField(registrar, classRegistrar, "myServiceID");
		return serviceID.toString();
	}

	/**
	 * @return the serviceByID
	 */
	public synchronized void setServiceByID() {
		HashMap serviceByID = (HashMap)ClassUtil.getValueOfField(registrar, classRegistrar, "serviceByID");
		servicesCount = serviceByID.size();
		if (LOG.isDebugEnabled()) {
			LOG.debug("serviceByID --> " + serviceByID);
		}
		for (Object key : serviceByID.keySet()) {
			Object o = serviceByID.get(key);
			Service service = new Service(o, this);
			services.add(service);
		}
	}

	/**
	 * @return the servicesCount
	 */
	@ManagedAttribute
	public int getServicesCount() {
		HashMap serviceByID = (HashMap)ClassUtil.getValueOfField(registrar, classRegistrar, "serviceByID");
		return serviceByID.size();
	}

	/**
	 * 
	 *
	 */
	public void refresh() {
		services.clear();
		setServiceByID();
	}


	/**
	 * 
	 * @param item
	 * @param lease
	 */
	public void register(Object item, int lease) {
		Class[] parameters = new Class[]{item.getClass(), long.class};
		Object[] parametersValues = new Object[]{item, lease};
		ClassUtil.invokeMethod(getServiceRegistrar(), ServiceRegistrar.class, "register", parameters, parametersValues);
	}

	/**
	 * 
	 * @param svcReg
	 */
	public void deleteService(Object svcReg) {
		Class[] parameters = new Class[]{svcReg.getClass(), long.class};
		Object[] parametersValues = new Object[]{svcReg, new Date().getTime()};
		ClassUtil.invokeMethod(registrar, classRegistrar, "deleteService", parameters, parametersValues);

	}

	/**
	 * 
	 * @return
	 */
	@ManagedAttribute
	public TabularDataSupport getServices(){
		try {
			
			String[] itemNames = new String[]{"UUID","Name","ImplClass","LeaseExpiration"};
			String[] itemDescriptions = new String[]{"UUID","Name","ImplClass","LeaseExpiration"};
			OpenType[] itemTypes = new OpenType[]{SimpleType.STRING,SimpleType.STRING, SimpleType.STRING, SimpleType.DATE };
			CompositeType compositeType = new CompositeType("Services","Services",itemNames,itemDescriptions,itemTypes);
			String[] indexNames = new String[]{"UUID"};
			TabularType tabularType = new TabularType("Services", "Services",compositeType,indexNames);
			TabularDataSupport support = new TabularDataSupport(tabularType);
			
			int i = 0;
			for (Service service : services) {
				Object[] itemValues = new Object[4];
				itemValues[0] = service.getServiceID();
				itemValues[1] = service.getSuperclass();
				itemValues[2] = service.getSuperclass();
				itemValues[3] = service.getLeaseExpiration();
				CompositeDataSupport compositeData = new CompositeDataSupport(compositeType, itemNames, itemValues);
				support.put(compositeData);
			}

			return support;

		} catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}
	}

}
