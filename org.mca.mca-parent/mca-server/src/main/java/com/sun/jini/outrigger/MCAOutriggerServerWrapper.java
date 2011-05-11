package com.sun.jini.outrigger;

import java.io.IOException;
import java.rmi.MarshalException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.security.auth.login.LoginException;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.space.AvailabilityEvent;
import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.core.ComponentInfo;
import org.mca.entry.Barrier;
import org.mca.javaspace.exceptions.NoDuplicatedTaskException;
import org.mca.listener.CaseActionType;
import org.mca.listener.TaskListener;
import org.mca.log.LogUtil;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

import com.sun.jini.start.LifeCycle;

public abstract class MCAOutriggerServerWrapper extends OutriggerServerWrapper 
												implements NotificationBroadcaster,MCAOutriggerServerWrapperMBean {

	/** Log */
	private final static Log LOG = LogFactory.getLog(MCAOutriggerServerWrapper.class);	

	private ArrayList<String> tasksWaitForANotherTask;

	private HashMap<String, Integer> barriers;

	private TaskListener taskListener;
	
	protected NotificationBroadcasterSupport nbs;
	
	private int numSeq;
	
	MCAOutriggerServerWrapper(String[] configArgs, LifeCycle lifeCycle, boolean persistent)
		throws IOException, ConfigurationException, LoginException {
		super(configArgs, lifeCycle, persistent);
		allowCalls();
		tasksWaitForANotherTask = new ArrayList<String>();
		barriers = new HashMap<String, Integer>();
		nbs = new NotificationBroadcasterSupport();
	}

	@Override
	public void addLookupAttributes(Entry[] attrSets) throws RemoteException {
		super.addLookupAttributes(attrSets);
	}
	
	@Override
	public Object read(EntryRep tmpl, Transaction txn, long timeout,
			QueryCookie cookie) throws TransactionException, RemoteException,
			InterruptedException {
		if (LOG.isDebugEnabled()) {
			try {
				Entry ew = tmpl.entry();
				if (ew instanceof Task) {
					LogUtil.debug("READ a Task : " + ew, getClass());
				}
				LogUtil.debug("\t -->Transaction : " + txn, getClass());
			} catch (UnusableEntryException e) {
				LOG.error(e);
			}
		}
		return super.read(tmpl, txn, timeout, cookie);
	}

	@Override
	public Object take(EntryRep tmpl, Transaction txn, long timeout,
			QueryCookie cookie) throws TransactionException, RemoteException,
			InterruptedException {

		if (LOG.isDebugEnabled()) {
			try {
				Entry ew = tmpl.entry();
				if (ew instanceof Task) {
					LogUtil.debug("TAKE a Task : " + ew, getClass());
					LogUtil.debug("\t -->Transaction : " + txn, getClass());
					Notification n = new Notification(CaseActionType.REMOVE_TASK.toString(), ew, ++numSeq);
					nbs.sendNotification(n);
				}
			} catch (UnusableEntryException e) {
				LOG.error(e);
				throw new RemoteException();
			}
		}
		return super.take(tmpl, txn, timeout, cookie);
	}

	@Override
	public long[] write(EntryRep entry, Transaction txn, long lease)
	throws TransactionException, RemoteException {
		try {
			Entry ew = entry.entry();
			if (ew instanceof Task) {
				LogUtil.debug("WRITE a Task : " + ew, getClass());
				Task task = (Task)ew;
				checkTask(task, txn);
				Notification n = new Notification(CaseActionType.ADD_TASK.toString(), task, ++numSeq);
				nbs.sendNotification(n);
			}else if(ew instanceof Barrier){
				LogUtil.debug("WRITE a Barrier : " + ew, getClass());
				Barrier barrier = (Barrier)ew;
				String name = barrier.getName();
				Integer total = barrier.getCounter();
				Integer nb = barriers.get(name);
				if (nb != null)	
					barriers.put(name, ++nb);
				else 
					barriers.put(name, 1);

				entry = new EntryRep(new Barrier(name, 0));
				if(barriers.get(name).intValue() == total){
					Barrier b = new Barrier(name, total);
					EntryRep er = new EntryRep(b);
					super.write(er, txn, lease);
				}
			}
			return super.write(entry, txn, lease);
		}catch (NoDuplicatedTaskException e) {
			throw e;
		} catch (UnusableEntryException e) {
			throw new RemoteException();
		}
		
	}

	@Override
	public EventRegistration registerForAvailabilityEvent(EntryRep[] tmpls,
			Transaction txn, boolean visibilityOnly,
			RemoteEventListener listener, long leaseTime,
			MarshalledObject handback) throws TransactionException,
			RemoteException {
		if (LOG.isTraceEnabled()) {
			try {
				LOG.trace("RegisterForAvailabilityEvent");
				LOG.trace("\t -- Entries : " );
				if (tmpls != null) {
					for (EntryRep entryRep : tmpls) {
						LOG.trace("\t \t -- Entry : " + entryRep.entry());
					}
				}
				LOG.trace("\t -- Transaction : " + txn);
				LOG.trace("\t -- Timeout : " + leaseTime);
				LOG.trace("\t -- Listener : " + listener);
				LOG.trace("\t -- Mobject : " + handback);
			} catch (UnusableEntryException e) {
				e.printStackTrace();
			}
		}
		try {
			if (handback != null) {
				ComponentInfo component = (ComponentInfo)handback.get();
				taskListener.addListener(component);
				return null;
			}
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;	
		}
		return super.registerForAvailabilityEvent(tmpls, txn, visibilityOnly, listener,
				leaseTime, handback);
	}


	/**
	 * 
	 * @param task
	 * @throws RemoteException
	 */
	private void checkTask(Task task, Transaction tnx) throws RemoteException {

		Task tmpl = new Task();
		tmpl.name = task.name;

		try {
			Entry ent = space().read(tmpl, tnx, JavaSpace.NO_WAIT);
			if (ent != null){
				throw new NoDuplicatedTaskException();
			}
		} catch (TransactionException e) {
			throw new RemoteException();
		} catch (InterruptedException e) {
			throw new RemoteException();
		} catch (UnusableEntryException e) {
			throw new RemoteException();
		}


		TaskState state = task.state;
		if (state == null) {
			throw new RemoteException("the state of the task is null");
		}else if (task.name == null || task.name.equals("")) {
			throw new RemoteException("the name of the task is null");
		}

		switch (state) {
		case WAIT_FOR_ANOTHER_TASK:
			if (!tasksWaitForANotherTask.contains(task.name)) {
				addTaskWaitForAnotherTask(task);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @param task
	 * @throws RemoteException
	 */
	private void addTaskWaitForAnotherTask(Task task) throws RemoteException{
		LogUtil.debug("Checking parent tasks for " + task.name, getClass());
		if (task.parentTasks == null) {
			throw new RemoteException("A task in state WAIT_FOR_ANOTHER_TASK must have parent Task.");
		}
		for (String name : task.parentTasks) {
			LogUtil.debug(name, getClass());
			Task t = new Task();
			t.name = name;
			try {
				Object o = space().read(t, null, JavaSpace05.NO_WAIT);
				if (o == null) {
					throw new RemoteException("Task [" + name + "] is not exist");
				}
			} catch (UnusableEntryException e) {
				e.printStackTrace();
			} catch (TransactionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		AnotherTaskListener listener = new AnotherTaskListener(task);
	}

	/**
	 * 
	 * @author Cyril Dumont
	 *
	 */
	public class AnotherTaskListener implements RemoteEventListener{

		private Task task;

		/**
		 * 
		 * @param task
		 */
		public AnotherTaskListener(Task task) {
			LogUtil.debug("Creation of e new AnotherTaskListener for " + task.name, getClass());
			tasksWaitForANotherTask.add(task.name);
			this.task = task;
			try {
				listen();
				checkTasks();
			} catch (MarshalException e) {
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (TransactionException e) {
				e.printStackTrace();
			} catch (UnusableEntryException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void checkTasks() throws RemoteException, UnusableEntryException, TransactionException, InterruptedException {
			ArrayList<String> tasks = task.parentTasks;
			for (String name : tasks) {
				Task task = new Task();
				task.name = name;
				task.state = TaskState.COMPUTED;
				if (space().read(task, null, JavaSpace05.NO_WAIT) != null ) {
					taskComputed(name);
				}
			}
		}

		/**
		 * 
		 * @throws MarshalException
		 * @throws TransactionException
		 * @throws RemoteException
		 */
		private void listen() throws MarshalException, TransactionException,
		RemoteException {
			ArrayList<String> tasks = task.parentTasks;
			EntryRep entries[] = new EntryRep[tasks.size()];
			int i = 0;
			for (String name : tasks) {
				Task task = new Task();
				task.name = name;
				task.state = TaskState.COMPUTED;
				entries[i++] = new EntryRep(task);		
			}
			registerForAvailabilityEvent(entries, null, true, this, Long.MAX_VALUE, null);
		}

		/**
		 * 
		 * @see net.jini.core.event.RemoteEventListener#notify(net.jini.core.event.RemoteEvent)
		 */
		public void notify(RemoteEvent event) throws UnknownEventException,
		RemoteException {
			AvailabilityEvent availabilityEvent = (AvailabilityEvent)event;

			try {
				Task task = (Task)availabilityEvent.getEntry();
				taskComputed(task.name);
			} catch (UnusableEntryException e) {
				LogUtil.error(e.getMessage(), getClass());
				e.printStackTrace();
			}
		}

		/**
		 * 
		 * @param name
		 */
		private void taskComputed(String name) {
			LogUtil.debug("The task [" + name + "] is COMPUTED.", getClass());
			Task tmpl = new Task();
			tmpl.name = task.name;
			try {
				Task t = (Task)space().take(tmpl, null, 10000l);
				if (t != null ) {
					LogUtil.debug("remove parent task [" + name + "] for the task [" + task.name +"].", getClass());
					t.removeParentTask(name);
					if (t.parentTasks == null) {
						t.setState(TaskState.WAIT_FOR_COMPUTE);
						tasksWaitForANotherTask.remove(task.name);
					}
					space().write(t, null, Long.MAX_VALUE);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (UnusableEntryException e) {
				e.printStackTrace();
			} catch (TransactionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws IllegalArgumentException {
		nbs.addNotificationListener(listener, filter, handback);
	}

	@Override
	public void removeNotificationListener(NotificationListener listener)
			throws ListenerNotFoundException {
		nbs.removeNotificationListener(listener);
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return nbs.getNotificationInfo();
	}
	
}
