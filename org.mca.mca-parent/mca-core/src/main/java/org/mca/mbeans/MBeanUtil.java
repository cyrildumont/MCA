/**
 * 
 */
package org.mca.mbeans;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.Context;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Cyril
 *
 */
public class MBeanUtil {

	/** Log */
	private final static Log LOG = LogFactory.getLog(MBeanUtil.class);

	public static JMXConnector createJMXConnector(){

		try {
			final HashMap env = new HashMap();
			final String rprop = RMIConnectorServer.JNDI_REBIND_ATTRIBUTE;
			final String rebind=System.getProperty(rprop,"true");
			final String factory = 
				System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
			final String ldapServerUrl = 
				System.getProperty(Context.PROVIDER_URL);
			final String ldapUser = 
				System.getProperty(Context.SECURITY_PRINCIPAL);
			final String ldapPasswd = 
				System.getProperty(Context.SECURITY_CREDENTIALS);

			// Transfer some system properties to the Map
			//
			if (factory!= null) // this should not be needed
				env.put(Context.INITIAL_CONTEXT_FACTORY,factory);
			if (ldapServerUrl!=null) // this should not be needed
				env.put(Context.PROVIDER_URL, ldapServerUrl);
			if (ldapUser!=null) // this is needed when LDAP is used
				env.put(Context.SECURITY_PRINCIPAL, ldapUser);
			if (ldapPasswd != null) // this is needed when LDAP is used
				env.put(Context.SECURITY_CREDENTIALS, ldapPasswd);
			env.put(rprop,rebind); // default is true.

			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			final JMXServiceURL jurl = new JMXServiceURL("service:jmx:rmi://");
			JMXConnectorServer rmis =
				JMXConnectorServerFactory.newJMXConnectorServer(jurl, env, mBeanServer);
			rmis.start();
			return rmis.toJMXConnector(env);


		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}


	public static void register(JMXConnector proxy, String host) throws JMXRegisterException{
		try {
			LookupLocator lookup = new LookupLocator("jini://" + host);
			final ServiceRegistrar registrar = lookup.getRegistrar();
			ServiceItem srvcItem = new ServiceItem(null, proxy, null);
			registrar.register(srvcItem, Lease.ANY);
		} catch (MalformedURLException e) {
			LOG.error(e.getClass().getName() + " : " + e.getMessage());
			throw new JMXRegisterException();
		} catch (IOException e) {
			LOG.error(e.getClass().getName() + " : " + e.getMessage());
			throw new JMXRegisterException();
		} catch (ClassNotFoundException e) {
			LOG.error(e.getClass().getName() + " : " + e.getMessage());
			throw new JMXRegisterException();
		} 

	}

}
