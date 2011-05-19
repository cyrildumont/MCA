package org.mca.javaspace;

import net.jini.entry.AbstractEntry;

@SuppressWarnings("serial")
public class ComputationCaseInfo extends AbstractEntry{

	public String name;
	public String description;
	
	public ComputationCaseInfo(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public ComputationCaseInfo() {
	}
	
}
