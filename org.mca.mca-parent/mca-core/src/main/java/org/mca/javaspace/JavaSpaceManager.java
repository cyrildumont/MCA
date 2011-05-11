package org.mca.javaspace;

import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.javaspace.exceptions.NoJavaSpaceFoundException;
import org.mca.lookup.Lookup;

public class JavaSpaceManager {
	

	/** Log */
	private final static Log LOG = LogFactory.getLog(JavaSpaceManager.class);
	
	private static final String DEFAULT_HOST = "localhost";

	public static JavaSpace05 getSpace() throws NoJavaSpaceFoundException{
		LOG.debug("Searching for a JavaSpace...");
		Lookup finder = new Lookup(JavaSpace.class);
		JavaSpace05 space = (JavaSpace05) finder.getService();
		if (space != null) {
			LOG.debug("A JavaSpace has been discovered.");
		}
		else{
			LOG.error("No JavaSpace found.");
			throw new NoJavaSpaceFoundException();
		}
		
		return space;	
	}
	
	public static JavaSpace05 getSpace(String name) throws NoJavaSpaceFoundException{
		return getSpace(DEFAULT_HOST, name);
	}
	
	public static JavaSpace05 getSpace(String host, String name) throws NoJavaSpaceFoundException{
		LOG.debug("Searching for a JavaSpace...");
		Lookup finder = new Lookup(JavaSpace.class);
		JavaSpace05 space = (JavaSpace05) finder.getService(host);
		if (space != null) {
			LOG.debug("A JavaSpace has been discovered.");
		}
		else{
			LOG.error("No JavaSpace.");
			throw new NoJavaSpaceFoundException();
		}		
		return space;
	}
	
}
