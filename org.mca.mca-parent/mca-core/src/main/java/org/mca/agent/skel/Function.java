package org.mca.agent.skel;

import java.io.Serializable;

public interface Function<T,S> extends Serializable{

	public S execute(T value);
	
}
