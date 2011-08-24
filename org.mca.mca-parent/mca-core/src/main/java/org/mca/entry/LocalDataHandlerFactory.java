package org.mca.entry;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.mca.log.LogUtil;

public class LocalDataHandlerFactory implements DataHandlerFactory{

	public DataHandler getDataHandler(File file){
		LocalDataHandler entry = new LocalDataHandler();
		try {
			entry.filename = file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		entry.name = FilenameUtils.getBaseName(entry.filename);
		LogUtil.debug("LocalDataHandler created : " + entry.name, getClass());
		return entry;
	}
	
	@Override
	public DataHandler getDataHandler(String filename) {
		LocalDataHandler entry = new LocalDataHandler();
		entry.filename =filename;
		entry.name = FilenameUtils.getBaseName(entry.filename);
		LogUtil.debug("LocalDataHandler created : " + entry.name, getClass());
		return entry;
	}

}
