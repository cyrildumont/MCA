package org.mca.skeleton;

import org.mca.math.DistributedVector;
import org.mca.math.SubVector;


/**
 * 
 * @author Cyril Dumont
 *
 */
public class MapAgent<T,S> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;
	
	private Function<T,S> f;
	
	private MapConfig config;
	
	private DistributedVector<T> input;
	
	private DistributedVector<S> output;
	
	private SubVector<T> inputPart;
	private SubVector<S> outputPart;
	
	public MapAgent(MapConfig config){
		this.config = config;
	}
	
	@Override
	protected Object executeSkel() throws Exception {
		input = computationCase.<DistributedVector<T>>getData(config.getInput());
		inputPart = (SubVector<T>)input.load(rank);
		T[] values = (T[])inputPart.getValues();
		output = computationCase.<DistributedVector<S>>getData(config.getOutput());
		outputPart = (SubVector<S>)output.load(rank);
		for(int i=0;i < values.length;i++){
			outputPart.set(i,f.execute(values[i]));
		}
		output.unload();
		return null;
	}
}
