/**
 * 
 */
package org.mca.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.management.openmbean.TabularDataSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.util.ClassUtil;

/**
 * @author Cyril
 *
 */
public class JavaSpace {

	private static final String CLASS_SCHEDULER = "com.sun.javaspaces.scheduler.Scheduler";


	/** Log */
	private final static Log LOG = LogFactory.getLog(JavaSpace.class);


	private Object javaspace;
	private Class classJavaspace;

	private int holdersCount;

	private String[] holdersClassName;

	private int entriesCount;


	/**
	 * 
	 * @param javaspace
	 */
	public JavaSpace(Object javaspace){
		Class classWrapper = javaspace.getClass().getSuperclass();

		this.javaspace = ClassUtil.getValueOfField(javaspace, classWrapper, "delegate");
		classJavaspace = this.javaspace.getClass();
		if (LOG.isDebugEnabled()) {
			LOG.debug("javaspace --> " + javaspace);
			LOG.debug("classJavaspace --> " + classJavaspace);
		}
		if (LOG.isDebugEnabled()) {
			ClassUtil.logFields(this.javaspace, classJavaspace, LOG);
		}

	}

	/**
	 * 
	 */
	public int getHoldersCount(){
		Object contents = ClassUtil.getValueOfField(this.javaspace, classJavaspace, "contents");
		HashMap holders = (HashMap)ClassUtil.getValueOfField(contents,"holders");
		return holders.size();
	}

	public TabularDataSupport getScheduler(){
		try {
			Object contents = ClassUtil.getValueOfField(this.javaspace, classJavaspace, "contents");
			HashMap holders = (HashMap)ClassUtil.getValueOfField(contents,"holders");
			Object schedulerHolder = holders.get(CLASS_SCHEDULER);
			Object fastList = ClassUtil.getValueOfField(schedulerHolder, "contents");
			Object head = ClassUtil.invokeMethod(fastList, "head", new Class[]{}, new Object[]{});
			Object rep = ClassUtil.invokeMethod(head,head.getClass().getSuperclass(), "rep", new Class[]{}, new Object[]{});
			Class schedulerClass = Class.forName(CLASS_SCHEDULER);
			Field[] fields = (Field[])ClassUtil.invokeMethod(rep, "getFields", new Class[]{Class.class}, new Object[]{schedulerClass});
			return null;

		} catch (ClassNotFoundException e) {
			LOG.error(e.getMessage());
			return null;
		}
	}
	/**
	 * @return the holdersClassName
	 */
	public String[] getHoldersClassName() {
		Object contents = ClassUtil.getValueOfField(this.javaspace, classJavaspace, "contents");
		HashMap holders = (HashMap)ClassUtil.getValueOfField(contents,"holders");
		String[] holdersClassName = new String[holders.size()];
		int i = 0;
		for (Object className : holders.keySet()) {
			holdersClassName[i++] = String.valueOf(className);
		};
		return holdersClassName;
	}

	/**
	 * @return the holdersClassName
	 */
	public int getEntriesCount() {
		Object contents = ClassUtil.getValueOfField(this.javaspace, classJavaspace, "contents");
		Hashtable idmap = (Hashtable)ClassUtil.getValueOfField(contents,"idMap");
		return idmap.size();
	}
	
	public int getEventRegistrations(){
		Map eventRegistrations = (Map)ClassUtil.getValueOfField(this.javaspace, classJavaspace, "eventRegistrations");
		return eventRegistrations.size();
	}

}
