package org.mca.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import net.jini.admin.Administrable;
import net.jini.admin.JoinAdmin;
import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.transaction.Transaction.Created;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.ssl.SslServerEndpoint;
import net.jini.lease.LeaseListener;
import net.jini.lease.LeaseRenewalEvent;
import net.jini.lease.LeaseRenewalManager;
import net.jini.space.AvailabilityEvent;
import net.jini.space.JavaSpace05;

import org.mca.entry.Barrier;
import org.mca.entry.ComputationCaseState;
import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.entry.Property;
import org.mca.entry.State;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.ComputationCaseInfo;
import org.mca.javaspace.ComputationCaseListener;
import org.mca.javaspace.JavaSpaceParticipant;
import org.mca.javaspace.exceptions.EntryNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.listener.TaskListener;
import org.mca.math.DistributedData;
import org.mca.scheduler.RecoveryTaskStrategy;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.mca.transaction.MCATransactionException;
import org.mca.transaction.Transaction;
import org.mca.util.MCAUtils;


class ComputationCaseImpl extends JavaSpaceParticipant implements ComputationCase, RemoteEventListener{ 

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.ComputationCase";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private Transaction currentTransaction;

	private transient LeaseRenewalManager leaseManager;

	private RecoveryTaskStrategy recoveryTaskStrategy;

	private static final int LEASE_DURATION = 8000;

	private String name;
	private String description;
	private TransactionManager transactionManager;

	private ComputationCaseState state;

	private List<ComputationCaseListener> listeners = new ArrayList<ComputationCaseListener>();

	ComputationCaseImpl(RecoveryTaskStrategy strategy,
			JavaSpace05 space, TransactionManager transactionManager) throws RemoteException {	
		recoveryTaskStrategy = strategy;
		setSpace(space);
		init(space);
		this.transactionManager = transactionManager;
		State s = new State();
		try {
			s = (State)readEntry(s, null);
			state = s.state;
		} catch (EntryNotFoundException e) {
			throw new RemoteException();
		}
	}

	/**
	 * 
	 * @param space
	 * @throws RemoteException
	 */
	private void init(JavaSpace05 space) throws RemoteException{
		Administrable admin = (Administrable) space;
		JoinAdmin jadmin = (JoinAdmin)admin.getAdmin();
		Entry[] entries = jadmin.getLookupAttributes();
		for (Entry entry : entries) {
			if(entry instanceof ComputationCaseInfo){
				ComputationCaseInfo info = (ComputationCaseInfo) entry;
				name = info.name;
				description = info.description;
				return;
			}
		}
	}

	@Override
	public void addProperty(Property property) throws MCASpaceException {
		writeEntry(property,  null);
		logger.finest("Property [" + property.name + " : " + property.value + "] added.");
	}

