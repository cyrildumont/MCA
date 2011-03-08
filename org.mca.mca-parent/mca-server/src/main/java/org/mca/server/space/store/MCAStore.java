package org.mca.server.space.store;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import com.sun.jini.outrigger.LogOps;
import com.sun.jini.outrigger.Recover;
import com.sun.jini.outrigger.Store;

public class MCAStore implements Store {

	private String logDir;
	private JavaSpaceRegenerator regenerator;
	
	/** Log */
	private final static Log LOG = LogFactory.getLog(MCAStore.class);
	
	public MCAStore(Configuration config) throws ConfigurationException {
		logDir = System.getProperty("mca.home") + "/data";
		regenerator = new JavaSpaceRegenerator(logDir);
	}
	
	public void close() throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("close");
		}
	}

	public void destroy() throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("close");
		}
	}

	public LogOps setupStore(Recover space) {
		MCASpaceOps MCASpace = new MCASpaceOps(logDir);
		regenerator.regenerate(space, MCASpace);
		return MCASpace;
	}

}
