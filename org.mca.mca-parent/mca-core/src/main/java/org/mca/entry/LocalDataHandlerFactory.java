package org.mca.entry;

import org.mca.log.LogUtil;

public class LocalDataHandlerFactory extends DataHandlerFactory{

	private String path;
	
	public LocalDataHandlerFactory() {}
	
	public LocalDataHandlerFactory(String path) {
		this.path = path;
	}

	@Override
	public DataHandler generate(String filename, String name) {
		LocalDataHandler entry = new LocalDataHandler();
		entry.filename =path + "/" + filename;
		entry.name = name;
		LogUtil.debug("LocalDataHandler created : " + entry.name, getClass());
		return entry;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

}
