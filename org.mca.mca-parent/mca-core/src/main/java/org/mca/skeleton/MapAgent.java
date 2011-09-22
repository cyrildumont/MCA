package org.mca.skeleton;

import java.util.List;

import org.mca.math.DistributedVector;
import org.mca.math.SubVector;


/**
 * 
 * @author Cyril Dumont
 *
 */
public class MapAgent<X,Y> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;
	
	private Function<X,Y> f;
	
	private String input;
	
	private String output;
	
	private SubVector<X> inputPart;
	private SubVector<Y> outputPart;
	
	
	public MapAgent(Function<X, Y> f, String input, String output) {
		this.f = f;
		this.input = input;
		this.output = output;
	}

	@Override
	protected Object executeSkel() throws Exception {
		DistributedVector<X> inputvector = computationCase.<DistributedVector<X>>getData(input);
		inputPart = (SubVector<X>)inputvector.load(rank);
		List<X> values = (List<X>)inputPart.getValues();
		DistributedVector<Y> outputVector = computationCase.<DistributedVector<Y>>getData(output);
		outputPart = (SubVector<Y>)outputVector.load(rank);
		for(int i=0;i < values.size();i++){
			outputPart.add(f.execute(values.get(i)));
		}
		outputVector.unload();
		return null;
	}
}
