package org.mca.math;

import java.rmi.RemoteException;
import java.util.NoSuchElementException;

public class SubVectorImpl<E> implements SubVector<E> {

	private E[] values;

	public SubVectorImpl(E[] values) {
		this.values = values;
	}

	@Override
	public E get(int index) throws RemoteException {
		return values[index];
	}

	@Override
	public void set(int index, E value) throws RemoteException {
		values[index] = value;
	}

	@Override
	public int size() throws RemoteException {
		return values.length;
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
			if (count < values.length){
				return true;
			}  
			return false;
		}

		@Override
		public E next() {
			if (count == values.length)
				throw new NoSuchElementException();
			return values[count++];
		}

	}

}
