package org.mca.entry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collection;

import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;

import org.apache.commons.net.io.Util;
import org.mca.core.ComponentInfo;
import org.mca.javaspace.exceptions.CaseNotFoundException;
import org.mca.javaspace.exceptions.EntryNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.javaspace.exceptions.NoDuplicatedDataHandlerException;
import org.mca.javaspace.exceptions.NotExistDataHandlerException;
import org.mca.log.LogUtil;
import org.mca.math.Data;
import org.mca.math.Vector;
import org.mca.model.Lookup;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@SuppressWarnings("serial")
public class ComputationCase extends Storable {

	/** name */
	public String name;

	/** description */
	public String description;

	/** state */
	public ComputationCaseState state;

	public Transaction transaction;

	public ComputationCase() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ComputationCaseState getState() {
		return state;
	}

	public void setState(ComputationCaseState state) {
		this.state = state;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		name = attributes.getNamedItem("name").getNodeValue();
		description = attributes.getNamedItem("description").getNodeValue();
		state = ComputationCaseState.valueOf(attributes.getNamedItem("state").getNodeValue());
	}

	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		node.setAttribute("description", this.description);
		node.setAttribute("state", this.state.toString());
		parent.appendChild(node);	
	}

	/**
	 * 
	 * @param computationCase
	 * @throws MCASpaceException
	 * @throws CaseNotFoundException 
	 */
	public void update() throws MCASpaceException, CaseNotFoundException{
		if(!check()) throw new MCASpaceException();
		ComputationCase template = new ComputationCase();
		template.name = name;
		ComputationCase caseReturned;
		try {
			caseReturned = (ComputationCase)takeEntry(template, null);
			caseReturned.state = state;
			writeEntry(caseReturned,null);
			LogUtil.debug("Computation case [" + template.name + "] updated.", getClass());
		} catch (EntryNotFoundException e) {
			throw new CaseNotFoundException();
		}

	}
	/**
	 * 
	 * @param property
	 * @throws MCASpaceException
	 */
	public void addProperty(MCAProperty property) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		MCAProperty template = new MCAProperty();
		template.name = property.name;
		try {
			takeEntry(template, transaction);
			LogUtil.debug("[" + this.name + "] The MCAProperty [" + template.name + "] exists.", getClass());
			writeEntry(property,  transaction);
			LogUtil.debug("[" + this.name + "] MCAProperty [" + template.name + "] updated.", getClass());
		} catch (EntryNotFoundException e) {
			writeEntry(property, transaction);
			LogUtil.debug("[" + this.name + "] MCAProperty [" + template.name + "] added.", getClass());
		}
	}

	/**
	 * 
	 * @return
	 * @throws MCASpaceException
	 */
	public Collection<Task> getTasks() throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		Collection<Entry> templates = new ArrayList<Entry>();
		templates.add(new Task());
		Collection<Entry> result = readEntry(templates, transaction);
		Collection<Task> tasks = new ArrayList<Task>();
		for (Entry entry : result) {
			tasks.add((Task)entry);
		}
		return tasks;
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws EntryNotFoundException 
	 */
	public String getProperty(String name) 
	throws MCASpaceException, EntryNotFoundException{
		if(!check()) throw new MCASpaceException();
		MCAProperty template = new MCAProperty();
		template.name = name;
		MCAProperty propertiesReturned = (MCAProperty)readEntry(template, transaction);
		String value = propertiesReturned.getValue();
		return value;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @throws MCASpaceException
	 */
	public void addProperty(String name, String value) throws MCASpaceException{
		addProperty(new MCAProperty(name, value));
	}

	/**
	 * 
	 * @return
	 * @throws MCASpaceException
	 */
	public Collection<MCAProperty> getProperties() throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		Collection<Entry> templates = new ArrayList<Entry>();
		templates.add(new MCAProperty());
		Collection<Entry> result = readEntry(templates,transaction);
		Collection<MCAProperty> properties = new ArrayList<MCAProperty>();
		for (Entry entry : result) {
			properties.add((MCAProperty)entry);
		}
		return properties;
	}

	/**
	 * 
	 * @param task
	 * @param computationCase
	 * @throws MCASpaceException
	 * @throws EntryNotFoundException 
	 */
	public void updateTask(Task task) throws MCASpaceException, EntryNotFoundException{
		getTask(task.name);
		addTask(task);
		LogUtil.debug("[" + this.name + "] Task [" + task.name + "] updated.", getClass());

	}

	/**
	 * 
	 * @param tasks
	 * @throws MCASpaceException
	 */
	public void addTasks(Task[] tasks, ComputationCase computationCase)throws MCASpaceException{
		LogUtil.debug("[" + this.name + "] add " + tasks.length + " tasks.",getClass());	
		for (Task task : tasks) {
			addTask(task);
		}
	}

	/**
	 * 
	 * @return
	 * @throws MCASpaceException 
	 * @throws EntryNotFoundException 
	 */
	public Task getTask(TaskState state) throws MCASpaceException, EntryNotFoundException{
		if(!check()) throw new MCASpaceException();
		Task taskTemplate = new Task();
		taskTemplate.state = state;
		return (Task)takeEntry(taskTemplate,transaction);
	}

	/**
	 * 
	 * @return
	 * @throws MCASpaceException 
	 * @throws EntryNotFoundException 
	 */
	public Task getTask(Task template) throws MCASpaceException, EntryNotFoundException{
		if(!check()) throw new MCASpaceException();
		return (Task)readEntry(template,transaction);
	}

	/**
	 * 
	 * @return
	 * @throws MCASpaceException 
	 * @throws EntryNotFoundException 
	 */
	public Task getTask(String name) throws MCASpaceException, EntryNotFoundException{
		if(!check()) throw new MCASpaceException();
		Task taskTemplate = new Task();
		taskTemplate.name = name;
		return (Task)takeEntry(taskTemplate, transaction);
	}

	/**
	 * 
	 * @param task
	 * @throws MCASpaceException
	 */
	public void addTask(Task task) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		LogUtil.debug("[" + this.name + "] add task [" + task + "].",getClass());
		writeEntry(task, transaction);
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws MCASpaceException
	 * @throws EntryNotFoundException 
	 */
	public String getResult(String name) throws MCASpaceException, EntryNotFoundException{
		if(!check()) throw new MCASpaceException();
		Task taskTemplate = new Task();
		taskTemplate.name = name;
		Task task =(Task)readEntry(taskTemplate, transaction);
		return String.valueOf(task.result);

	}

	/**
	 * 
	 * @return
	 * @throws MCASpaceException 
	 * @throws EntryNotFoundException 
	 */
	public Task readTask(String name) throws MCASpaceException, EntryNotFoundException{
		if(!check()) throw new MCASpaceException();
		Task taskTemplate = new Task();
		taskTemplate.name = name;
		return (Task)readEntry(taskTemplate, transaction);
	}

	/**
	 * 
	 * @param handler
	 * @throws MCASpaceException
	 * @throws NoDuplicatedDataHandlerException 
	 */
	public void addDataHandler(DataHandler handler) throws MCASpaceException, NoDuplicatedDataHandlerException{
		if(!check()) throw new MCASpaceException();
		DataHandler template = new DataHandler();
		template.name = handler.name;
		try {
			readEntry(template, transaction);
			throw new NoDuplicatedDataHandlerException();
		} catch (EntryNotFoundException e) {
			writeEntry(handler, transaction);
		}
	}

	/**
	 * 
	 * @param handler
	 * @throws MCASpaceException
	 * @throws NotExistDataHandlerException
	 * @throws EntryNotFoundException 
	 */
	public DataHandler removeDataHandler(String name) 
	throws MCASpaceException, EntryNotFoundException{
		if(!check()) throw new MCASpaceException();
		DataHandler template = new DataHandler();
		template.name = name;
		DataHandler handlerReturned = (DataHandler)takeEntry(template,transaction);
		LogUtil.debug("[" + this.name + "] DataHandler [ name = " + name +" ] removed.", getClass());
		return handlerReturned;
	}

	/**
	 * 
	 */
	public void removeAll() throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		ArrayList<DataHandler> templates = new ArrayList<DataHandler>();
		DataHandler template = new DataHandler();
		templates.add(template);
		//Collection<DataHandler> result = takeEntry(templates, transaction);
		//LogUtil.debug("[" + this.name + "] " + result.size() + " dataHandler deleted.", getClass());
	}



	/**
	 * 
	 * @param name
	 * @return
	 * @throws MCASpaceException
	 * @throws EntryNotFoundException 
	 */
	public DataHandler getDataHandler(String name) throws MCASpaceException, EntryNotFoundException{
		if(!check()) throw new MCASpaceException();
		DataHandler dataTemplate = new DataHandler();
		dataTemplate.name = name;
		return (DataHandler)readEntry(dataTemplate, transaction);
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws MCASpaceException
	 * @throws EntryNotFoundException 
	 */
	public InputStream getData(String name) throws MCASpaceException, EntryNotFoundException{
		try{
			DataHandler dataHandler = getDataHandler(name); 
			InputStream stream = dataHandler.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Util.copyStream(stream, out, 1024);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			stream.close();
			dataHandler.close();
			return in;
		}catch (FileNotFoundException e) {
			e.printStackTrace();
			LogUtil.error("[" + this.name + "] [" + name  + "] is not found ", getClass());
			throw new MCASpaceException();
		} catch (IOException e) {
			throw new MCASpaceException();
		}
	}

	/**
	 * 
	 * @param name
	 * @param destDir
	 * @throws MCASpaceException 
	 * @throws EntryNotFoundException 
	 */
	public File downloadData(String name, String destDir) throws MCASpaceException, EntryNotFoundException{

		try{
			LogUtil.debug("[" + this.name + "] download [" + name + "] ...", getClass());
			DataHandler dataHandler = getDataHandler(name); 
			InputStream stream = dataHandler.getInputStream();
			String dest = destDir + "/" +dataHandler.name +".dat";
			LogUtil.debug("[" + this.name + "] download [" + name + "] to [" + dest + "]", getClass());
			File file = new File(dest);
			FileOutputStream out = new FileOutputStream(file);
			Util.copyStream(stream, out, 1024);
			stream.close();
			dataHandler.close();
			LogUtil.debug("[" + this.name + "] [" + name + "] downloaded", getClass());
			return file;
		}catch (FileNotFoundException e) {
			e.printStackTrace();
			LogUtil.error("[" + this.name + "] destination directory not found ", getClass());
			throw new MCASpaceException();
		} catch (IOException e) {
			throw new MCASpaceException();
		} catch (EntryNotFoundException e) {
			LogUtil.error("[" + this.name + "] [" + name  + "] is not found ", getClass());
			throw e;
		}
	}

	/**
	 * 
	 * @param name
	 * @param srcDir
	 * @throws MCASpaceException 
	 * @throws EntryNotFoundException 
	 */
	public void uploadData(String name, InputStream input) 
	throws MCASpaceException, EntryNotFoundException{
		try{
			LogUtil.debug("[" + this.name + "] upload [" + name + "] ...", getClass());
			DataHandler dataHandler = getDataHandler(name); 
			OutputStream stream = dataHandler.getOutputStream();
			Util.copyStream(input, stream);
			stream.close();
			dataHandler.close();
			LogUtil.debug("[" + this.name + "] [" + name + "] uploaded", getClass());
		}catch (FileNotFoundException e) {
			e.printStackTrace();
			LogUtil.error("[" + this.name + "] [" +name  + "] is not found ", getClass());
			throw new MCASpaceException();
		} catch (IOException e) {
			throw new MCASpaceException();
		} catch (EntryNotFoundException e) {
			LogUtil.error("[" + this.name + "] [" +name  + "] is not found ", getClass());
			throw e;
		}
	}	

	/**
	 * 
	 * @param remote
	 * @throws MCASpaceException
	 */
	public void registerForTasks(Remote remote,TaskState state,Lookup lookup) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		Task task = new Task();
		task.state = state;
		entries.add(task);
		registerForAvailabilityEvent(entries, null, true, remote,
				Long.MAX_VALUE,new String(lookup.getHost() + ":" + lookup.getPort()));
	}

	/**
	 * 
	 * @param remote
	 * @param state
	 * @throws MCASpaceException
	 */
	public void registerForTasks(Remote remote,TaskState state) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		Task task = new Task();
		task.state = state;
		entries.add(task);
		registerForAvailabilityEvent(entries, transaction, true, remote, Long.MAX_VALUE, null);
	}

	/**
	 * 
	 * @param remote
	 * @throws MCASpaceException
	 */
	public void registerForMCAProperties(Remote remote) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		MCAProperty template = new MCAProperty();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		entries.add(template);
		registerForAvailabilityEvent(entries, transaction, true, remote, Long.MAX_VALUE, null);
	}

	/**
	 * 
	 * @param remote
	 * @throws MCASpaceException
	 */
	public void registerForTasks(Remote remote) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		Task template = new Task();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		entries.add(template);
		registerForAvailabilityEvent(entries, transaction, true, remote, Long.MAX_VALUE, null);
	}

	/**
	 * 
	 * @param tasks
	 * @param remote
	 * @throws MCASpaceException
	 */
	public void registerForResults(Collection<Task> tasks, Remote remote) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		for (Task task : tasks) {
			task.state = TaskState.COMPUTED;
			task.computing_agent_name = null;
			task.parameters = null;
			task.worker = null;
			task.result = null;
		}
		registerForAvailabilityEvent(tasks, transaction, true, remote, Long.MAX_VALUE, null);
	}

	/**
	 * 
	 * @param lookup
	 * @throws MCASpaceException
	 */
	public void registerWorker(ComponentInfo componentInfo) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		Task task = new Task();
		task.state = TaskState.WAIT_FOR_COMPUTE;
		entries.add(task);
		registerForAvailabilityEvent(entries, transaction, true, null, Long.MAX_VALUE, componentInfo);
	}

	/**
	 * 
	 * @param name
	 * @param nbWorker
	 * @throws MCASpaceException
	 * @throws EntryNotFoundException 
	 */
	public void barrier(String name, int nbWorker) throws MCASpaceException, EntryNotFoundException{
		Barrier barrier = new Barrier(name, null);
		barrier.setCounter(nbWorker);
		writeEntry(barrier, transaction);	
		LogUtil.debug("[" + this.name + "] Barrier [" + name + "] waiting ...", getClass());
		readEntry(barrier, transaction, Lease.FOREVER);
		LogUtil.debug("[" + this.name + "] Barrier [" + name + "] OK", getClass());
	}


	/**
	 * 
	 * @throws MCASpaceException
	 */
	public void stop() throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		ComputationCase template = new ComputationCase();
		template.name = this.name;
		ComputationCase theCase = (ComputationCase)takeEntry(template, null);
		theCase.state = ComputationCaseState.FINISHED;
		writeEntry(theCase, null);
	}

	/**
	 * 
	 * @param remote
	 * @throws MCASpaceException
	 */
	public void registerForUpdates(Remote remote) throws MCASpaceException{
		if(!check()) throw new MCASpaceException();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		entries.add(new Task());
		entries.add(new MCAProperty());
		entries.add(new DataHandler());
		registerForAvailabilityEvent(entries, transaction, false, remote, Long.MAX_VALUE, null);
	}
	
	/**
	 * 
	 * @param v
	 * @throws MCASpaceException
	 */
	public <E> void addData(Data<E> v) throws MCASpaceException{
		writeEntry(v, transaction);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws MCASpaceException
	 */
	public <E extends Data<?>> E getData(String name, Class<E> c) throws MCASpaceException{
		E data = null;
		try {
			data = c.newInstance();
			data.name = name;
			System.out.println(data);
			data = (E)readEntry(data,transaction);
			data.setComputationCase(this);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return data;
	}
}
