package org.mca.ext.skeleton;

import java.util.List;

import org.mca.math.DistributedVector;
import org.mca.math.SubVector;


/**
 * 
 * @author Cyril Dumont
 *
 */
public class MapIdxAgent<Y,Z> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;
	
	private BinFunction<Integer,Y,Z> g;
	
	private String input;
	
	private String output;
	
	private SubVector<Y> inputPart;
	private SubVector<Z> outputPart;
	
	
	public MapIdxAgent(BinFunction<Integer,Y,Z> g, String input, String output) {
		this.g = g;
		this.input = input;
		this.output = output;
	}

	@Override
	protected Object executeSkel() throws Exception {
		DistributedVector<Y> inputvector = computationCase.<DistributedVector<Y>>getData(input);
		inputPart = (SubVector<Y>)inputvector.load(rank);
		int index = (rank - 1) * inputvector.getPartSize() ;
		List<Y> values = (List<Y>)inputPart.getValues();
		DistributedVector<Z> outputVector = computationCase.<DistributedVector<Z>>getData(output);
		outputPart = (SubVector<Z>)outputVector.load(rank);
		for(int i=0;i < values.size();i++){
			outputPart.add(g.execute(index++,values.get(i)));
		}
		outputVector.unload();
		return null;
	}
}