	@Override
	public File download(String name, String dir)
	throws MCASpaceException {
		DataHandler dh = getDataHandler(name);
		if(dh == null)
			throw new MCASpaceException("DataHandler [" + name + "] not found");
		DownloadTask task = new DownloadTask(dh, dir);
		task.start();
		try {
			task.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		File file = task.getFile();
		if (file == null)
			throw new MCASpaceException("Error during download of data [" + name + "]");
		return file;

	}

	@Override
	public String getName(){
		return this.name;
	}

	@Override
	public Collection<Property> getProperties() throws MCASpaceException {
		Collection<Entry> templates = new ArrayList<Entry>();
		templates.add(new Property());
		Collection<Entry> result = readEntry(templates,null);
		Collection<Property> properties = new ArrayList<Property>();
		for (Entry entry : result) {
			properties.add((Property)entry);
		}
		return properties;
	}

	@Override
	public void addTask(Task task) throws MCASpaceException {
		writeEntry(task,currentTransaction != null ? currentTransaction.getTransaction() : null);
		logger.fine("ComputationCaseImpl -- [" + this.name + "] Task [" + task.name + "] added : " + task.toString());
	}

	@Override
	public void addData(DistributedData<?> data, String name, DataHandlerFactory factory) throws MCASpaceException {
		data.setName(name);
		data.deploy(this, factory);
		writeEntry(data, null);
		logger.fine("ComputationCaseImpl -- [" + this.name + "] DistributedData [name = " + name +"]" +
				"[class=" + data.getClass().getName() + "] added.");
	}

	@Override
	public void addDataHandler(DataHandler entry) throws MCASpaceException {
		writeEntry(entry,null);
		logger.fine("ComputationCaseImpl -- [" + this.name + "] DataHandler [ name = " + entry.name +" ] added.");
	}

	@Override
	public DataHandler removeDataHandler(String name) throws MCASpaceException {
		DataHandler template = new DataHandler();
		template.name = name;
		DataHandler handlerReturned = (DataHandler)takeEntry(template,null);
		logger.fine("ComputationCaseImpl -- [" + this.name + "] DataHandler [ name = " + name +" ] removed.");
		return handlerReturned;
	}

	@Override
	public DataHandler getDataHandler(String name) throws MCASpaceException {
		DataHandler dataTemplate = new DataHandler();
		dataTemplate.name = name;
		try {
			return (DataHandler)readEntry(dataTemplate, null);
		} catch (EntryNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void upload(String name, InputStream input)
	throws MCASpaceException {
		DataHandler dh = getDataHandler(name);
		if(dh == null)
			throw new MCASpaceException("DataHandler [" + name + "] not found");
		try {
			dh.upload(input);
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASpaceException("Error during upload of data [" + name + "]");
		}
	}

	@Override
	public void start() throws MCASpaceException {
		if (state == ComputationCaseState.STARTED)
			logger.warning("ComputationCaseImpl -- Computation Case is already started");
		else
			updateState(ComputationCaseState.STARTED);
	}

	@Override
	public void stop() throws MCASpaceException {
		if (state == ComputationCaseState.PAUSED || state == ComputationCaseState.FINISHED)
			logger.warning("ComputationCaseImpl -- Computation Case isn't started");
		else
			updateState(ComputationCaseState.PAUSED);
	}

	@Override
	public void finish() throws MCASpaceException {
		if (state == ComputationCaseState.FINISHED)
			logger.warning("ComputationCaseImpl -- Computation Case is already finished");
		else
			updateState(ComputationCaseState.FINISHED);
	}

	private void updateState(ComputationCaseState state) throws MCASpaceException{
		State template = new State();
		try {
			Created created = TransactionFactory.create(transactionManager, LEASE_DURATION);
			net.jini.core.transaction.Transaction t = created.transaction;
			template = (State)takeEntry(template, t,Long.MAX_VALUE);
			template.state = state;
			writeEntry(template, t);
			t.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}
	}

	private void setState(ComputationCaseState state) {
		logger.fine(
				"ComputationCaseImpl -- State updated : [" + this.state + "] --> [" + state + "]");
		this.state = state;
		for (ComputationCaseListener listener : listeners) {
			switch (state) {
			case STARTED:
				listener.caseStart();break;
			case PAUSED:
				listener.caseStop();break;
			case FINISHED:
				listener.caseFinish();break;
			}	
		}
	}

	@Override
	public ComputationCaseState getState() throws MCASpaceException {
		return state;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public void updateTask(Task task) throws MCASpaceException {
		getTask(task.name);
		addTask(task);
	}

	@Override
	public Task getTask(TaskState state) throws MCASpaceException {
		return getTask(state, null);
	}

	@Override
	public Collection<Task<?>> getTasks(TaskState state, int maxTasks)
	throws MCASpaceException {
		Task template = new Task();
		template.state = state;
		return getTasks(template, maxTasks);
	}

	@Override
	public Collection<Task<?>> getTasks(Task template, int maxTasks)
	throws MCASpaceException {
		logger.fine("ComputationCaseImpl -- [" + this.name + "] getTasks [ template = " + template +" ][maxTasks = " + maxTasks + "]");
		return (Collection<Task<?>>)takeEntries(
				Collections.singletonList(template), currentTransaction.getTransaction(), maxTasks);
	}
	
	/**
	 * 
	 * @param state
	 * @param txn
	 * @return
	 * @throws MCASpaceException
	 */
	private Task getTask(TaskState state, Transaction txn)
	throws MCASpaceException{
		Task taskTemplate = new Task();
		taskTemplate.state = state;
		return (Task)takeEntry(taskTemplate,txn.getTransaction());	
	}

	@Override
	public Task<?> getTask(Task<?> template) throws MCASpaceException {
		return (Task<?>)takeEntry(template, currentTransaction.getTransaction());
	}

	@Override
	public void join(ComputationCaseListener listener) throws MCASpaceException {
		State state = new State();
		Collection<Entry> entries = new ArrayList<Entry>();
		entries.add(state);
		try {		
			Exporter exporter = 
				new BasicJeriExporter(SslServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
			RemoteEventListener proxy = (RemoteEventListener)exporter.export(this);
			space.registerForAvailabilityEvent(entries, null, true, proxy, Long.MAX_VALUE, null);
			listeners.add(listener);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TransactionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + "[name=" + name + ", description=" + description + "]";
	}

	@Override
	public Task getTask(String name) throws MCASpaceException {
		Task taskTemplate = new Task();
		taskTemplate.name = name;
		return (Task)takeEntry(taskTemplate, null);
	}


	@Override
	public Collection<? extends Task<?>> getTaskToCompute(String hostname, int maxTasks)
			throws MCASpaceException {
		if(!isRunning())
			throw new MCASpaceException("Case isn't started");
		try {
			Created created = TransactionFactory.create(transactionManager, LEASE_DURATION);
			currentTransaction = new Transaction(created);

			if(leaseManager == null) leaseManager = new LeaseRenewalManager();
			leaseManager.renewFor(created.lease,
					Long.MAX_VALUE, LEASE_DURATION, new TaskLeaseListener());
			Collection<? extends Task<?>> taskToCompute = 
				recoveryTaskStrategy.recoverTasksToCompute(this);

			if (taskToCompute.isEmpty()){
				leaseManager.cancel(created.lease);
				return null;
			}
			for (Task<?> task : taskToCompute) {
				task.setState(TaskState.ON_COMPUTING);
				task.worker = hostname;
			}
			List<Task<?>> list = new ArrayList<Task<?>>(taskToCompute);
			writeEntries(list,created.transaction);
			return taskToCompute;
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}
	}

	private boolean isRunning() {
		return state == ComputationCaseState.STARTED;
	}

	@Override
	public void updateTaskComputed(List<Task<?>> tasksToUpdate) throws MCASpaceException {
		if (currentTransaction == null) {
			logger.warning("ComputationCaseImpl -- no task in progress");
			throw new MCASpaceException();
		}
		try {
			String localIP = MCAUtils.getIP();
			List<Task> templates = new ArrayList<Task>();
			for (Task task : tasksToUpdate) {
				Task template = new Task();
				template.name = task.name;
				template.worker = localIP;
				template.state = TaskState.ON_COMPUTING;
				templates.add(template);
			}
			Collection tasksFinded = 
				takeEntries(templates, currentTransaction.getTransaction(), templates.size());
			if (tasksFinded.size() != tasksToUpdate.size()) {
				logger.warning("local number of tasks to update [" + tasksToUpdate.size() + "]" +
						" is different of number of remote tasks to update [" + tasksFinded.size() + "]");
				currentTransaction.abort();
				logger.warning("current transaction aborted");
			}else{
				writeEntries(tasksToUpdate, currentTransaction.getTransaction());
				currentTransaction.commit();
				logger.fine("ComputationCaseImpl -- [" + this.name + "] " + tasksToUpdate.size() + " tasks updated.");	
			}
		} catch (MCATransactionException e) {
			logger.warning("ComputationCaseImpl -- transaction can't be committed : " + e.getMessage());
			logger.throwing("ComputationCaseImpl", "updateTaskComputed", e);
			throw new MCASpaceException(e.getMessage());
		}finally{
			try {
				leaseManager.remove(currentTransaction.getLease());
			} catch (UnknownLeaseException e) {
				logger.warning("UnknownLeaseException :" + e.getMessage());
				logger.throwing("ComputationCaseImpl", "updateTaskComputed", e);
			}
		}
		
	}

	@Override
	public void barrier(String name, int nbWorker) throws MCASpaceException {
		Barrier barrier = new Barrier(name, null);
		barrier = (Barrier)takeEntry(barrier, null, Long.MAX_VALUE);	
		barrier.increment();
		writeEntry(barrier, null);
		logger.fine("ComputationCaseImpl -- [" + this.name + "] Barrier [" + name + "] waiting ...");
		try {
			barrier.counter = nbWorker;
			readEntry(barrier, null, Lease.FOREVER);
		} catch (EntryNotFoundException e) {
			e.printStackTrace();
		}
		logger.fine("ComputationCaseImpl -- [" + this.name + "] Barrier [" + name + "] OK");	
	}

	@Override
	public void createBarrier(String name) throws MCASpaceException {
		Barrier barrier = new Barrier(name, 0);
		writeEntry(barrier, null);
		logger.fine("ComputationCaseImpl -- [" + this.name + "] Barrier [" + name + "] created");	
	}

	@Override
	public void removeBarrier(String name) throws MCASpaceException {
		Barrier barrier = new Barrier(name, null);
		takeEntry(barrier, null);
		logger.fine("ComputationCaseImpl -- [" + this.name + "] Barrier [" + name + "] removed	");	
	}

	@Override
	public <T extends DistributedData<?>> T getData(String name) throws MCASpaceException {
		DistributedData<?> template = new DistributedData(name);
		try {
			template = (T)readEntry(template, null);
			template.setComputationCase(this);
			return (T)template;
		} catch (EntryNotFoundException e) {
			return null;
		}
	}

	@Override
	public void notify(RemoteEvent event) throws UnknownEventException,
	RemoteException {
		AvailabilityEvent ae = (AvailabilityEvent)event;
		try {
			org.mca.entry.State entry = (org.mca.entry.State)ae.getEntry();
			ComputationCaseState state = entry.state;
			setState(state);
		} catch (UnusableEntryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerForTasks(Collection<Task> pendingTasks,
			TaskListener listener) throws MCASpaceException {
		Exporter exporter = 
			new BasicJeriExporter(SslServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
		try {
			RemoteEventListener proxy = (RemoteEventListener)exporter.export(listener);
			space.registerForAvailabilityEvent(pendingTasks, null, true, proxy, Lease.ANY,null);
			logger.fine("ComputationCaseImpl -- Registered to listen tasks activity [" + listener + "]");	
		} catch (Exception e) {
			logger.warning("ComputationCaseImpl -- " + e.getMessage());	
			e.printStackTrace();
			throw new MCASpaceException();	
		}
	}


	@Override
	public <R> R recoverResult() throws MCASpaceException {
		logger.fine("ComputationCaseImpl -- [" + this.name + "] waiting for a result ...");	
		try{
			Task<R> task = new Task<R>();
			task.state = TaskState.COMPUTED;
			task = (Task<R>)takeEntry(task, null,Long.MAX_VALUE);
			logger.fine("ComputationCaseImpl -- [" + this.name + "] a result recovered [task = " + task +"]");	
			task.state = TaskState.RECOVERED;
			writeEntry(task, null);
			return task.result;
		} catch (Exception e) {
			logger.warning("ComputationCaseImpl -- " + e.getMessage());	
			logger.throwing("ComputationCaseImpl", "recoverResult", e);
			throw new MCASpaceException();	
		}
	}


	/**
	 * 
	 * @author cyril
	 *
	 */
	class TaskLeaseListener implements LeaseListener{
		@Override
		public void notify(LeaseRenewalEvent event) {
			System.out.println(event);
		}
	}

	/**
	 * 
	 * @author Cyril Dumont
	 *
	 */
	class DownloadTask extends Thread{

		private File file;
		private DataHandler dh;
		private boolean downloaded = false;
		private String dir;

		public DownloadTask(DataHandler dh,  String dir) {
			this.dh = dh;
			this.dir = dir;
		}

		@Override
		public void run() {
			try {
				file = dh.download(dir);
			}catch (IOException e) {
				e.printStackTrace();
			}finally{
				downloaded = true;
			}

		}

		boolean isFileDownloaded(){
			return downloaded;
		}

		public File getFile() {
			return file;
		}
	}

}
