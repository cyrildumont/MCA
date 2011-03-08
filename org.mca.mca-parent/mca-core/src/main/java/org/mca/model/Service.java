package org.mca.model;

import java.util.Date;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.util.ClassUtil;

public class Service {

	/** Log */
	private final static Log LOG = LogFactory.getLog(Service.class);

	private Lookup lookup;

	private Object svcReg;
	private Object item;
	private Object serviceType;
	private ServiceItem serviceItem;
	
	private Class classSvcReg;

	private String serviceID;

	private Date leaseExpiration;

	private String codebase;

	private String serviceTypeName;

	private String superclass;

	private org.mca.model.Entry[] mEntries;

	public Service(Object svcReg) {
		this.svcReg = svcReg;
		classSvcReg = svcReg.getClass();
		if (LOG.isDebugEnabled()) {
			LOG.debug("svcReg --> " + svcReg);
			LOG.debug("classSvcReg --> " + classSvcReg);
		}
		setItem();
		Entry[] entries = getServiceItem().attributeSets;
		mEntries = new org.mca.model.Entry[entries.length];
		int i= 0;
		for (Entry entry : entries) {
			try {
				org.mca.model.Entry mEntry = new org.mca.model.Entry(entry, this);
				mEntries[i++] = mEntry;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param o
	 * @param lookup
	 */
	public Service(Object svcReg, Lookup lookup) {
		this(svcReg);
		this.lookup = lookup;

	}

	/**
	 * 
	 * @return
	 */
	public String getServiceID(){
		ServiceID serviceID = (ServiceID)ClassUtil.getValueOfField(item,"serviceID");
		return serviceID.toString();
	}

	/**
	 * 
	 *
	 */
	private void setItem() {
		this.item =  ClassUtil.getValueOfField(svcReg, "item");
		this.serviceItem  = getServiceItem();
		setServiceType();
	}

	/**
	 * 
	 *
	 */
	private void setServiceType() {
		this.serviceType = ClassUtil.getValueOfField(item, "serviceType");
	}

	/**
	 * 
	 * @return
	 */
	public String getServiceTypeName(){
		return String.valueOf(ClassUtil.getValueOfField(serviceType, "name"));
	}

	/**
	 * 
	 * @return
	 */
	public String getSuperclass(){
		Object o = ClassUtil.getValueOfField(serviceType, "superclass");
		return String.valueOf(ClassUtil.getValueOfField(o, "name"));
	}

	/**
	 * 
	 * @return
	 */
	public Date getLeaseExpiration(){		
		long leaseExpiration = (Long)ClassUtil.getValueOfField(svcReg, "leaseExpiration");
		return new Date(leaseExpiration);
	}

	public TabularDataSupport getEntries(){
		try {
			
			String[] itemNames = new String[]{"className"};
			String[] itemDescriptions = new String[]{"className"};
			OpenType[] itemTypes = new OpenType[]{SimpleType.STRING,};
			CompositeType compositeType = new CompositeType("Entry","Entry",itemNames,itemDescriptions,itemTypes);
			
			String[] indexNames = new String[]{"className"};
			TabularType tabularType = new TabularType("Entries", "Entries",compositeType,indexNames);
			TabularDataSupport support = new TabularDataSupport(tabularType);
			Entry[] entries = serviceItem.attributeSets;
			for (Entry entry : entries) {
				Object[] itemValues = new Object[1];
				itemValues[0] = entry.getClass().getName();
				//CompositeDataSupport compositeDataSupport = generateCompositeDataEntry(entry);
				CompositeDataSupport compositeData = new CompositeDataSupport(compositeType, itemNames, itemValues);
				support.put(compositeData);
			}
			return support;

		} catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public String getCodebase(){
		return String.valueOf(ClassUtil.getValueOfField(item, "codebase"));
	}

	public ServiceItem getServiceItem(){
		return (ServiceItem)ClassUtil.invokeMethod(item, "get", new Class[]{}, new Object[]{});
	}

	public void unsubscribe(){
		lookup.register(serviceItem, 0);
	}

}
