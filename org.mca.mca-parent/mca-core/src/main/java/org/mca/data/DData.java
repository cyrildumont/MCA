package org.mca.data;

import net.jini.core.entry.Entry;

public class DData implements Entry {

	private static final long serialVersionUID = 1L;

	public Integer globalCheckpoint;
	
	public Integer checkpoint;
	
	public String name;

	public Object value;

	public DData() {}

	public DData(String name, Object value, Integer checkpoint) {
		this.name = name;
		this.value = value;
		this.checkpoint = checkpoint;
	}

	public DData(String name,Integer checkpoint) {
		this.name = name;
		this.checkpoint = checkpoint;
	}

	public DData(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "DData [name=" + name + ", value=" + value + ", checkpoint=" + checkpoint + "]";
	}
}
