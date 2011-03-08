/**
 * 
 */
package org.mca.model;

import java.lang.reflect.Field;

/**
 * @author Cyril
 *
 */
public class Entry {
	
	private Object entry;
	
	private Class classEntry;
	
	private Service service;
	
	private String[][] properties;
	
	public Entry(Object entry, Service service) {
		this.entry = entry;
		this.service = service;
		this.classEntry = entry.getClass();
	}
	
	
	public Service getService(){
		return service;
	}
	
	public String getClassImpl(){
		return classEntry.getName();
	}
	
	public String[][] getProperties(){
		Field[] fields = classEntry.getFields();
		String[][] result = new String[fields.length][2];
		int i = 0;
		for (Field field : fields) {
			String[] property = new String[2];
			property[0] = field.getName();
			try {
				property[1] = String.valueOf(field.get(entry));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			result[i++] = property;
		}
		return result;
	}
}
