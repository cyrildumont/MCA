package org.mca.math;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.ssl.SslServerEndpoint;
import net.jini.lookup.entry.Name;

import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.entry.Storable;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.format.DataFormat;
import org.mca.util.MCAUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class DData<E> extends Storable{

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.data.DData";

	protected static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	protected int localPart;
	
	protected File outputlocalFile;

	public String name;
	
	public DataFormat<E> format;
	
	protected ComputationCase computationCase;
	
	protected Map<Integer, DataPart> dataParts;

	public DData() {}
	
	public DData(String name){
		this.name = name;
	}
	
	public DData(DataFormat<E> format) {
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
		localPart = part;
		String localPartName = name + "-" + part;
		File inputlocalFile = download(localPartName);
		outputlocalFile = 
			new File(System.getProperty("temp.worker.result") + "/" + localPartName + ".dat");
		DataPart data = format.parse(inputlocalFile);
		//publishPart(localPartName, data);
		DataHandler handler = computationCase.removeDataHandler(localPartName);
		handler.worker = MCAUtils.getIP();
		computationCase.addDataHandler(handler);
		addPart(part, data);
		return data;
	}
	
	/**
	 * 
	 * @param part
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public DataPart load(int part, Object values) throws Exception{
		logger.fine("[" + name + "] - loading part [" + part + "] ...");
		localPart = part;
		String localPartName = name + "-" + part;
		outputlocalFile = 
			new File(System.getProperty("temp.worker.result") + "/" + localPartName + ".dat");
		DataPart data = generatePart(values);
		//publishPart(localPartName, data);
		DataHandler handler = computationCase.removeDataHandler(localPartName);
		handler.worker = MCAUtils.getIP();
		computationCase.addDataHandler(handler);
		addPart(part, data);
		return data;
	}

	/**
	 * 
	 * @param part
	 * @throws Exception
	 */
	public void unload() throws Exception{
		if (localPart == 0) 
			throw new MCASpaceException("No part load on local");
		logger.fine("[" + name + "] - unloading part [" + localPart + "] ...");
		localSave();
		String localPartName = name + "-" + localPart;
		upload(localPartName);
	}
	

	public DataPart getDataPart(int part){
		DataPart result = dataParts.get(part);
		if (result != null) return result;
		String partName = this.name + "-" + part;
		try {
			DataHandler dataHandler = computationCase.getDataHandler(partName);
			String lookup = dataHandler.worker;
			LookupLocator ll = new LookupLocator(lookup, 4161);
			ServiceRegistrar registrar = ll.getRegistrar();
			Entry[] entries = new Entry[]{new Name(partName)};
			ServiceTemplate template = new ServiceTemplate(null, null,entries);
			DataPart dataPart = (DataPart)registrar.lookup(template);
			dataParts.put(part, dataPart);
			return dataPart;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	/**
	 * 
	 * @param name
	 * @param data
	 * @throws Exception
	 */
	private void publishPart(String name, DataPart data)
			throws Exception {
		LookupLocator lookup = new LookupLocator(MCAUtils.getIP(),4161);
		ServiceRegistrar registrar = lookup.getRegistrar();
		Entry[] entries = new Entry[]{new Name(name)};
		Exporter exporter =
			new BasicJeriExporter(SslServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
		Remote remote = exporter.export(data);
		ServiceItem item = new ServiceItem(null, remote, entries);
		registrar.register(item, Long.MAX_VALUE);
	}

	
	public void update() throws Exception {
		logger.fine("[" + name + "] - update local part [" + localPart + "]...");
				
		for(int part = 1; part <= getNbParts(); part++)	{
			if (localPart != part) {
				String partName = this.name + "-" + part;
				DataHandler dataHandler = computationCase.getDataHandler(partName);
				String lookup = dataHandler.worker;
				LookupLocator ll = new LookupLocator(lookup, 4161);
				ServiceRegistrar registrar = ll.getRegistrar();
				Entry[] entries = new Entry[]{new Name(partName)};
				ServiceTemplate template = new ServiceTemplate(null, null,entries);
				DataPart dataPart = (DataPart)registrar.lookup(template);
				dataParts.put(part, dataPart);
				logger.fine("[" + name + "] - part [" + part + "] updated --> " + dataPart);
			}
		}
	}
	
	private void addPart(int part, DataPart data) {
		if (dataParts == null ) dataParts = new HashMap<Integer, DataPart>(); 
		dataParts.put(part, data);
	}
	
	private File download(String name) throws MCASpaceException{
		logger.fine("[" + name + "] - download file [" + name + "]  ...");
		File file = computationCase.download(name, System.getProperty("temp.worker.download"));
		logger.fine("[" + name + "] - file [" + name + "]  downloaded");
		return file;
	}
	
	private File upload(String name) throws Exception{
		logger.fine("[" + name + "] - upload file [" + name + "]  ...");
		InputStream input = new FileInputStream(outputlocalFile);
		computationCase.upload(name, input);
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
		String dataHandlerName = name + "-" + localPart;
		DataPart part = dataParts.get(localPart);
		File out = new File(System.getProperty("temp.worker.result") + dataHandlerName);
		format.format(part, out);
		FileInputStream fis = new FileInputStream(out);
		computationCase.upload(dataHandlerName, fis);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void localSave() throws Exception{
		DataPart part = dataParts.get(localPart);
		format.format(part.getValues(), outputlocalFile);
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

	
	protected DataPart generatePart(Object values){throw new NotImplementedException();}
			
	@Override
	public String toString() {
		return "DistributedData [name:" + name + "]";
	}
}
