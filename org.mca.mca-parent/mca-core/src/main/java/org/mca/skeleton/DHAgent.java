package org.mca.skeleton;

public class DHAgent<X> extends SkeletonAgent {

	private static final long serialVersionUID = 1L;

	private BinOperator<X> oplus;
	
	private BinOperator<X> otimes;
	
	private String input;
	
	private String output;
	
	public DHAgent(BinOperator<X> oplus, BinOperator<X> otimes, String input, String output) {
	}
	
	@Override
	protected Object executeSkel() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
