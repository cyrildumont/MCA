/**
 * 
 */
package org.mca.agent;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.entry.Name;

import org.mca.entry.MCAProperty;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.result.TaskVerifier;
import org.mca.scheduler.Task;
import org.mca.util.MCAUtils;

/**
 * Classe abstraite définissant un ComputeAgent
 * 
 * @author Cyril
 * 
 *
 */
public abstract class AbstractComputeAgent implements ComputeAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9162142949499544555L;

	protected ComputationCase computationCase;

	protected Task task;

	protected Map<String, String> properties = new HashMap<String, String>();

	private ArrayList<Task> tasksToCheck = new ArrayList<Task>();

	protected HashMap<String, Object> results;

	final public Object compute(Task task) 	throws ComputeAgentException{
		try{
			this.task = task;
			Collection<MCAProperty> props = computationCase.getProperties(); 
			for (MCAProperty mcaProperty : props) {
				properties.put(mcaProperty.name, mcaProperty.value);
			}
			LogUtil.debug("[JacobiAgent][" + computationCase.getName() + "] ", getClass());
			LogUtil.debug("[JacobiAgent]" +
					"[" + computationCase.getName() + "] : " + properties.size() + " properties", getClass());
			for (Map.Entry<String, String> property : properties.entrySet()) {
				LogUtil.debug(" \t " + property.getKey() + " = " + property.getValue(), getClass());
			}
			preCompute();
			if (tasksToCheck.size() != 0) {
				checkTasks();	
			}
			return execute();
		}catch(Exception e){
			throw new ComputeAgentException();
		}
	}


	final public void serviceIDNotify(ServiceID serviceID) {
		LogUtil.info("Agent [ID=" + serviceID.toString() + "] is deployed", getClass());
		System.exit(1);
	}

	final public void setCase(ComputationCase computationCase){
		this.computationCase = computationCase;
	}

	final protected void addTaskToCheck(Task task){
		tasksToCheck.add(task);
	}

	protected abstract Object execute() throws Exception;

	final private void checkTasks() throws WaitForAnotherTaskException{
		TaskVerifier tv = new TaskVerifier(computationCase, tasksToCheck);
		results = tv.checkResults();
	}

	/**
	 * 
	 * @param taskname
	 * @return
	 */
	final protected Object getResult(String taskname){
		return results.get(taskname);
	}

	/**
	 * 
	 */
	protected void preCompute(){
		LogUtil.info("No precompute", getClass());
	}


	protected void shareData(String name, Remote data)
	throws MalformedURLException, IOException, ClassNotFoundException,
	ExportException, RemoteException {
		LookupLocator lookup = new LookupLocator("jini://localhost");
		ServiceRegistrar registrar = lookup.getRegistrar();
		Entry[] entries = new Entry[]{new Name(name)};
		Exporter exporter = 
			new BasicJeriExporter(TcpServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
		Remote remote = exporter.export(data);
		ServiceItem item = new ServiceItem(null, remote, entries);
		registrar.register(item, Long.MAX_VALUE);
	}

	protected File download(String name) throws MCASpaceException{
		File file = computationCase.downloadData(name, System.getProperty("temp.worker.download"));
		return file;
	}

	protected File getTempFile(String name){
		File file = new File(System.getProperty("temp.worker.result") + "/" + name);
		return file;
	}

}
