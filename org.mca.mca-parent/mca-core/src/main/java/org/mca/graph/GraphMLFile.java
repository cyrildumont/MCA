package org.mca.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mca.scheduler.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uci.ics.jung.exceptions.FatalException;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.Indexer;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.io.GraphFile;

public class GraphMLFile implements GraphFile{

	private GraphMLFileHandler mFileHandler;

	private Document document;
	
	private Element eGraph;
	
	public GraphMLFile() {
		mFileHandler = new GraphMLFileHandler();
	}

	public Graph load(String filename) {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(filename), mFileHandler);

		} catch (Exception e) {
			throw new FatalException("Error loading graphml file: " + filename, e);
		}

		return mFileHandler.getGraph();
	}

	public void save(Graph graph, String filename) {
		try {
			File f = new File(filename);
			f.createNewFile();
			DocumentBuilderFactory documentBuilder = DocumentBuilderFactory.newInstance();
			documentBuilder.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = documentBuilder.newDocumentBuilder();
			document = builder.newDocument();
			
			Element graphmlNode = document.createElement("graphml");
			graphmlNode.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");
			graphmlNode.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
			graphmlNode.setAttribute("xsi:schemaLocation", "http://graphml.graphdrawing.org/xmlns test.xsd");
			document.appendChild(graphmlNode);
			Element key = document.createElement("key");
			key.setAttribute("id", "task");
			key.setAttribute("for", "node");
			graphmlNode.appendChild(key);
			eGraph = document.createElement("graph");
			eGraph.setAttribute("edgedefault", "directed");
			graphmlNode.appendChild(eGraph);
			saveVertices(graph);
			saveEdges(graph);
			FileOutputStream w = new FileOutputStream(filename);
			save(w);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param graph
	 */
	private void saveEdges(Graph graph) {
        Indexer id = Indexer.getIndexer(graph);
        for (Iterator edgeIterator = graph.getEdges().iterator(); edgeIterator.hasNext();)  {
        	DirectedSparseEdge edge = (DirectedSparseEdge) edgeIterator.next();
        	Vertex source = edge.getSource();
        	int idSource = id.getIndex(source) + 1;
        	Vertex target = edge.getDest();
        	int idTarget = id.getIndex(target)  + 1;
        	Element eEdge = document.createElement("edge");
        	eEdge.setAttribute("source", String.valueOf(idSource));
        	eEdge.setAttribute("target", String.valueOf(idTarget));
        	eEdge.setAttribute("directed", "true");
        	eGraph.appendChild(eEdge);     	
        }
		
	}

	/**
	 * 
	 * @param graph
	 */
	protected void saveVertices(Graph graph){
		int nbVertices = graph.getVertices().size();
		Indexer id = Indexer.getIndexer(graph);
		for (int i = 0; i < nbVertices; i++) {
			MCAVertex vertex = (MCAVertex)id.getVertex(i);
			Task task = vertex.getTask();
			Element node = document.createElement("node");
			node.setAttribute("id", String.valueOf(i + 1));
			Element data = document.createElement("data");
			data.setAttribute("key", "task");
			saveTask(task,data);
			node.appendChild(data);
			eGraph.appendChild(node);
		}
	}
	
	/**
	 * 
	 * @param task
	 * @param data
	 */
	private void saveTask(Task task, Element data) {
		Element eTask = document.createElement("mca:task");
		eTask.setAttribute("name", task.name);
		eTask.setAttribute("computing_agent_name",task.computing_agent_name);
		eTask.setAttribute("state",task.state.toString());
		Object[] parameters = task.parameters;
		if (parameters != null ) {
			Element eParams = document.createElement("params");
			for (Object parameter : parameters) {
				Element eParam = document.createElement("param");
				eParam.setAttribute("value", String.valueOf(parameter));
				eParams.appendChild(eParam);
			}		
			eTask.appendChild(eParams);
		}
		data.appendChild(eTask);		
	}

	/**
	 * 
	 * @param os
	 * @throws IOException
	 */
	protected void save(OutputStream os) throws IOException {
		Result result = new StreamResult(os);
		Source source = new DOMSource(document);
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(source, result);
		} catch (Exception e) {
			throw (IOException) (new IOException().initCause(e));
		}
	}

}
