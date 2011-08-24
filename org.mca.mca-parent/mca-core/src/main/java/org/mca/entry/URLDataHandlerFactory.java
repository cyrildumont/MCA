package org.mca.entry;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.mca.log.LogUtil;

public class URLDataHandlerFactory implements DataHandlerFactory{

	private String url;

	public DataHandler getDataHandler(File file){
		URLDataHandler entry = new URLDataHandler();
		String filename = file.getName();
		entry.url = url + "/" + filename;
		entry.name = FilenameUtils.getBaseName(filename);
		LogUtil.debug("URLDataHandler created : " + entry.name, getClass());
		return entry;
	}
	
	@Override
	public DataHandler getDataHandler(String filename) {
		URLDataHandler entry = new URLDataHandler();
		entry.url = url + "/" + filename;
		entry.name = FilenameUtils.getBaseName(filename);
		LogUtil.debug("URLDataHandler created : " + entry.name, getClass());
		return entry;
	}

	public void setUrl(String url) {
		this.url = url;
	}



}
