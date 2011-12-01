package org.mca.ft;

import net.jini.core.entry.Entry;

import org.mca.data.DData;

public class Checkpoint implements Entry{

	public DData data;
	
	public Integer id;

	public Checkpoint() {}
	
	public Checkpoint(Integer id, DData data){
		this.id = id;
		this.data = data;
	}
	
	public Object getValue(){
		return data.value;
	}
	
}
