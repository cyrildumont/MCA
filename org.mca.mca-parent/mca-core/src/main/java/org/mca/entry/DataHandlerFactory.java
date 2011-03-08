package org.mca.entry;

import java.io.File;

public interface DataHandlerFactory {

	public DataHandler getDataHandler(File file);
}
