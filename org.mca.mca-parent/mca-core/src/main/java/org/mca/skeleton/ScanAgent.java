package org.mca.skeleton;

import org.mca.math.DistributedVector;
import org.mca.math.SubVector;


public class ScanAgent<T> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;
	
	private BinOperator<T> operator;
	
	public ScanAgent(BinOperator<T> operator){
		this.operator = operator;
	}
	
	@Override
	protected Object executeSkel() throws Exception {
		int rank = task.getIntParameter(0);
		String inputName = task.getStringParameter(1);
		String outputName = task.getStringParameter(2);
		DistributedVector<T> input = 
			computationCase.getData(inputName) ;
		DistributedVector<T> output = 
			computationCase.getData(outputName) ;
		SubVector<T> localInput = (SubVector<T>)input.load(rank);
		SubVector<T> localOutput = (SubVector<T>)output.load(rank);
		T temp = null;
		for(int i=0;i < localInput.size();i++){
			T value = operator.execute(temp , localInput.get(i));
			localOutput.set(i, temp);
		}
		return temp;
	}
}
