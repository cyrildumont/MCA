package org.mca.service;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.jini.config.AbstractConfiguration;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.entry.DataHandler;
import org.mca.entry.EntryFactory;
import org.mca.log.LogUtil;

/**
 * 
 */

/**
 * @author Cyril
 *
 */
public class ServiceConfigurator extends AbstractConfiguration{

	/** Log */
	private final static Log LOG = LogFactory.getLog(ServiceConfigurator.class);

	private static final String JINI_LOCATOR_PREFIX = "jini://";

	private String name;

	private List<String> codebase;

	private String policy;

	private  List<String> classpath;

	private String implClass;

	private String[] serverConfigArgs;

	private  List<Entry> entries;

	private  List<String> locators;

	private DataHandler byteCodeHandler;

	public ServiceConfigurator() {
		locators = new ArrayList<String>();
		locators.add("localhost");
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
			classpath += jar + ";";
		}
		return classpath;
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
	 * @return the policy
	 */
	public String getPolicy() {
		return policy;
	}

	/**
	 * @param policy the policy to set
	 */
	public void setPolicy(String policy) {
		this.policy = policy;
	}


	public String[] getServerConfigArgs() {
		return serverConfigArgs;
	}

	public void setServerConfigArgs(String[] serverConfigArgs) {
		this.serverConfigArgs = serverConfigArgs;
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


	/**
	 * @return the locators
	 */
	public LookupLocator[] getLookupLocators() {
		LookupLocator[] ll = new LookupLocator[locators.size()];
		for (int i=0;i < ll.length; i++) {
			try {
				ll[i] = new LookupLocator(JINI_LOCATOR_PREFIX + locators.get(i));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return ll;
	}

	/**
	 * @param locators the locators to set
	 */
	public void setLocators(List<String> locators) {
		this.locators = locators;
	}

	@Override
	protected Object getEntryInternal(String component, String name, 
			Class type, Object data) throws ConfigurationException {

		LogUtil.debug("requested entry : [component=" + component + "]" +
				"[name=" + name + "][type=" + type + "][data=" + data + "]",getClass());
		Configuration config = ConfigurationProvider.getInstance(new String[0]);
		return  config.getEntry(component, name, type);
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

	public DataHandler getByteCodeHandler() {
		return byteCodeHandler;
	}

	public void setByteCodeHandler(DataHandler byteCodeHandler) {
		this.byteCodeHandler = byteCodeHandler;
	}

}
