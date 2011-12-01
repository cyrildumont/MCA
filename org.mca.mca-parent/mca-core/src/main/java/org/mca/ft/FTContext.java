package org.mca.ft;

import net.jini.core.entry.Entry;

public class FTContext implements Entry {

	private static final long serialVersionUID = 1L;

	public String computationCase;

	@Override
	public String toString() {
		return "FTContext [computationCase=" + computationCase + "]";
	}
	
}
