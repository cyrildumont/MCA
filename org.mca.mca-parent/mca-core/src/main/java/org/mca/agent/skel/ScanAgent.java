package org.mca.agent.skel;

import org.mca.agent.AbstractComputeAgent;
import org.mca.math.SubVector;
import org.mca.math.Vector;


public class ScanAgent<T> extends AbstractComputeAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -959548414464618085L;
	
	private BinOperator<T> operator;
	
	public ScanAgent(BinOperator<T> operator){
		this.operator = operator;
	}
	
	@Override
	protected Object execute() throws Exception {
		int rank = task.getIntParameter(0);
		String inputName = task.getStringParameter(1);
		String outputName = task.getStringParameter(2);
		Vector<T> input = 
			computationCase.getData(inputName) ;
		Vector<T> output = 
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
