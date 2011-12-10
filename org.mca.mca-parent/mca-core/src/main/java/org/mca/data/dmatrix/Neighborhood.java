package org.mca.data.dmatrix;



public class Neighborhood{
	
	private SubMatrix north;
	private SubMatrix south;
	private SubMatrix east;
	private SubMatrix west;

	public SubMatrix getNorth() {
		return north;
	}
	public SubMatrix getSouth() {
		return south;
	}
	public SubMatrix getEast() {
		return east;
	}
	public SubMatrix getWest() {
		return west;
	}
	public void setNorth(SubMatrix north) {
		this.north = north;
	}
	public void setSouth(SubMatrix south) {
		this.south = south;
	}
	public void setEast(SubMatrix east) {
		this.east = east;
	}
	public void setWest(SubMatrix west) {
		this.west = west;
	}
	
	@Override
	public String toString() {
		return "Neighborhood [north=" + north + ", south=" + south + ", east="
				+ east + ", west=" + west + "]";
	}
	
}