package org.mca.skeleton;

import org.mca.math.DistributedVector;
import org.mca.math.SubVector;

public class ReplAgent<T> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;

	private T x;
	
	private String output;
	
	public ReplAgent(T x, String output){
		this.x = x;
		this.output = output;
	}
	
	@Override
	protected Object executeSkel() throws Exception {
		DistributedVector<T> outputVector = computationCase.<DistributedVector<T>>getData(output);
		SubVector<T> subVector = (SubVector<T>)outputVector.load(rank);
		int partSize = outputVector.getPartSize();
		for (int i = 0; i < partSize; i++) {
			subVector.add(x);	
		}
		outputVector.unload();
		return null;
	}
	
}
