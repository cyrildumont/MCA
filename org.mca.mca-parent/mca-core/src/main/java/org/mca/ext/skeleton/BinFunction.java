package org.mca.ext.skeleton;

import java.io.Serializable;

public interface BinFunction<X,Y,Z> extends Serializable{

	public Z execute(X x, Y y);
	
}
