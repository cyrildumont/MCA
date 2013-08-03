package org.mca.data.dmatrix;



public class Neighborhood{
	
	private DMatrixPart north;
	private DMatrixPart south;
	private DMatrixPart east;
	private DMatrixPart west;

	public DMatrixPart getNorth() {
		return north;
	}
	public DMatrixPart getSouth() {
		return south;
	}
	public DMatrixPart getEast() {
		return east;
	}
	public DMatrixPart getWest() {
		return west;
	}
	public void setNorth(DMatrixPart north) {
		this.north = north;
	}
	public void setSouth(DMatrixPart south) {
		this.south = south;
	}
	public void setEast(DMatrixPart east) {
		this.east = east;
	}
	public void setWest(DMatrixPart west) {
		this.west = west;
	}
	
	@Override
	public String toString() {
		return "Neighborhood [north=" + north + ", south=" + south + ", east="
				+ east + ", west=" + west + "]";
	}
	
}