package org.mca.scheduler.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.graph.GraphMLFile;
import org.mca.graph.MCAGraph;
import org.mca.graph.MCAVertex;
import org.mca.graph.TaskStateFilter;
import org.mca.scheduler.Scheduler;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.filters.UnassembledGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;

public class XMLScheduler  extends Scheduler{

	/** Log */
	private final static Log LOG = LogFactory.getLog(XMLScheduler.class);

	private MCAGraph graph;

	private String filename;

	private StringLabeller nameLabeller;

	private int nbComputedTasks;

	private int nbTasks;

	private static int notifSeqNum = 1;

	
	/**
	 * 
	 *
	 */
	public XMLScheduler(Observer o) {
		super(o);
		graph = new MCAGraph();
		nameLabeller = graph.getNameLabeller();
	}
	
	/**
	 * 
	 *
	 */
	public XMLScheduler() {
		super();
		graph = new MCAGraph();
		nameLabeller = graph.getNameLabeller();
	}
	
	/**
	 * @param filename
	 */
	private void load() {
		GraphMLFile file = new GraphMLFile();
		try {
			filename = getSchedulerFile();
		} catch (NoSchedulerFileFoundException e) {
			LOG.error("No scheduler file found");
		}
		this.graph = (MCAGraph)file.load(filename);
		nameLabeller = graph.getNameLabeller();
		nbTasks = graph.getVertices().size();
		initialize();
	}

	@Override
	public void start() {
		load();
	}
	
	/**
	 * 
	 * @param task
	 */
	public void addTask(Task task){
		MCAVertex vertex = new MCAVertex();
		vertex.setTask(task);
		graph.addVertex(vertex);
		try {
			nameLabeller.setLabel(vertex, task.name);
		} catch (UniqueLabelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param task
	 * @param dependsOn
	 */
	public void dependsOn(Task task, Task dependsOn){
		Vertex to = graph.getVertex(task.name);
		Vertex from = graph.getVertex(dependsOn.name);
		DirectedSparseEdge edge = new DirectedSparseEdge(from,to);
		graph.addEdge(edge);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Task getTask(long id){
		MCAVertex vertex = graph.getVertex(id);
		if (vertex != null) {
			return vertex.getTask();
		}
		return null;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Task getTask(String name){
		MCAVertex vertex = graph.getVertex(name);
		if (vertex != null) {
			return vertex.getTask();
		}
		return null;
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	public Task[] getTasks(TaskState state){
		TaskStateFilter filter = new TaskStateFilter(state);
		UnassembledGraph ug = filter.filter(this.graph);
		Set<MCAVertex> vertices = ug.getUntouchedVertices();
		if (LOG.isDebugEnabled()) {
			LOG.debug(vertices.size() + " tasks with the state " + state );
		}
		Task[] tasks = new Task[vertices.size()];
		int i = 0; 
		for (MCAVertex vertex : vertices) {
			tasks[i++] = vertex.getTask();
		}
		return tasks;
	}
	
	/**
	 * 
	 * @param id
	 */
	public void addTaskComputed(String name){
		ArrayList<Task> tasks = new ArrayList<Task>();
		MCAVertex vertex = graph.getVertex(name);
		if (vertex != null) {
			Task task = vertex.getTask();
			setTasktoComputed(task);
			Set<MCAVertex> sucessors = vertex.getSuccessors();
			if (LOG.isDebugEnabled()) {
				LOG.debug(sucessors.size() + " task(s) depend(s) of " + task);
			}
			for (MCAVertex sucessor : sucessors) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("-->" + sucessor.getTask());
				}
				if(sucessor.isReadyToCompute()){
					Task ready = sucessor.getTask();
					ready.setState(TaskState.READY_TO_COMPUTE);
					tasks.add(ready);
				}
			}
			//save();
			notifyforNewTaks(tasks.toArray(new Task[tasks.size()]));
		}
	}

	/**
	 * @param task
	 */
	private void setTasktoComputed(Task task) {
		task.state = TaskState.COMPUTED;
		nbComputedTasks++;
		if(nbComputedTasks == nbTasks){
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * 
	 *
	 */
	public void save() {
		GraphMLFile file = new GraphMLFile();
		file.save(graph,this.filename);

	}

	/**
	 * 
	 *
	 */
	public void save(String filename) {
		GraphMLFile file = new GraphMLFile();
		file.save(graph,filename);

	}


	/**
	 * 
	 * @return
	 */
	private void initialize(){
		TaskStateFilter filter = new TaskStateFilter(TaskState.WAIT_FOR_ANOTHER_TASK);
		UnassembledGraph ug = filter.filter(this.graph);
		Set<MCAVertex> vertices = ug.getUntouchedVertices();
		for (MCAVertex vertex : vertices) {
			Task task = vertex.getTask();
			if (vertex.isReadyToCompute()) {
				task.setState(TaskState.READY_TO_COMPUTE);
				notifyforNewTaks(new Task[]{task});
			}
		}
		//save();
	}

	/**
	 * 
	 */
	public void reset(){
		Set<MCAVertex> vertices = graph.getVertices();
		for (MCAVertex vertex : vertices) {
			Task task = vertex.getTask();
			task.setState(TaskState.WAIT_FOR_ANOTHER_TASK);
		}
		save();
	}

	public String getFilename() {
		return filename;
	}

	public int getNbTasks(){
		return nbTasks;
	}

	public int getNbComputedTasks() {
		return nbComputedTasks;
	}


	/**
	 * 
	 * @return
	 * @throws NoSchedulerFileFoundException
	 */
	private String getSchedulerFile() throws NoSchedulerFileFoundException {
		String filename = System.getProperty("mca.home") + "/cases/" + projectName + "/work/scheduler.xml";
		File file = new File(filename);
		boolean exist = file.isFile();
		if (!exist) {
			throw new NoSchedulerFileFoundException();
		}
		return filename;
	}
}
