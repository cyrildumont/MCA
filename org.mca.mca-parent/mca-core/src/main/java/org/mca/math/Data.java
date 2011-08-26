package org.mca.math;

import java.io.File;
import java.io.FileInputStream;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.entry.Name;

import org.mca.entry.DataHandler;
import org.mca.entry.DataHandlerFactory;
import org.mca.entry.Storable;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.math.format.DataFormat;
import org.mca.util.MCAUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class Data<E> extends Storable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7042018059419847706L;

	protected int localPart;
	
	protected File localFile;
	
	public String name;
	
	public DataFormat<E> format;
	
	protected ComputationCase computationCase;
	
	protected Map<Integer, DataPart<E>> dataParts;
	
	
	public Data() {}
	
	public Data(String name) {
		this.name = name;
	}
	
	public Data(String name, DataFormat<E> format) {
		this.name = name;
		this.format = format;
	}
	
	public void setComputationCase(ComputationCase computationCase) {
		this.computationCase = computationCase;
	}

	public DataPart<E> load(int part) throws Exception{
		LogUtil.debug("Loading part [" + part + "] ...", getClass());
		localPart = part;
		String localPartName = name + "-" + part;
		localFile = download(localPartName);
		DataPart<E> data = format.parse(localFile);
		publishPart(localPartName, data);
		DataHandler handler = computationCase.removeDataHandler(localPartName);
		handler.worker = MCAUtils.getIP();
		computationCase.addDataHandler(handler);
		addPart(part, data);
		return data;
	}

	public DataPart<E> getDataPart(int index){
		return dataParts.get(index);
	}
	
	/**
	 * 
	 * @param name
	 * @param data
	 * @throws Exception
	 */
	private void publishPart(String name, DataPart<E> data)
			throws Exception {
		LookupLocator lookup = new LookupLocator("jini://localhost");
		ServiceRegistrar registrar = lookup.getRegistrar();
		Entry[] entries = new Entry[]{new Name(name)};
		Exporter exporter =
			new BasicJeriExporter(TcpServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
		Remote remote = exporter.export(data);
		ServiceItem item = new ServiceItem(null, remote, entries);
		registrar.register(item, Long.MAX_VALUE);
	}

	
	public void update() throws Exception {
		LogUtil.debug("Update local part [" + name + "]...", getClass());
		LogUtil.debug("Part [" + localPart + "] local", getClass());
		
		for(int part = 1; part <= getNbParts(); part++)	{
			if (localPart != part) {
				String partName = this.name + "-" + part;
				DataHandler dataHandler = computationCase.getDataHandler(partName);
				String lookup = dataHandler.worker;
				LookupLocator ll = new LookupLocator("jini://"+ lookup);
				ServiceRegistrar registrar = ll.getRegistrar();
				Entry[] entries = new Entry[]{new Name(partName)};
				ServiceTemplate template = new ServiceTemplate(null, null,entries);
				DataPart<E> dataPart = (DataPart<E>)registrar.lookup(template);
				dataParts.put(part, dataPart);
				LogUtil.debug("Part [" + part + "] updated --> " + dataPart, getClass());
			}
		}
	}
	
	private void addPart(int part, DataPart<E> data) {
		if (dataParts == null ) dataParts = new HashMap<Integer, DataPart<E>>(); 
		dataParts.put(part, data);
	}
	
	private File download(String name) throws MCASpaceException{
		LogUtil.debug("Download file [" + name + "]  ...", getClass());
		File file = computationCase.download(name, System.getProperty("temp.worker.download"));
		return file;
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
		DataPart<E> part = dataParts.get(localPart);
		File out = new File(System.getProperty("temp.worker.result") + dataHandlerName);
		format.format(part, out);
		FileInputStream fis = new FileInputStream(out);
		computationCase.upload(dataHandlerName, fis);
	}

	/**
	 * 
	 * @return
	 */
	protected int getNbParts(){throw new NotImplementedException();};	
	

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
			DataHandlerFactory factory) throws MCASpaceException{throw new NotImplementedException();};

	
}
