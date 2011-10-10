package org.mca.skeleton;

import org.mca.math.DistributedVector;
import org.mca.math.SubVector;

public class ReplAgent<X> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;

	private X x;
	
	private String output;
	
	public ReplAgent(X x, String output){
		this.x = x;
		this.output = output;
	}
	
	@Override
	protected Object executeSkel() throws Exception {
		DistributedVector<X> outputVector = computationCase.<DistributedVector<X>>getData(output);
		SubVector<X> subVector = (SubVector<X>)outputVector.load(rank);
		int partSize = outputVector.getPartSize();
		for (int i = 0; i < partSize; i++) {
			subVector.add(x);	
		}
		outputVector.unload();
		return null;
	}
	
}
