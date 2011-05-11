package org.mca.server.space.store;

import java.io.File;
import java.io.IOException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import com.sun.jini.outrigger.LogOps;
import com.sun.jini.outrigger.Recover;
import com.sun.jini.outrigger.Store;

public class MCAStore implements Store {

	private String logDir;
	private JavaSpaceRegenerator regenerator;
	private MCASpaceOps MCASpace;
	public MCAStore(Configuration config) throws ConfigurationException {
		logDir = System.getProperty("mca.home") + "/data";
		regenerator = new JavaSpaceRegenerator(logDir);
	}
	
	public void close() throws IOException {
		System.out.println("close");
	}

	public void destroy() throws IOException {
		File file = MCASpace.getFile();
		file.delete();
	}

	public LogOps setupStore(Recover space) {
		MCASpace = new MCASpaceOps(logDir);
		regenerator.regenerate(space, MCASpace);
		return MCASpace;
	}

}
