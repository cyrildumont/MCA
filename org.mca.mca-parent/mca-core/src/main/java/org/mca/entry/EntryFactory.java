/**
 * 
 */
package org.mca.entry;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;

import net.jini.core.entry.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Cyril
 *
 */
public class EntryFactory {

	/** Log */
	private final static Log LOG = LogFactory.getLog(EntryFactory.class);
	
	/**
	 * 
	 * @param className
	 * @param propertiesTable
	 * @return
	 */
	public static Entry createEntry(String className, Hashtable<String, String> propertiesTable){
		try {
			if (LOG.isTraceEnabled()) {
				LOG.trace("creation of a new Entry ..");
				LOG.trace("		className ---> " + className);
			}
			Class entryClass = Class.forName(className);
			Entry entry = (Entry)entryClass.newInstance();
			setPropertiesToEntry(entry, propertiesTable);
			return entry;
		} catch (ClassNotFoundException e) {
			LOG.error(className + " not found.");
			return null;
		} catch (InstantiationException e) {
			LOG.error("Instantiation error.");
			return null;
		} catch (IllegalAccessException e) {
			LOG.error("Illegal Access error.");
			return null;
		}
	}

	/**
	 * 
	 * @param entry
	 * @param propertiesTable
	 */
	private static void setPropertiesToEntry(Entry entry, Hashtable<String, String> propertiesTable) {
		Enumeration<String> names = propertiesTable.keys();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String value = propertiesTable.get(name);
			if (LOG.isTraceEnabled()) {
				LOG.trace("		" + name + " ---> " + value);
			}
			Class classEntry = entry.getClass();
			try {
				Field field = classEntry.getDeclaredField(name);
				field.set(entry, value);
			} catch (SecurityException e) {
				LOG.error("impossible to set property [" + name + "]");
			} catch (NoSuchFieldException e) {
				LOG.error("property [" + name + "] does not exist.");
			} catch (IllegalArgumentException e) {
				LOG.error("impossible to set property [" + name + "]");
			} catch (IllegalAccessException e) {
				LOG.error("impossible to set property [" + name + "]");
			}
			
		}
	}
	
	
	
}
