package org.mca.javaspace;

import java.rmi.MarshalledObject;

import net.jini.core.event.RemoteEvent;

@SuppressWarnings("serial")
public class MCASpaceEvent extends RemoteEvent{

	protected ComputationCase computationCase;
	
	public MCASpaceEvent(Object source, long eventID, long seqNum,
			MarshalledObject handback, ComputationCase computationCase) {
		super(source, eventID, seqNum, handback);
		this.computationCase = computationCase;
	}
	
	public ComputationCase getCase(){
		return computationCase;
	}

}
