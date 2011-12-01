/**
 * 
 */
package org.mca.agent;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.jini.core.lookup.ServiceID;

import org.mca.entry.Property;
import org.mca.ft.FTManager;
import org.mca.javaspace.ComputationCase;
import org.mca.scheduler.Task;

/**
 * abstract class define a ComputeAgent
 * 
 * @author Cyril Dumont
 * 
 *
 */
public abstract class AbstractComputeAgent<R> implements ComputeAgent<R>{

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.agent.ComputeAgent";

	protected static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	protected ComputationCase computationCase;

	protected Task<R> task;

	protected Map<String, String> properties = new HashMap<String, String>();

	protected Object[] parameters;
	
	private FTManager ftManager;
	
	public AbstractComputeAgent() {
	}
	
	final public R compute(Task<R> task) 	throws ComputeAgentException{
		try{
			ftManager = FTManager.getInstance();
			this.task = task;
			this.parameters = task.parameters;
			Collection<Property> props = computationCase.getProperties(); 
			for (Property mcaProperty : props) {
				properties.put(mcaProperty.name, mcaProperty.value);
			}
			logger.fine("AbstractComputeAgent -- Computation Case [" + computationCase.getName() + "] ");
			logger.fine("AbstractComputeAgent -- " +
					"[" + computationCase.getName() + "] : " + properties.size() + " properties");
			for (Map.Entry<String, String> property : properties.entrySet()) {
				logger.fine(" \t " + property.getKey() + " = " + property.getValue());
			}
			preCompute();
			R result= execute();
			return result;
		}catch(Exception e){
			e.printStackTrace();
			throw new ComputeAgentException();
		}
	}

	final public void serviceIDNotify(ServiceID serviceID) {
		logger.fine("Agent [ID=" + serviceID.toString() + "] is deployed");
		System.exit(1);
	}

	final public void setCase(ComputationCase computationCase){
		this.computationCase = computationCase;
	}

	protected abstract R execute() throws Exception;

	/**
	 * 
	 */
	protected void preCompute(){
		logger.fine("AbstractComputeAgent -- no precompute defined");
	}

	protected final File getTempFile(String name){
		File file = new File(System.getProperty("temp.worker.result") + "/" + name);
		return file;
	}
	
}
