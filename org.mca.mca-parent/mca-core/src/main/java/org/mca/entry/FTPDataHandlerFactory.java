package org.mca.entry;


public class FTPDataHandlerFactory extends DataHandlerFactory{

	private String login;
	private String host;
	private String password;
	private String path;
	
	
	public FTPDataHandlerFactory() {}
	
	public FTPDataHandlerFactory(String login, String host, String password,
			String path) {
		this.login = login;
		this.host = host;
		this.password = password;
		this.path = path;
	}
	
	@Override
	public DataHandler generate(String filename, String name) {
		FTPDataHandler entry = new FTPDataHandler();
		entry.filename = path + "/" + filename;
		entry.name = name;
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
