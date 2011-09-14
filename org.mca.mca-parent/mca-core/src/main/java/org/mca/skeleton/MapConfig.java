package org.mca.skeleton;

import java.io.Serializable;

/**
 * 
 * @author Cyril Dumont
 *
 */
public class MapConfig implements Serializable{

	private static final long serialVersionUID = 1L;

	private String name;
	private String input;
	private String output;
	private Function<?, ?> function;
	
	public MapConfig(String name, String input, String output,
			Function<?, ?> function) {
		this.name = name;
		this.input = input;
		this.output = output;
		this.function = function;
	}

	public String getName() {
		return name;
	}
	
	public String getInput() {
		return input;
	}
	
	public String getOutput() {
		return output;
	}
	
	public Function<?, ?> getFunction() {
		return function;
	}
	
}
