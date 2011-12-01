package org.mca.entry;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SFTPDataHandlerFactory extends DataHandlerFactory{

	/** Log */
	private final static Log LOG = LogFactory.getLog(SFTPDataHandlerFactory.class);
	private String login;
	private String host;
	private String password;
	private String path;
	
	public SFTPDataHandlerFactory(String login, String host, String password,
			String path) {
		this.login = login;
		this.host = host;
		this.password = password;
		this.path = path;
	}
	
	@Override
	public DataHandler generate(String filename, String name) {
		SFTPDataHandler entry = new SFTPDataHandler();
		entry.filename = path + "/" + filename;
		entry.name = name;
		entry.login = this.login;
		entry.server = this.host;
		entry.password = this.password;
		LOG.debug("SFTPDataReader created : " + entry.name);
		return entry;
	}

	public void setLogin(String login) {
		this.login = login;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
}
