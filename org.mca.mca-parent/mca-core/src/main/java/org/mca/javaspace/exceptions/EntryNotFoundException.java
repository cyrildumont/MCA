package org.mca.javaspace.exceptions;

import net.jini.core.entry.Entry;

public class EntryNotFoundException extends MCASpaceException {

	private Entry template;
	
	private String host;
	
	public EntryNotFoundException(Entry template, String host){
		this.template = template;
		this.host = host;
	}

	public Entry getTemplate() {
		return template;
	}

	public void setTemplate(Entry template) {
		this.template = template;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	@Override
	public String getMessage() {
		return "[" + host + "] Entry not found : " + template ;
	}
	
}
