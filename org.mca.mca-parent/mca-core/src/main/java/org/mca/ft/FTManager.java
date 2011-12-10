package org.mca.ft;

import java.util.logging.Logger;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.space.JavaSpace05;

import org.mca.data.DData;
import org.mca.data.DataPartInfo;
import org.mca.javaspace.JavaSpaceParticipant;
import org.mca.javaspace.exceptions.EntryNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.Task;

public class FTManager extends JavaSpaceParticipant {

	
	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.ft.FTManager";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	private static FTManager instance;
	
	private FTManager(JavaSpace05 space){
		logger.fine("FTManager -- fault tolerance context initializing ...");
		this.space = space;
		logger.fine("FTManager -- fault tolerance context initialized on [" + space + "]");
	}
	
	public static FTManager getInstance() throws Exception{
		if (instance == null) {
			JavaSpace05 space = findSpace();
			instance = new FTManager(space);
		}
		return instance;
	}
	
	public static FTManager getInstance(JavaSpace05 space) {
		if (instance == null) {
			instance = new FTManager(space);
		}
		return instance;
	}
	
	private static JavaSpace05 findSpace() throws Exception {
		LookupLocator ll = new LookupLocator("jini://localhost:4161");
		ServiceRegistrar registrar = ll.getRegistrar();
		Class<?>[] classes = new Class<?>[]{JavaSpace05.class};	
		ServiceTemplate template = new ServiceTemplate(null, classes,null);
		JavaSpace05 space = (JavaSpace05)registrar.lookup(template);
		return space;
	}
	
	public void saveData(String name, Object value, Integer checkpoint) throws Exception{
		DData data = new DData(name,checkpoint);
		takeEntry(data, null);
		data.checkpoint = checkpoint;
		data.value = value;
		writeEntry(data, null);
	}
	
	public Object recoverData(String name) throws Exception{
		DData data = new DData(name);
		data = (DData)readEntry(data, null);
		return data;
	}
	
	public void saveCurrentTask(Task task) throws Exception{
		takeEntry(new Task(), null);
		writeEntry(task, null);
		logger.fine("FTManager -- current task saved : " + task);
	}
	
	public Task recoverCurrentTask() throws MCASpaceException {
		try {
			Task task = (Task)readEntry(new Task(), null);
			logger.fine("FTManager -- recover current task : " + task);
			return task;
		} catch (EntryNotFoundException e) {
			logger.fine("FTManager -- no current task to recover");
			return null;
		}

	}
	
	
	public void saveCheckPoint(Checkpoint checkpoint) throws MCASpaceException{
		writeEntry(checkpoint, null);
		logger.fine("FTManager -- checkpoint saved : " + checkpoint);
	}
	
	public void saveDataPartInfo(DataPartInfo infos) throws MCASpaceException{
		DataPartInfo template = new DataPartInfo(infos.name, infos.part);
		takeEntry(template, null);
		writeEntry(infos, null);
		logger.fine("FTManager -- data part infos saved : " + infos);
	}
	
	public Task removeCurrentTask() throws Exception{
		Task task = (Task)takeEntry(new Task(), null);
		logger.fine("FTManager -- remove current task : " + task);
		return task;
	}
	
	public void saveCurrentContext(FTContext context) throws MCASpaceException{
		takeEntry(new FTContext(), null);
		writeEntry(context, null);
		logger.fine("FTManager -- current context saved : " + context);
	}
	
	public FTContext recoverContext() throws MCASpaceException{
		try {
			FTContext context = (FTContext)readEntry(new FTContext(), null);
			logger.fine("FTManager -- recover current context : " + context);
			return context;
		} catch (EntryNotFoundException e) {
			logger.fine("FTManager -- no context to recover");
			return null;
		}
	}

}
