package org.mca.ext.skeleton;

import java.io.Serializable;

public interface BinOperator<X> extends Serializable {

	public X execute(X a, X b);
	
}
