package org.mca.server.space.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.MarshalledObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.jini.core.discovery.LookupLocator;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.io.MarshalledInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.entry.Storable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.jini.outrigger.LogOps;
import com.sun.jini.outrigger.StorableObject;
import com.sun.jini.outrigger.StorableResource;

public class MCASpaceOps implements LogOps{

	/**
	 * 
	 * @author Cyril Dumont
	 * @version 1.0
	 *
	 */
	private class TransactionalOp<T>{
		
		private T opsElement;		
		private OpsType opsType;
		
		public TransactionalOp(T opsElement, OpsType opsType) {
			this.opsElement = opsElement;
			this.opsType = opsType;
		}

		public T getOpsElement() {
			return opsElement;
		}

		public OpsType getOpsType() {
			return opsType;
		}
				
	}
	
	
	/**
	 * 
	 * @author Cyril Dumont
	 * @version 1.0
	 *
	 */
	private enum OpsType{
		WRITE,
		TAKE
	}
	
	private Document doc;
	private Element javaspace;
	private File file;
	private Element entries;
	private Element listeners;
	private Map<Uuid, Node> entriesMap;
	
	
	private Map<Long, TransactionalOp<?>> transactionMap; 
	
	private Node joinStateManager;
	private String path;
	
	/** Log */
	private final static Log LOG = LogFactory.getLog(MCASpaceOps.class);

	/**
	 * 
	 * @param path
	 */
	public MCASpaceOps(String path) {
		this.path = path;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.newDocument();
			javaspace = doc.createElement("javaspace");
			doc.appendChild(javaspace);
			entriesMap = new HashMap<Uuid, Node>();
			transactionMap = new HashMap<Long, TransactionalOp<?>>();
			entries = doc.createElement("entries");
			javaspace.appendChild(entries);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public void abortOp(Long txnId) {
		System.out.println("abortOp(" + txnId +")");
		transactionMap.remove(txnId);
	}

	/**
	 * 
	 */
	public void bootOp(long time, long sessionId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("bootOp(" + time + "," + sessionId + ")");
		}
		javaspace.setAttribute("time", String.valueOf(time));
		javaspace.setAttribute("sessionId", String.valueOf(sessionId));

	}

	/**
	 * 
	 */
	public void cancelOp(Uuid cookie, boolean expired) {
		System.out.println("cancelOp(" + cookie + "," + expired + ")");	
	}

	/**
	 * 
	 */
	public void commitOp(Long txnId) {
		System.out.println("commitOp(" + txnId + ")");
		TransactionalOp<?> to = transactionMap.get(txnId);
		OpsType type =to.getOpsType();
		switch (type) {
		case TAKE:	
			Uuid uuid = (Uuid)to.getOpsElement();
			takeOp(uuid, null);
			break;
		case WRITE:	
			StorableResource entry = (StorableResource)to.getOpsElement();
			writeOp(entry, null);
			break;
		}
		transactionMap.remove(txnId);
	}

