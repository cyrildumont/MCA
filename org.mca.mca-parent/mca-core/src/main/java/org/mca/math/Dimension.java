package org.mca.math;

public class Dimension {

	/** Width of the submatrix */
	public Integer width;

	/** Height of the submatrix */
	public Integer height;
	
	public Dimension() {
	}

	public Dimension(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	
}
