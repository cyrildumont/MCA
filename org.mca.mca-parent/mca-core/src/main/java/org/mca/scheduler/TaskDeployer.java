package org.mca.scheduler;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mca.javaspace.MCASpace;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.javaspace.exceptions.NoJavaSpaceFoundException;
import org.mca.log.LogUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TaskDeployer {

	private ArrayList<Task> tasks;
	
	private MCASpace space;
	
	public TaskDeployer(ArrayList<Task> tasks, String host){
//		this.tasks = tasks;
//		space = new MCASpace();
//		space.setHost(host);
//		try {
//			space.init();
//		} catch (NoJavaSpaceFoundException e) {
//			e.printStackTrace();
//		}
	}
	
	public void deploy(){
//		Task[] tasks = this.tasks.toArray(new Task[this.tasks.size()]);
//		try {
//			space.addTasks(tasks, null);
//		} catch (MCASpaceException e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 2){
			LogUtil.error("TaskDeployer <file> <host>", TaskDeployer.class);
			System.exit(-1);
		}
		
		String file = args[0];
		String host = args[1];
		ArrayList<Task> tasks = parse(file);
		TaskDeployer deployer = new TaskDeployer(tasks, host);
		deployer.deploy();
		
	}

	private static ArrayList<Task> parse(String file) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			Node root = doc.getDocumentElement();
			NodeList list = root.getChildNodes();
			int nbTasks = list.getLength();
			

			for (int i = 0; i < nbTasks; i++) {
				Node node = list.item(i);
				if(node.getNodeType() == Document.ELEMENT_NODE){
					Task task  = new Task();
					task.parse(list.item(i));
					tasks.add(task);
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tasks;
	}

}