	/**
	 * 
	 */
	public void joinStateOp(StorableObject state) {
		if (joinStateManager != null)
			javaspace.removeChild(joinStateManager);
		joinStateManager = doc.createElement("joinStateManager");
		javaspace.appendChild(joinStateManager);
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			state.store(oos);
			byte[] bytes = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			int nbAttributes = ois.readInt();
			Node attributes = doc.createElement("attributes");
			joinStateManager.appendChild(attributes);
			for (int i = 0; i < nbAttributes; i++) {
				MarshalledObject mObject = (MarshalledObject)ois.readObject();
				Object object = mObject.get();
				Element attribute = doc.createElement("attribute");
				attribute.setAttribute("class", object.getClass().getName());
				Element node = doc.createElement("object");
				XMLUtil.encodeObject(object, node);
				attribute.appendChild(node);
				attributes.appendChild(attribute);
			}
			Node lookupLocators = doc.createElement("lookupLocators");
			joinStateManager.appendChild(lookupLocators);
			LookupLocator[] ll = (LookupLocator[])ois.readObject();
			Node nGroups = doc.createElement("groups");
			joinStateManager.appendChild(nGroups);
			String[] groups = (String[])ois.readObject();
		}catch (Exception e) {
			e.printStackTrace();
		}
		save();

	}

	/**
	 * 
	 */
	public void prepareOp(Long txnId, StorableObject transaction) {
		System.out.println("prepareOp(" + txnId + "," + transaction + ")");			

	}

	/**
	 * 
	 */
	public void registerOp(StorableResource registration, String type, StorableObject[] templates) {
		System.out.println("registerOp()");
		if (listeners == null) {
			listeners = doc.createElement("listeners");
			javaspace.appendChild(listeners);
		}
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			registration.store(oos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			writeRegistration(ois, type, templates);
		}catch (IOException e) {
			e.printStackTrace();
		} 
		save();

	}

	/**
	 * 
	 */
	public void renewOp(Uuid cookie, long expiration)  {
		if (LOG.isDebugEnabled()) {
			LOG.debug("renewOp(" + cookie + "," + expiration + ")");			
		}
	}

	/**
	 * 
	 */
	public void takeOp(Uuid[] cookies, Long txnId)  {
		System.out.println("takeOp(" + cookies + "," + txnId + ")");
		for (Uuid cookie : cookies) {
			Node node = entriesMap.get(cookie);
			if (entries != null) {
				entries.removeChild(node);
			}
			entriesMap.remove(node);
		}
		save();

	}

	/**
	 * 
	 */
	public void takeOp(Uuid cookie, Long txnId) {
		System.out.println("takeOp(" + cookie + "," + txnId + ")");
		if (txnId != null){
			TransactionalOp<Uuid> to = new TransactionalOp<Uuid>(cookie, OpsType.TAKE);
			transactionMap.put(txnId, to);
			return;
		}
		Node node = entriesMap.get(cookie);
		if (node != null) {
			if (entries != null) {
				entries.removeChild(node);
			}
			entriesMap.remove(node);
			save();
		}
		
	}

	/**
	 * 
	 */
	public void uuidOp(Uuid uuid) {
		javaspace.setAttribute("uuid", String.valueOf(uuid));
		String logFile = "javaspace_" + uuid + "_" + new Date().getTime() + ".xml";
		this.file = new File(path, logFile );
		save();
	}

	/**
	 * 
	 */
	public void writeOp(StorableResource entry, Long txnId)  {
		if (txnId != null){
			TransactionalOp<StorableResource> to = 
				new TransactionalOp<StorableResource>(entry, OpsType.WRITE);
			transactionMap.put(txnId, to);
			return;
		}
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			entry.store(oos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			Element node = createEntryNode(ois, txnId, entriesMap);
			entries.appendChild(node);

		}catch (IOException e) {
			e.printStackTrace();
		} 
		save();
	}

	/**
	 * 
	 * @param ois
	 * @param txnId 
	 */
	private Element createEntryNode(ObjectInputStream ois, Long txnId, Map<Uuid, Node> entriesMap) {

		String classname = null;
		Element entry = doc.createElement("entry");
		try{

			entry.setAttribute("txnId", txnId != null ? txnId.toString() : "");
			Long bits0 = ois.readLong();
			entry.setAttribute("bits0", String.valueOf(bits0));
			Long bits1 = ois.readLong();
			entry.setAttribute("bits1", String.valueOf(bits1));
			Long expires = ois.readLong();
			entry.setAttribute("expires", String.valueOf(expires));
			String codebase = (String)ois.readObject();
			entry.setAttribute("codebase", codebase);
			classname = (String)ois.readObject();
			entry.setAttribute("classname", classname);
			String[] superclasses = (String[])ois.readObject();
			entry.setAttribute("superclasses", toArray(superclasses));
			Class entryClass = Class.forName(classname);
			Object object = entryClass.newInstance();
			if(object instanceof Storable){
				MarshalledInstance[] instances =(MarshalledInstance[])ois.readObject();
				Storable storable = (Storable)object;
				storable.init(instances);
				storable.store(entry);
				long hash = ois.readLong();
				entry.setAttribute("hash", String.valueOf(hash));
				long[] hashes = (long[])ois.readObject();
				entry.setAttribute("hashes", toArray(hashes));
				Uuid uuid = UuidFactory.create(bits0, bits1);
				entriesMap.put(uuid, entry);
			}
		}catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			LOG.error(classname + " is not storable");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return entry;
	}



	/**
	 * 
	 * @param ois
	 */
	private void writeRegistration(ObjectInputStream ois,String type, StorableObject[] templates) {

		try {
			Element listener = doc.createElement("listener");
			listener.setAttribute("type", type);
			Uuid cookie = UuidFactory.read(ois);
			listener.setAttribute("cookie", String.valueOf(cookie));
			Long expiration = ois.readLong();
			listener.setAttribute("expiration", String.valueOf(expiration));
			Long eventID = ois.readLong();
			listener.setAttribute("eventID", String.valueOf(eventID));
			Boolean visibilityOnly = ois.readBoolean();
			listener.setAttribute("visibilityOnly", String.valueOf(visibilityOnly));
			MarshalledObject handback = (MarshalledObject)ois.readObject();	
			listeners.appendChild(listener);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	/**
	 * 
	 * @param tab
	 * @return
	 */
	private String toArray(Object[] tab){
		String result = "";
		for (Object object : tab) {
			result += object + ";";
		}
		return result.substring(0, result.length() - 1);
	}

	/**
	 * 
	 * @param tab
	 * @return
	 */
	private String toArray(long[] tab){
		String result = "";
		for (Object object : tab) {
			result += object + ";";
		}
		return result.substring(0, result.length() - 1);
	}
	/**
	 * 
	 */
	public void writeOp(StorableResource[] entries, Long txnId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("writeOp(" + entries + "," + txnId + ")");
		}
	}

	/**
	 * save in the file
	 *
	 */
	private void save(){
		try{
			Source source = new DOMSource(doc);
			Result resultat = new StreamResult(file);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			transformer.transform(source, resultat);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param uuid
	 */
	public void setUuid(Uuid uuid) {
		uuidOp(uuid);
	}

	public File getFile() {
		return file;
	}

}
