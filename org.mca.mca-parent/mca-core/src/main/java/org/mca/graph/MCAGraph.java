package org.mca.graph;

import org.mca.scheduler.Task;

import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.impl.SparseGraph;

public class MCAGraph extends SparseGraph{
	
	private StringLabeller labeller;
	private StringLabeller nameLabeller;
	
	public MCAGraph() {
		labeller = StringLabeller.getLabeller(this);
		nameLabeller = StringLabeller.getLabeller(this, new Task());	
	}

	public StringLabeller getLabeller() {
		return labeller;
	}
	
	public MCAVertex getVertex(long id){
		return (MCAVertex)labeller.getVertex(String.valueOf(id));
	}
	
	public MCAVertex getVertex(String name){
		return (MCAVertex)nameLabeller.getVertex(name);
	}

	public StringLabeller getNameLabeller() {
		return nameLabeller;
	}

}
