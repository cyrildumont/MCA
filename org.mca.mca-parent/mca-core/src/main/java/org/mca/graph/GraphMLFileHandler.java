package org.mca.graph;

import java.util.ArrayList;

import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uci.ics.jung.exceptions.FatalException;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;

public class GraphMLFileHandler extends DefaultHandler{

	private static final String GRAPH = "graph";
	private static final String NODE = "node";
	private static final String EDGE = "edge";
	private static final String TASK = "mca:task";
	private static final String PARAMS = "params";
	private static final String PARAM = "param";

	private MCAGraph mGraph;
	private StringLabeller mLabeller;
	private StringLabeller nameLabeller;
	private MCAVertex currentVertex;
	private Task currentTask;
	private ArrayList<Object> currentParams;
	private long currentID;

	protected Graph getGraph() {
		return mGraph;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

		super.characters(ch, start, length);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if(qName.toLowerCase().equals(GRAPH)){
			createGraph(attributes);
		}else if (qName.toLowerCase().equals(NODE)) {
			createNode(attributes);
		}else if (qName.toLowerCase().equals(EDGE)) {
			createEdge(attributes);
		}else if (qName.toLowerCase().equals(TASK)) {
			addTaskToNode(attributes);
		}else if (qName.toLowerCase().equals(PARAMS)) {
			currentParams = new ArrayList<Object>();
		}else if (qName.toLowerCase().equals(PARAM)) {
			addParam(attributes);
		}
	}
	
	/**
	 * 
	 * @param attributes
	 */
	private void addParam(Attributes attributes) {
		Object value = attributes.getValue("value");
		currentParams.add(value);
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.toLowerCase().equals(PARAMS)){
			currentTask.parameters = currentParams.toArray();
		}
	}

	/**
	 * 
	 * @param attributes
	 */
	private void addTaskToNode(Attributes attributes) {
		currentTask = new Task();
		currentTask.state = TaskState.valueOf(attributes.getValue("state"));
		currentTask.name = attributes.getValue("name");
		currentTask.compute_agent_url = attributes.getValue("compute_agent_url");
		currentVertex.setTask(currentTask);
		try {
			nameLabeller.setLabel(currentVertex, currentTask.name);
		} catch (UniqueLabelException e) {
			throw new FatalException("Name of the task must be unique");
		}
	}

	/**
	 * 
	 * @param attributes
	 */
	private void createEdge(Attributes attributes) {
		String sourceId = attributes.getValue("source");
		Vertex sourceVertex =
            mLabeller.getVertex(sourceId);	
        String targetId = attributes.getValue("target");
        Vertex targetVertex =
                 mLabeller.getVertex(targetId);
        mGraph.addEdge(new DirectedSparseEdge(sourceVertex, targetVertex));
	}

	/**
	 * 
	 * @param attributes
	 */
	private void createNode(Attributes attributes) {
		currentVertex = (MCAVertex)mGraph.addVertex(new MCAVertex());
		try {
			currentID = Long.valueOf(attributes.getValue("id"));
			mLabeller.setLabel(currentVertex, String.valueOf(currentID));		
		} catch (UniqueLabelException ule) {
			throw new FatalException("Ids must be unique");
		}

	}

	/**
	 * 
	 * @param attributes
	 */
	private void createGraph(Attributes attributes) {
		mGraph = new MCAGraph();
		mLabeller = mGraph.getLabeller();
		nameLabeller = mGraph.getNameLabeller();
		
	}


}
