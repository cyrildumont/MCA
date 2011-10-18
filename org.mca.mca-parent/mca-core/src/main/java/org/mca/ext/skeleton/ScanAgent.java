package org.mca.ext.skeleton;

import java.util.List;

import org.mca.math.DistributedVector;
import org.mca.math.SubVector;


public class ScanAgent<X> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;
	
	private BinOperator<X> operator;
	
	private String input;
	
	private String output;
	
	public ScanAgent(BinOperator<X> operator, String input, String output){
		this.operator = operator;
	}
	
	@Override
	protected Object executeSkel() throws Exception {
		DistributedVector<X> inputvector = computationCase.<DistributedVector<X>>getData(input);
		SubVector<X> inputPart = (SubVector<X>)inputvector.load(rank);
		List<X> values = (List<X>)inputPart.getValues();
		DistributedVector<X> outputVector = computationCase.<DistributedVector<X>>getData(output);
		SubVector<X> outputPart = (SubVector<X>)outputVector.load(rank);
		X temp = null;
		for(int i=0;i < inputPart.size();i++){
			X value = operator.execute(temp , inputPart.get(i));
			outputPart.set(i, temp);
		}
		return temp;
	}
}
