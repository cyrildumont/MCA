package org.mca.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.space.AvailabilityEvent;
import net.jini.space.JavaSpace05;

import org.mca.data.format.DataFormat;
import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.entry.Storable;
import org.mca.ft.Checkpoint;
import org.mca.ft.FTManager;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.util.MCAUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * Parent class to all distributed data structures 
 * 
 * @author Cyril Dumont
 *
 * @param <E>
 */
public class DDataStructure<E> extends Storable implements RemoteEventListener{

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.data.DData";

	protected static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	protected int numLocalPart;
	
	protected DataPartLocal localPart;
	
	protected File outputlocalFile;

	public String name;
	
	public DataFormat<E> format;
	
	protected ComputationCase computationCase;
	
	protected Map<Integer, DataPartRemote> remoteParts;

	private FTManager ftManager;

	public DDataStructure() {}
	
	public DDataStructure(String name){
		this.name = name;
	}
	
	public DDataStructure(DataFormat<E> format) {
		this.format = format;
	}
	
	public void setComputationCase(ComputationCase computationCase) {
		this.computationCase = computationCase;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @param part
	 * @return
	 * @throws Exception
	 */
	public DataPart load(int part) throws Exception{
		logger.fine("[" + name + "] - loading part [" + part + "] ...");
		numLocalPart = part;
		String localPartName = name + "-" + part;
		outputlocalFile = 
			new File(System.getProperty("temp.worker.result") + "/" + localPartName + ".dat");
		File inputlocalFile = download(part);

		localPart = format.parse(inputlocalFile);
		DataPartInfo infos = localPart.getInfos();
		infos.name = name;
		infos.part = part;
		ftManager = FTManager.getInstance();
		ftManager.saveDataPartInfo(infos);
		DataHandler handler = computationCase.removeDataHandler(name, part);
		handler.worker = MCAUtils.getIP();
		computationCase.addDataHandler(handler);
		Collection<DataHandler> parts = computationCase.listenDataPart(name, this);
		remoteParts = new HashMap<Integer, DataPartRemote>();
		for (DataHandler dh : parts) {
			addPart(dh.part, dh.worker);
		}
		localPart.setParent(this);
		return localPart;
	}
	
	/**
	 * 
	 * @param part
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public DataPartLocal load(int part, Object values) throws Exception{
		logger.fine("[" + name + "] - loading part [" + part + "] ...");
		numLocalPart = part;
		String localPartName = name + "-" + part;
		outputlocalFile = 
			new File(System.getProperty("temp.worker.result") + "/" + localPartName + ".dat");
		localPart = generatePart(values);
		DataPartInfo infos = localPart.getInfos();
		infos.name = name;
		infos.part = part;
		ftManager = FTManager.getInstance();
		ftManager.saveDataPartInfo(infos);
		DataHandler handler = computationCase.removeDataHandler(name, part);
		handler.worker = MCAUtils.getIP();
		computationCase.addDataHandler(handler);
		Collection<DataHandler> parts = computationCase.listenDataPart(name, this);
		remoteParts = new HashMap<Integer, DataPartRemote>();
		for (DataHandler dh : parts) {
			addPart(dh.part, dh.worker);
		}
		localPart.setParent(this);
		return localPart;
	}

	/**
	 * 
	 * @param part
	 * @throws Exception
	 */
	public void unload() throws Exception{
		if (numLocalPart == 0) 
			throw new MCASpaceException("No part load on local");
		logger.fine("[" + name + "] - unloading part [" + numLocalPart + "] ...");
		localSave();
		upload(numLocalPart);
	}
	
	private void updatePart(int part, String worker) throws Exception {
		JavaSpace05 space = null;
		if(worker != null && !"".equals(worker)){
			LookupLocator ll = new LookupLocator(worker, 4161);
			ServiceRegistrar registrar = ll.getRegistrar();
			Class[] classes = new Class[]{JavaSpace05.class};
			ServiceTemplate template = new ServiceTemplate(null, classes, null);
			space = (JavaSpace05)registrar.lookup(template);
		}
		DataPartRemote dataPart = remoteParts.get(part);
		dataPart.setSpace(space);
	}
	
	private void addPart(int part, String worker) throws Exception {
		JavaSpace05 space = null;
		if(worker != null && !"".equals(worker)){
			LookupLocator ll = new LookupLocator(worker, 4161);
			ServiceRegistrar registrar = ll.getRegistrar();
			Class[] classes = new Class[]{JavaSpace05.class};
			ServiceTemplate template = new ServiceTemplate(null, classes, null);
			space = (JavaSpace05)registrar.lookup(template);
		}
		DataPartRemote dataPart = generateRemotePart(part, space);
		dataPart.setParent(this);
		remoteParts.put(part, dataPart);
		logger.fine("[" + name + "] - part [" + part + "] is on [" + worker + "]");
	}
	
	private File download(int part) throws MCASpaceException{
		logger.fine("[" + name + "] - download part [" + part + "]  ...");
		File file = computationCase.download(name, part, System.getProperty("temp.worker.download"));
		logger.fine("[" + name + "] - part [" + part + "]  downloaded");
		return file;
	}
	
	private File upload(int part) throws Exception{
		logger.fine("[" + name + "] - upload part [" + part + "]  ...");
		InputStream input = new FileInputStream(outputlocalFile);
		computationCase.upload(name, part, input);
		return outputlocalFile;
	}
	
	/**
	 * @see org.mca.entry.Storable#parse(org.w3c.dom.Node)
	 */
	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		name = attributes.getNamedItem("name").getNodeValue();
		parseProperties(attributes);
	}

