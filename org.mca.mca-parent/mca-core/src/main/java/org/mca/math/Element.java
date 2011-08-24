package org.mca.math;

import java.io.Serializable;

public class Element<T> implements Serializable{

	private T value;
	private int index;
	private int vectorSize;
	
	public Element(T value){
		this(value,0,0);
	}

	public Element(T value, int index) {
		this(value, index,0);
	}
	
	public Element(T value, int index, int vectorSize) {
		this.value = value;
		this.index = index;
		this.vectorSize = vectorSize;
	}
	

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getVectorSize() {
		return vectorSize;
	}
	
	public void setVectorSize(int vectorSize) {
		this.vectorSize = vectorSize;
	}
	
}
