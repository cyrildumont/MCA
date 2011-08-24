package org.mca.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
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
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction.Created;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.ssl.SslServerEndpoint;
import net.jini.space.AvailabilityEvent;
import net.jini.space.JavaSpace05;

import org.mca.entry.ComputationCaseState;
import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.entry.Property;
import org.mca.entry.State;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.ComputationCaseInfo;
import org.mca.javaspace.ComputationCaseListener;
import org.mca.javaspace.exceptions.EntryNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.math.Data;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.mca.transaction.Transaction;
import org.mca.util.MCAUtils;


class ComputationCaseImpl extends JavaSpaceParticipant implements ComputationCase, RemoteEventListener{ 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3127097490816772998L;

	private static final String COMPONENT_NAME = "org.mca.ComputationCase";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private Task taskInProgress;
	private LeaseRenewalTask leaseRenewalTask;
	
	private static final int LEASE_DURATION = 7000;
	private static final int SLEEP_TIME = 6000;

	private String name;
	private String description;
	private TransactionManager transactionManager;
	
	private ComputationCaseState state;
	
	private List<ComputationCaseListener> listeners = new ArrayList<ComputationCaseListener>();
	
	ComputationCaseImpl(JavaSpace05 space, TransactionManager transactionManager) throws RemoteException {	
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
		Property template = new Property();
		template.name = property.name;
		takeEntry(template,null);
		logger.finest("MCAProperty [" + template.name + "] exists.");
		writeEntry(property,  null);
		logger.finest("MCAProperty [" + template.name + "] updated.");

	}

	@Override
	public File download(String name, String dir)
	throws MCASpaceException {
		DataHandler dh = getDataHandler(name);
		if(dh == null)
			throw new MCASpaceException("DataHandler [" + name + "] not found");
		try {
			File file = dh.download(dir);
			return file;
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASpaceException("Error during download of data [" + name + "]");
		}
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
		net.jini.core.transaction.Transaction txn = null;
		Transaction transaction = task.getTransaction();
		if (transaction != null) txn = transaction.getTransaction();
		writeEntry(task,txn);
	}

	@Override
	public void addData(Data<?> data, DataHandlerFactory factory) throws MCASpaceException {
		data.deploy(this, factory);
		writeEntry(data, null);
	}

	@Override
	public void addDataHandler(DataHandler entry) throws MCASpaceException {
		writeEntry(entry,null);
	}

	@Override
	public DataHandler removeDataHandler(String name) throws MCASpaceException {
		DataHandler template = new DataHandler();
		template.name = name;
		DataHandler handlerReturned = (DataHandler)takeEntry(template,null);
		logger.fine("[" + this.name + "] DataHandler [ name = " + name +" ] removed.");
		return handlerReturned;
	}

	@Override
	public DataHandler getDataHandler(String value) throws MCASpaceException {
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
			template = (State)takeEntry(template, t);
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
				"ComputationCaseImpl -- State changed : [" + this.state + "] --> [" + state + "]");
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
		LogUtil.debug("[" + this.name + "] Task [" + task.name + "] updated.", getClass());
	}

	@Override
	public Task getTask(TaskState state) throws MCASpaceException {
		return getTask(state, null);
	}
	
	/**
	 * 
	 * @param state
	 * @param txn
	 * @return
	 * @throws MCASpaceException
	 */
	private Task getTask(TaskState state, net.jini.core.transaction.Transaction txn)
		throws MCASpaceException{
		Task taskTemplate = new Task();
		taskTemplate.state = state;
		return (Task)takeEntry(taskTemplate,txn);	
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
	
	private Task getTask(String name, net.jini.core.transaction.Transaction txn) throws MCASpaceException {
		Task taskTemplate = new Task();
		taskTemplate.name = name;
		return (Task)takeEntry(taskTemplate, txn);
	}

	@Override
	public Task getTaskToCompute(String hostname) throws MCASpaceException{
		if(!isRunning())
			throw new MCASpaceException("Case isn't started");
		try {
			Created created = TransactionFactory.create(transactionManager, LEASE_DURATION);
			Transaction transaction = new Transaction(created);
			leaseRenewalTask = new LeaseRenewalTask(transaction.getLease());
			leaseRenewalTask.start();
			taskInProgress = 
				getTask(TaskState.WAIT_FOR_COMPUTE, transaction.getTransaction());
			if (taskInProgress == null){
				leaseRenewalTask.interrupt();
				leaseRenewalTask = null;
				return null;
			}
			taskInProgress.setTransaction(transaction);
			taskInProgress.setState(TaskState.ON_COMPUTING);
			taskInProgress.worker = hostname;
			addTask(taskInProgress);
			return taskInProgress;
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASpaceException();
		}
	}

	private boolean isRunning() {
		return state == ComputationCaseState.STARTED;
	}

	@Override
	public void updateTaskComputed(Task task) throws MCASpaceException {
		if (taskInProgress == null) {
			logger.warning("ComputationCaseImpl -- no task in progress");
			return;
		}
		Transaction transaction = task.getTransaction();
		getTask(task.name, transaction.getTransaction());
		addTask(task);
		try {
			transaction.getTransaction().commit();
		} catch (UnknownTransactionException e) {
			logger.warning("ComputationCaseImpl -- transaction can't be committed : " + e.getMessage());
			e.printStackTrace();
			throw new MCASpaceException(e.getMessage());
		} catch (CannotCommitException e) {
			e.printStackTrace();
			logger.warning("ComputationCaseImpl -- transaction can't be committed : " + e.getMessage());
			throw new MCASpaceException(e.getMessage());
		} catch (RemoteException e) {
			logger.warning("ComputationCaseImpl -- " + e.getMessage());
			throw new MCASpaceException(e.getMessage());
		}finally{
			taskInProgress = null;
			leaseRenewalTask.interrupt();
			leaseRenewalTask = null;
		}
		logger.fine("ComputationCaseImpl -- Task [" + task.name + "] updated.");
	}

	/**
	 * 
	 * @author cyril
	 *
	 */
	class LeaseRenewalTask extends Thread{

		private Lease lease;
		private boolean interrupt;

		public LeaseRenewalTask(Lease lease) {
			this.lease = lease;
		}

		@Override
		public void run() {
			while(!interrupt){
				try {
					Thread.sleep(SLEEP_TIME);
					logger.finest("LeaseRenewalTask -- Renew transaction lease");
					lease.renew(LEASE_DURATION);
				} catch (Exception e) {
					interrupt = true;
				}
			}
		}

		@Override
		public synchronized void interrupt() {
			interrupt = true;
			super.interrupt();
		}

	}

	@Override
	public <T extends Data<?>> T getData(String name, Class<T> formatClass) {
		// TODO Auto-generated method stub
		return null;
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


}
