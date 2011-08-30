package org.mca.agent.skel;

import org.mca.agent.AbstractComputeAgent;
import org.mca.math.Element;
import org.mca.math.SubVector;
import org.mca.math.DistributedVector;


/**
 * 
 * @author cyril
 *
 */
public class MapAgent<T,S> extends AbstractComputeAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5918264376865000141L;
	
	private Function<T,S> f;
	
	public MapAgent(Function<T,S> f){
		this.f = f;
	}
	
	
	@Override
	protected Object execute() throws Exception {
		int rank = task.getIntParameter(0);
		String inputName = task.getStringParameter(1);
		String outputName = task.getStringParameter(2);
		DistributedVector<T> input = computationCase.getData(inputName);
		DistributedVector<S> output = computationCase.getData(outputName);
		SubVector<T> localInput = (SubVector<T>)input.load(rank);
		SubVector<S> localOutput = (SubVector<S>)output.load(rank);
		for(int i=0;i < localInput.size();i++){
			S result = f.execute(localInput.get(i));
			localOutput.set(i, result);
		}
		return null;
	}
}
