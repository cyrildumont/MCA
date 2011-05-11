/**
 * 
 */
package org.mca.entry;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;


/**
 * @author Cyril
 *
 */
public class EntryGenerator implements Observer,Runnable {

	/** Log */
	private final static Log LOG = LogFactory.getLog(EntryGenerator.class);

	private String dirName;

	private DataHandlerFactory dataHandlerFactory;

	private ComputationCase computationCase;

	public void setComputationCase(ComputationCase computationCase) {
		this.computationCase = computationCase;
	}


	public void setDirName(String dirName) {
		this.dirName = dirName;
	}


	/**
	 * 
	 */
	public void update(Observable observable, Object o) {
		DataHandler entry = dataHandlerFactory.getDataHandler((File)o);
		addDataHandler(entry);
	}

	/**
	 * 
	 * @param entry
	 * @param tnx
	 */
	private void addDataHandler(DataHandler entry) {
		LOG.debug("Add DataHandler to Space : [ name = " + entry.name +" ]");
		try {
			computationCase.addDataHandler(entry);
			LOG.debug("DataHandler added to Space : [ name = " + entry.name +" ]");
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}


	public void run() {
		ScanDir scan = new ScanDir(dirName,this);
		scan.run();

	}

	public void setDataHandlerFactory(DataHandlerFactory dataHandlerFactory) {
		this.dataHandlerFactory = dataHandlerFactory;
	}
}
