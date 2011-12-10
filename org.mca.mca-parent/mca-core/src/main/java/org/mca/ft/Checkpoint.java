package org.mca.ft;

import net.jini.core.entry.Entry;

public class Checkpoint implements Entry{

	private static final long serialVersionUID = 1L;

	/**
	 * Different types of checkpoint
	 * 
	 * @author Cyril Dumont
	 *
	 */
	public enum Type{
		LOCAL,
		GLOBAL
	}
	
	public Type type;
	
	public Integer id;

	public Checkpoint() {}
	
	public Checkpoint(Integer id, Type type){
		this.id = id;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}
	
	public Type getType() {
		return type;
	}
	
}
