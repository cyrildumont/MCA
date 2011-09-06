package org.mca.agent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;

import org.mca.entry.EntryFactory;

/**
 * 
 */

/**
 * @author Cyril
 *
 */
public class AgentDescriptor {


	private static final String JINI_LOCATOR_PREFIX = "jini://";

	private String name;

	private List<String> codebase;

	private  List<String> classpath;

	private String implClass;

	private  List<Entry> entries;

	private String locator;

	private String byteCodeFile;

	public AgentDescriptor() {
		locator = "localhost";
		entries = new ArrayList<Entry>();
	}

	/**
	 * 
	 * @param jarPath
	 */
	public void addJarToCodebase(String jarPath){
		codebase.add(jarPath);
	}

	/**
	 * 
	 * @param jarPath
	 */
	public void addJarToClasspath(String jarPath){
		classpath.add(jarPath);
	}

	/**
	 * @return the classpath
	 */
	public String getClasspathFormate() {
		String classpath= "";
		for (String jar : this.classpath) {
			classpath += jar + ":";
		}
		return classpath.substring(0, classpath.length()-1);
	}

	/**
	 * @param classpath the classpath to set
	 */
	public void setClasspath(ArrayList<String> classpath) {
		this.classpath = classpath;
	}

	/**
	 * @return the codebase
	 */
	public String getCodebaseFormate() {
		String codebase= "";
		for (String jar : this.codebase) {
			codebase += jar + " ";
		}
		return codebase;
	}

	/**
	 * @param codebase the codebase to set
	 */
	public void setCodebase(List<String> codebase) {
		this.codebase = codebase;
	}

	/**
	 * @return the implClass
	 */
	public String getImplClass() {
		return implClass;
	}

	/**
	 * @param implClass the implClass to set
	 */
	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}

	/**
	 * 
	 * @return
	 */
	public Entry[] getEntries(){
		return entries.toArray(new Entry[entries.size()]);
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	public void addEntry(Entry entry){
		entries.add(entry);
	}

	/**
	 * @return the locators
	 */
	public LookupLocator getLookupLocator() {
		try {
			return new LookupLocator(JINI_LOCATOR_PREFIX + locator);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param locators the locators to set
	 */
	public void setLocator(String locator) {
		this.locator = locator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		Hashtable<String, String> propertiesTable = new Hashtable<String, String>();
		propertiesTable.put("name", name);
		Entry entry = EntryFactory.createEntry("net.jini.lookup.entry.Name", propertiesTable);
		entries.add(entry);
	}

	public byte[] getByteCode() {
		File file = new File(byteCodeFile);
		long length = file.length();
		byte[] bytes = new byte[(int)length];
		try {
			InputStream is = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while ( (offset < bytes.length)
					&&
					( (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) ) {
				offset += numRead;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	public void setByteCodeFile(String byteCodeFile) {
		this.byteCodeFile = byteCodeFile;
	}
	
	public String getURL(){
		return JINI_LOCATOR_PREFIX + locator +"/" + name;
	}


}
