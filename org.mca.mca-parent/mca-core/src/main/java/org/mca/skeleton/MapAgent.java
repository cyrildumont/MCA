package org.mca.skeleton;

import java.util.List;

import org.mca.math.DistributedVector;
import org.mca.math.Element;
import org.mca.math.SubVector;


/**
 * 
 * @author Cyril Dumont
 *
 */
public class MapAgent<T,S> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;
	
	private Function<T,S> f;
	
	private String input;
	
	private String output;
	
	private SubVector<T> inputPart;
	private SubVector<S> outputPart;
	
	
	public MapAgent(Function<T, S> f, String input, String output) {
		this.f = f;
		this.input = input;
		this.output = output;
	}

	@Override
	protected Object executeSkel() throws Exception {
		DistributedVector<T> inputvector = computationCase.<DistributedVector<T>>getData(input);
		inputPart = (SubVector<T>)inputvector.load(rank);
		List<T> values = (List<T>)inputPart.getValues();
		DistributedVector<S> outputVector = computationCase.<DistributedVector<S>>getData(output);
		outputPart = (SubVector<S>)outputVector.load(rank);
		for(int i=0;i < values.size();i++){
			outputPart.add(f.execute(values.get(i)));
		}
		outputVector.unload();
		return null;
	}
}
