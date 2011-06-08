package org.mca.worker;

import java.io.FileInputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.logging.Logger;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;

import org.mca.agent.ComputeAgent;
import org.mca.worker.exception.AgentNotFoundException;

/**
 * 
 * @author Cyril
 *
 */
public class AgentListener  {

	private static final String COMPONENT_NAME = "org.mca.worker.AgentListener";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private ComputeAgent agent;

	private ServiceTemplate template;

	private Hashtable<String, ComputeAgent> agents;

	public AgentListener() {
		logger.finest("Worker -- AgentListener started");
		agents = new Hashtable<String, ComputeAgent>();
	}


	/**
	 * 
	 * @param serviceID
	 * @return
	 */
	public ComputeAgent getAgent(String url) throws AgentNotFoundException{

		int i = url.lastIndexOf("/");
		String host = url.substring(0, i);
		String name = url.substring(i+1);

		agent = this.agents.get(name);
		if (agent != null) {
			return agent;
		}
		logger.finest("Search for the ComputingAgent [" + name +"] on [" + host + "]");	
		Name entry = new Name(name);
		template = new ServiceTemplate(null, null, new Entry[]{entry});
		try {
			LookupLocator ll = new LookupLocator(host);
			ServiceRegistrar registrar = ll.getRegistrar();
			ComputeAgent agent = (ComputeAgent)registrar.lookup(template);
			if(agent == null) throw new AgentNotFoundException("Agent not found");
			verifyComputeAgent(agent);
			return agent;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AgentNotFoundException(e.getMessage());
		}

	}

	/**
	 * 
	 * @param agent
	 * @throws Exception
	 */
	public void verifyComputeAgent(ComputeAgent agent) throws Exception{
		CodeSource source = agent.getClass().getProtectionDomain().getCodeSource();
		URL url = source.getLocation();
		logger.fine("AgentListener -- download ComputeAgent bytecode on " + url);
		Certificate[] certificates = source.getCertificates();
		if (certificates == null) {
			logger.warning("AgentListener -- bytecode is not signed");
			throw new Exception("bytecode of the agent not signed");
		}
		KeyStore keystore = KeyStore.getInstance("jks");
		String keystoreFile = System.getProperty("javax.net.ssl.trustStore");
		FileInputStream fis = new FileInputStream(keystoreFile);
		keystore.load(fis, "worker".toCharArray());
		X509Certificate cert = (X509Certificate)keystore.getCertificate("root");
		X509Certificate certToVerify = (X509Certificate)certificates[0];
		logger.fine("AgentListener -- agent certificate issuer: " + certToVerify.getIssuerDN());
		certToVerify.verify(cert.getPublicKey());
		logger.fine("AgentListener -- signature verification succeeded");
	}
}
