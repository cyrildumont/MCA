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
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.log.LogUtil;
import org.mca.util.MCAUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class Vector<E> extends Data<E> {

	public Integer size;
	public Integer nbPart;

	private int localPart;
	private File localFile;
	
	public Map<Integer, String> dataHandlers;
	public Map<Integer, SubVector<E>> subVectors;

	public Vector(){
		dataHandlers = null;
		subVectors = null;
	}

	public E get(int index) throws Exception{
		if (index >= size ) throw new Exception();
		int partSize = size / nbPart;
		int num = index / partSize;
		int indexLocal = index - (num * partSize)  ;
		LogUtil.debug("l'élément [" + index + "] se trouve dans la partie [" + num + "] à l'index [" + indexLocal + "]", getClass());
		SubVector<E> vector = subVectors.get(num);
		return vector.get(indexLocal);
	}
	
	public void set(int index, E value) throws Exception{
		if (index >= size ) throw new Exception();
		int partSize = size / nbPart;
		int num = index / partSize;
		int indexLocal = index - (num * partSize)  ;
		LogUtil.debug("l'élément [" + index + "] se trouve dans la partie [" + num + "] à l'index [" + indexLocal + "]", getClass());
		SubVector<E> vector = subVectors.get(num);
		vector.set(indexLocal, value);
	}


	/**
	 * 
	 * @param number
	 * @param name
	 */
	public void addPart(Integer number, String name){
		if (dataHandlers == null) dataHandlers = new HashMap<Integer, String>();
		dataHandlers.put(number, name);
	}

	/**
	 * @see org.mca.entry.Storable#parse(org.w3c.dom.Node)
	 */
	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		name = attributes.getNamedItem("name").getNodeValue();
		size = Integer.valueOf(attributes.getNamedItem("size").getNodeValue());
		nbPart = Integer.valueOf(attributes.getNamedItem("nbPart").getNodeValue());
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if(child.getNodeName().equals("parts")){
				parseParts(child);
			}
		}
	}

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
				addPart(number, name);
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
		node.setAttribute("size", this.size.toString());
		node.setAttribute("nbPart", this.nbPart.toString());

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

	/**
	 * 
	 * @param part
	 */
	public SubVector<E> load(int part, SubVectorFormat<E> format) throws Exception{
		LogUtil.debug("Loading part [" + part + "] ...", getClass());
		localPart = part;
		String name = dataHandlers.get(part);
		localFile = download(name);
		SubVector<E> data = format.parse(localFile);
		LookupLocator lookup = new LookupLocator("jini://localhost");
		ServiceRegistrar registrar = lookup.getRegistrar();
		Entry[] entries = new Entry[]{new Name(name)};
		Exporter exporter =
			new BasicJeriExporter(TcpServerEndpoint.getInstance(MCAUtils.getIP(),0), new BasicILFactory());
		Remote remote = exporter.export(data);
		ServiceItem item = new ServiceItem(null, remote, entries);
		registrar.register(item, Long.MAX_VALUE);
		DataHandler handler = computationCase.removeDataHandler(name);
		handler.lookup = MCAUtils.getIP();
		computationCase.addDataHandler(handler);
		addSubVector(part, data);
		return data;
	}

	/**
	 * 
	 * @param part
	 * @param data
	 */
	private void addSubVector(int part, SubVector<E> data) {
		if (subVectors == null ) subVectors = new HashMap<Integer, SubVector<E>>(); 
		subVectors.put(part, data);

	}

	/**
	 * 
	 * @throws Exception
	 */
	public void save(SubVectorFormat<E> format) throws Exception{
		String dataHandlerName = dataHandlers.get(localPart);
		SubVector<E> subVector = subVectors.get(localPart);
		File out = new File(System.getProperty("temp.worker.result") + dataHandlerName);
		format.format(subVector, out);
		FileInputStream fis = new FileInputStream(out);
		computationCase.uploadData(dataHandlerName, fis);
	}

	private File download(String name) throws MCASpaceException{
		LogUtil.debug("Download file [" + name + "]  ...", getClass());
		File file = computationCase.downloadData(name, System.getProperty("temp.worker.download"));
		return file;
	}

	public void update() throws Exception {
		LogUtil.debug("Update local vector [" + name + "]...", getClass());
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
				Class[] classes = new Class[]{SubVector.class};
				ServiceTemplate template = new ServiceTemplate(null, classes,entries);
				SubVector<E> subVector = (SubVector<E>)registrar.lookup(template);
				subVectors.put(handler.getKey(), subVector);
				LogUtil.debug("Part [" + part + "] updated --> " + subVector, getClass());
			}
		}
	}

}