	protected void parseProperties(NamedNodeMap attributes){throw new NotImplementedException();}

	
	/**
	 * @see org.mca.entry.Storable#store(org.w3c.dom.Node)
	 */
	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		
		storeProperties(node);
		parent.appendChild(node);
	}


	protected void storeProperties(Element node){throw new NotImplementedException();}

	/**
	 * 
	 * @throws Exception
	 */
	public void save(DataFormat<E> format) throws Exception{
		String dataHandlerName = name + "-" + numLocalPart;
		File out = new File(System.getProperty("temp.worker.result") + dataHandlerName);
		format.format(localPart, out);
		FileInputStream fis = new FileInputStream(out);
		computationCase.upload(name, numLocalPart, fis);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void localSave() throws Exception{
		format.format(localPart.getValues(), outputlocalFile);
	}

	/**
	 * 
	 * @return
	 */
	public int getNbParts(){throw new NotImplementedException();};	
	
	/**
	 * @param part
	 * @return
	 */
	protected File generatePartFile(int part) {
		String tmpDir = System.getProperty("mca.home") + "/work/" ;
		File file = new File(tmpDir + "/" + name + "-" + part + ".dat");
		return file;
	}
	
	protected DataPart getDataPart(int part){
		return remoteParts.get(part);
	}
	
	/**
	 * 
	 * @param cc
	 * @param factory
	 * @throws MCASpaceException
	 */
	public void deploy(ComputationCase cc, DataHandlerFactory factory) throws MCASpaceException {
		
		int nbParts = getNbParts();
		for (int i = 1; i <= nbParts; i++) {
			deployPart(i,cc,factory);
		}

	}

	protected void deployPart(int i, ComputationCase cc,
			DataHandlerFactory factory) throws MCASpaceException{throw new NotImplementedException();}

	
	protected DataPartLocal generatePart(Object values) throws Exception{throw new NotImplementedException();}
	
	protected DataPartRemote generateRemotePart(int part, JavaSpace05 space) 
		throws Exception{throw new NotImplementedException();}

	@Override
	public String toString() {
		return "DData [name:" + name + "]";
	}

	@Override
	public void notify(RemoteEvent event) throws UnknownEventException,
			RemoteException {
		AvailabilityEvent ae = (AvailabilityEvent)event;
		try {
			DataHandler dh = (DataHandler)ae.getEntry();
			Integer part = dh.part;
			String worker = dh.worker;
			logger.fine("DData -- [" + name + "] part [" + part + "] moved on [" + worker + "]");
			String localWorker = MCAUtils.getIP();
			if (!localWorker.equals(worker)) {
				updatePart(part, worker);
			}
		} catch (Exception e) {
			logger.warning("DData -- error during notification : " + e.getMessage());
			logger.throwing(getClass().getName(), "notify", e);
		}
		
	}
	
	public Checkpoint getLastCheckpoint(){
		return computationCase.getLastCheckpoint();
	}

}
