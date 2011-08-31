package org.mca.math;

public class Neighborhood<E>{
	private SubMatrix<E> north;
	private SubMatrix<E> south;
	private SubMatrix<E> east;
	private SubMatrix<E> west;

	public SubMatrix<E> getNorth() {
		return north;
	}
	public SubMatrix<E> getSouth() {
		return south;
	}
	public SubMatrix<E> getEast() {
		return east;
	}
	public SubMatrix<E> getWest() {
		return west;
	}
	public void setNorth(SubMatrix<E> north) {
		this.north = north;
	}
	public void setSouth(SubMatrix<E> south) {
		this.south = south;
	}
	public void setEast(SubMatrix<E> east) {
		this.east = east;
	}
	public void setWest(SubMatrix<E> west) {
		this.west = west;
	}
}