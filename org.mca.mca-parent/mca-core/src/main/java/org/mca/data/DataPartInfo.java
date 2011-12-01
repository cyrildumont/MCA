package org.mca.data;

import net.jini.core.entry.Entry;

public class DataPartInfo implements Entry {

	private static final long serialVersionUID = 1L;
	
	public String name;
	
	public Integer part;
	
	public DataPartInfo() {}

	public DataPartInfo(String name, Integer part) {
		this.name = name;
		this.part = part;
	}

}
