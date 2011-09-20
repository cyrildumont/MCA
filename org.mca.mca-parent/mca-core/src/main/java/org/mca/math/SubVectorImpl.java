package org.mca.math;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class SubVectorImpl<E> implements SubVector<E> {

	private List<E> values;

	public SubVectorImpl(E[] values) {
		this.values = Arrays.asList(values);
	}
	
	public SubVectorImpl(List<E> values) {
		this.values = values;
	}
	
	public SubVectorImpl() {
		this.values = new ArrayList<E>();
	}

	@Override
	public E get(int index) throws RemoteException {
		return values.get(index);
	}

	@Override
	public void set(int index, E value) throws RemoteException {
		values.set(index, value);
	}

	public void add(E value) throws RemoteException {
		values.add(value);
	}
	
	@Override
	public int size() throws RemoteException {
		return values.size();
	}

	@Override
	public Iterator<E> iterator() {
		return new SubVectorIterator();
	}

	@Override
	public Object getValues() throws RemoteException {
		return values;
	}

	/**
	 * 
	 * @author cyril
	 *
	 * @param <E>
	 */
	private final class SubVectorIterator implements Iterator<E>{

		private int count;

		@Override
		public boolean hasNext() {
			if (count < values.size()){
				return true;
			}  
			return false;
		}

		@Override
		public E next() {
			if (count == values.size())
				throw new NoSuchElementException();
			return values.get(count++);
		}

	}

}
