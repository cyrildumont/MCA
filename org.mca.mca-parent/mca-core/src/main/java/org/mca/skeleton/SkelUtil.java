package org.mca.skeleton;

import java.util.List;

import org.mca.math.DistributedVector;

public abstract class SkelUtil {

	protected <T> List<T> repl(T value, int n, String ouput){
		return null;
	}
	
	protected void scan(){
		
	}

	protected <T> T mapidx(String input, String output, BinOperator<T> oplus){
		
		return null;
	}
	
	protected <T> T reduce(String input, String output, BinOperator<T> oplus){
		
		return null;
	}

	protected <T> T scan(String input, String output, BinOperator<T> oplus){
		return null;
	}

	protected <T,S> T apply(String input, String output, Function<T,S> oplus){
		
		return null;
	}	
	
	protected <T> T dh(String input, String output, BinOperator<T> oplus, BinOperator<T> otimes){
		
		return null;
	}
}
