package org.mca.ext.skeleton;

import java.io.Serializable;

public interface Function<X,Y> extends Serializable{

	public Y execute(X x);
	
}
