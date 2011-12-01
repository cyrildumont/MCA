package org.mca.entry;

import org.mca.log.LogUtil;

public class URLDataHandlerFactory extends DataHandlerFactory{

	private String url;

	
	public URLDataHandlerFactory(String url) {
		this.url = url;
	}

	@Override
	public DataHandler generate(String filename, String name) {
		URLDataHandler entry = new URLDataHandler();
		entry.url = url + "/" + filename;
		entry.name = name;
		LogUtil.debug("URLDataHandler created : " + entry.name, getClass());
		return entry;
	}

	public void setUrl(String url) {
		this.url = url;
	}


}
