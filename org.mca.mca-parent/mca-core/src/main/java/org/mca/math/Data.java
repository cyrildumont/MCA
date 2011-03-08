package org.mca.math;

import org.mca.entry.ComputationCase;
import org.mca.entry.Storable;
import org.w3c.dom.Node;


public class Data<E> extends Storable{

	public String name;
	
	protected ComputationCase computationCase;
	
	public void setComputationCase(ComputationCase computationCase) {
		this.computationCase = computationCase;
	}

	@Override
	public void parse(Node node) {}

	@Override
	public void store(Node parent) {}
		
}
