package org.mca.entry;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class FTPDataHandlerFactory implements DataHandlerFactory{

	private String login;
	private String host;
	private String password;
	private String path;
	
	
	public FTPDataHandler getDataHandler(File file){
		FTPDataHandler entry = new FTPDataHandler();
		entry.filename = path + "/" + file.getName();
		entry.name = FilenameUtils.getBaseName(entry.filename);
		entry.login = this.login;
		entry.server = this.host;
		entry.password = this.password;
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
