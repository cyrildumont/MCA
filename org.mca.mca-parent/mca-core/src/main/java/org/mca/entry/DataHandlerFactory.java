package org.mca.entry;

import java.io.File;
import java.io.Serializable;

public interface DataHandlerFactory extends Serializable{

	public DataHandler getDataHandler(File file);
	
	public DataHandler getDataHandler(String filename);
	
}
