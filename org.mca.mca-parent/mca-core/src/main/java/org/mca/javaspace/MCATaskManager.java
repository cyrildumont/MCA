package org.mca.javaspace;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import com.sun.jini.thread.TaskManager;

public class MCATaskManager extends TaskManager{

	@Override
	public synchronized void add(Task task) {
		Class taskClass = task.getClass();
		try {
			Field field = taskClass.getDeclaredField("sender");
			field.setAccessible(true);
			System.out.println(field.get(task));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.add(task);
	}

	@Override
	public synchronized void addAll(Collection arg0) {
		// TODO Auto-generated method stub
		super.addAll(arg0);
	}

	@Override
	public synchronized void addIfNew(Task t) {
		// TODO Auto-generated method stub
		super.addIfNew(t);
	}

	@Override
	public int getMaxThreads() {
		// TODO Auto-generated method stub
		return super.getMaxThreads();
	}

	@Override
	public synchronized ArrayList getPending() {
		// TODO Auto-generated method stub
		return super.getPending();
	}

	@Override
	protected boolean needThread() {
		// TODO Auto-generated method stub
		return super.needThread();
	}

	@Override
	public synchronized boolean remove(Task t) {
		// TODO Auto-generated method stub
		return super.remove(t);
	}

	@Override
	public synchronized boolean removeIfPending(Task t) {
		// TODO Auto-generated method stub
		return super.removeIfPending(t);
	}

	@Override
	public synchronized void terminate() {
		// TODO Auto-generated method stub
		super.terminate();
	}

}
