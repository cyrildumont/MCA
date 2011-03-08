package org.mca.math;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class SubVectorImpl<E> implements SubVector<E> {

	private List<E> data;

	public SubVectorImpl() {
		data = new ArrayList<E>();
	}

	public SubVectorImpl(List<E> data) {
		this.data = data;
	}

	@Override
	public E get(int index) throws RemoteException {
		return data.get(index);
	}

	@Override
	public void set(int index, E value) throws RemoteException {
		data.set(index, value);
	}

	public void add(E e){
		data.add(e);
	}


	@Override
	public Iterator<E> iterator() {
		return new SubVectorIterator();
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
			if (count < data.size()){
				return true;
			}  
			return false;
		}

		@Override
		public E next() {
			if (count == data.size())
				throw new NoSuchElementException();
			return data.get(count++);
		}

	}

}
