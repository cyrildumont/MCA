package org.mca.agent.skel;

import java.io.Serializable;

public interface BinOperator<T> extends Serializable {

	public T execute(T a, T b);
	
}
