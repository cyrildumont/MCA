package org.mca.math;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
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
import org.mca.entry.Storable;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.util.MCAUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class Data<E> extends Storable{

	protected int localPart;
	
	protected File localFile;
	
	public String name;
	
	protected ComputationCase computationCase;
	
	protected Map<Integer, String> dataHandlers;
	protected Map<Integer, DataPart<E>> dataParts;
	
	public void setComputationCase(ComputationCase computationCase) {
		this.computationCase = computationCase;
	}

	public DataPart<E> load(int part, DataFormat<E> format) throws Exception{
		LogUtil.debug("Loading part [" + part + "] ...", getClass());
		localPart = part;
		String name = dataHandlers.get(part);
		localFile = download(name);
		DataPart<E> data = format.parse(localFile);
		publishPart(name, data);
		DataHandler handler = computationCase.removeDataHandler(name);
		handler.lookup = MCAUtils.getIP();
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
		for (Map.Entry<Integer, String> handler : dataHandlers.entrySet()) {
			int part = handler.getKey();
			String subVectorName = handler.getValue();
			if (localPart != part) {
				DataHandler dataHandler = computationCase.getDataHandler(handler.getValue());
				String lookup = dataHandler.lookup;
				LookupLocator ll = new LookupLocator("jini://"+ lookup);
				ServiceRegistrar registrar = ll.getRegistrar();
				Entry[] entries = new Entry[]{new Name(subVectorName)};
				ServiceTemplate template = new ServiceTemplate(null, null,entries);
				DataPart<E> dataPart = (DataPart<E>)registrar.lookup(template);
				dataParts.put(handler.getKey(), dataPart);
				LogUtil.debug("Part [" + part + "] updated --> " + dataPart, getClass());
			}
		}
	}
	
	public void addDataHandler(Integer number, String name){
		if (dataHandlers == null) dataHandlers = new HashMap<Integer, String>();
		dataHandlers.put(number, name);
	}
	
	private void addPart(int part, DataPart<E> data) {
		if (dataParts == null ) dataParts = new HashMap<Integer, DataPart<E>>(); 
		dataParts.put(part, data);
	}
	
	private File download(String name) throws MCASpaceException{
		LogUtil.debug("Download file [" + name + "]  ...", getClass());
		File file = computationCase.downloadData(name, System.getProperty("temp.worker.download"));
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
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if(child.getNodeName().equals("parts")){
				parseParts(child);
			}
		}
	}

	protected abstract void parseProperties(NamedNodeMap attributes);

	/**
	 * 
	 * @param node
	 */
	private void parseParts(Node node) {
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if(child.getNodeName().equals("part")){
				NamedNodeMap attributes = child.getAttributes();
				Integer number = 
					Integer.valueOf(attributes.getNamedItem("number").getNodeValue());
				String name = attributes.getNamedItem("name").getNodeValue();
				addDataHandler(number, name);
			}
		}
	}

	/**
	 * @see org.mca.entry.Storable#store(org.w3c.dom.Node)
	 */
	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		storeProperties(node);

		if (dataHandlers.size() > 0) {
			Element eParts = doc.createElement("parts");
			for (Map.Entry<Integer, String> part : dataHandlers.entrySet()) {
				Element ePart = doc.createElement("part");
				ePart.setAttribute("number", String.valueOf(part.getKey()));
				ePart.setAttribute("name", part.getValue());
				eParts.appendChild(ePart);
			}
			node.appendChild(eParts);
		}
		parent.appendChild(node);
	}


	protected abstract void storeProperties(Element node);

	/**
	 * 
	 * @throws Exception
	 */
	public void save(DataFormat<E> format) throws Exception{
		String dataHandlerName = dataHandlers.get(localPart);
		DataPart<E> part = dataParts.get(localPart);
		File out = new File(System.getProperty("temp.worker.result") + dataHandlerName);
		format.format(part, out);
		FileInputStream fis = new FileInputStream(out);
		computationCase.uploadData(dataHandlerName, fis);
	}
	
	
	public void deploy(String host){
		
	}

		
}
