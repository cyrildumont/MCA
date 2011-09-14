package org.mca.entry;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.mca.log.LogUtil;

public class LocalDataHandlerFactory implements DataHandlerFactory{

	private String path;
	
	public DataHandler getDataHandler(File file){
		LocalDataHandler entry = new LocalDataHandler();
		entry.filename = path + "/" + file.getName();
		entry.name = FilenameUtils.getBaseName(entry.filename);
		LogUtil.debug("LocalDataHandler created : " + entry.name, getClass());
		return entry;
	}
	
	@Override
	public DataHandler getDataHandler(String filename) {
		LocalDataHandler entry = new LocalDataHandler();
		entry.filename =path + "/" + filename;
		entry.name = FilenameUtils.getBaseName(entry.filename);
		LogUtil.debug("LocalDataHandler created : " + entry.name, getClass());
		return entry;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

}
