/**
 * 
 */
package org.mca.scheduler;

import java.util.ArrayList;

import org.mca.entry.Storable;
import org.mca.transaction.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Cyril Dumont
 * @version 1.0
 *
 */
public class Task extends Storable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8980475130094174166L;

	public String name;

	public Object[] parameters;
	
	/** etat de la tache */
	public TaskState state;
	
	public String computing_agent_name;
	
	public String worker;
	
	public Object result;
	
	public ArrayList<String> parentTasks;
	
	public String dataHandlerName;
	
	public String message;
	
	private transient Transaction transaction;
	
	@Override
	public String toString() {
		String parentTasks = "";
		if (this.parentTasks != null) {
			for (String task : this.parentTasks) {
				parentTasks += task + ";";
			}
			parentTasks = parentTasks.substring(0, parentTasks.length()-1);
		}
		return 	"[name : " + this.name + "] - " +
				"[computing_agent_name : " + computing_agent_name + "] - " +
				"[nbParams : " + (parameters != null ? parameters.length : 0) + "] - " +
				"[parentTasks : " + parentTasks + "] - " +
				"[state : " + this.state + "]" +
				"[message : " + this.message + "]" +
				"[worker : " + this.worker + "]" +
				"[dataHandlerName : " + this.dataHandlerName + "]";
	}
	
	
	/**
	 * @return the state
	 */
	public TaskState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(TaskState state) {
		this.state = state;
	}
	
	public void setWorker(String worker) {
		this.worker = worker;
	}


	public void setResult(Object result) {
		this.result = result;
	}


	public void setDataHandlerName(String dataHandlerName) {
		this.dataHandlerName = dataHandlerName;
	}

	/**
	 * 
	 * @param name
	 */
	public void removeParentTask(String name){
		if (parentTasks != null) {
			parentTasks.remove(name);
			if (parentTasks.size() == 0) {
				parentTasks = null;
			}
		}
	}
	
	public void addParentTask(String name){
		if (parentTasks == null) {
			parentTasks = new ArrayList<String>();
		}
		parentTasks.add(name);
	}

	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		name = attributes.getNamedItem("name").getNodeValue();
		computing_agent_name = attributes.getNamedItem("computing_agent_name").getNodeValue();
		worker = attributes.getNamedItem("worker").getNodeValue();
		result = attributes.getNamedItem("result").getNodeValue(); 
		state = TaskState.valueOf(attributes.getNamedItem("state").getNodeValue());
		message = attributes.getNamedItem("message").getNodeValue();
		dataHandlerName = attributes.getNamedItem("dataHandlerName").getNodeValue(); 
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if(child.getNodeName().equals("params")){
				parseParams(child);
			}
			else if(child.getNodeName().equals("parentTasks")){
				parseParentTasks(child);
			}
		}
	}

	/**
	 * 
	 * @param node
	 */
	private void parseParentTasks(Node node) {
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if(child.getNodeName().equals("task")){
				NamedNodeMap attributes = child.getAttributes();
				String value = attributes.getNamedItem("name").getNodeValue();
				parentTasks.add(value);
			}
		}
	}


	/**
	 * 
	 * @param child
	 */
	private void parseParams(Node node) {
		ArrayList<Object> listParams = new ArrayList<Object>();
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if(child.getNodeName().equals("param")){
				NamedNodeMap attributes = child.getAttributes();
				String value = attributes.getNamedItem("value").getNodeValue();
				listParams.add(value);
			}
		}
		parameters = listParams.toArray();	
	}

	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		node.setAttribute("computing_agent_name", this.computing_agent_name);
		node.setAttribute("worker", this.worker);
		node.setAttribute("result", String.valueOf(this.result));
		node.setAttribute("state", this.state.toString());
		node.setAttribute("message", this.message);
		node.setAttribute("dataHandlerName", this.dataHandlerName);
		if (parameters != null) {
			Element params = doc.createElement("params");
			for (Object param : parameters) {
				Element eParam = doc.createElement("param");
				eParam.setAttribute("value", String.valueOf(param));
				params.appendChild(eParam);
			}
			node.appendChild(params);
		}
		if (parentTasks != null) {
			Element tasks = doc.createElement("parentTasks");
			for (Object task : parentTasks) {
				Element eTask = doc.createElement("task");
				eTask.setAttribute("name", String.valueOf(task));
				tasks.appendChild(eTask);
			}
			node.appendChild(tasks);
		}
		parent.appendChild(node);
	
	}
	
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public Transaction getTransaction() {
		return transaction;
	}
}
