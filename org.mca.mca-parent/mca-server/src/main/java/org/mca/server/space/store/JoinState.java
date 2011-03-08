package org.mca.server.space.store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.MarshalledObject;
import java.util.ArrayList;

import net.jini.core.discovery.LookupLocator;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Cyril
 *
 */
public class JoinState extends XMLObject {

	private static final String ATTRIBUTES = "attributes";
	private static final String LOOKUP_LOCATORS = "lookupLocators";
	private static final String GROUPS = "groups";
	private static final String ATTRIBUTE = "attribute";
	private static final String OBJECT = "object";
	private static final String CLASS = "class";

	private ArrayList attributesList;
	
	private LookupLocator[] lookupLocators;
	
	private String[] groups;
	
	public JoinState(Node node) {
		super(node);
		
	}

	@Override
	public void parse(Node node) {
		NodeList childsnode = node.getChildNodes();
		for (int i = 0; i < childsnode.getLength(); i++) {
			Node child = childsnode.item(i);
			String nodeName = child.getNodeName();
			if (nodeName.equals(ATTRIBUTES)) {
				parseAttributes(child);
			}
			if (nodeName.equals(LOOKUP_LOCATORS)) {
				parseLookupLocators(child);
			}
			if (nodeName.equals(GROUPS)) {
				parseGroups(child);
			}
		}
	}




	private void parseGroups(Node child) {
		// TODO Auto-generated method stub
		
	}

	private void parseLookupLocators(Node child) {
		
	}

	/**
	 * 
	 * @param child
	 */
	private void parseAttributes(Node node) {
		attributesList = new ArrayList();
		NodeList childsnode = node.getChildNodes();
		for (int i = 0; i < childsnode.getLength(); i++) {
			Node child = childsnode.item(i);
			String nodeName = child.getNodeName();
			if (nodeName.equals(ATTRIBUTE)) {
				parseAttribute(child);
			}
		}
	}

	/**
	 * 
	 * @param child
	 */
	private void parseAttribute(Node node) {
		NodeList childsnode = node.getChildNodes();
		NamedNodeMap attributes = node.getAttributes();
		Node nodeClass = attributes.getNamedItem(CLASS);
		String className = nodeClass.getNodeValue();
		for (int i = 0; i < childsnode.getLength(); i++) {
			Node child = childsnode.item(i);
			String nodeName = child.getNodeName();
			if (nodeName.equals(OBJECT)) {
				Object attibute = XMLUtil.decode((Element)child,className);
				attributesList.add(attibute);
			}
		}
		
	}

	@Override
	public ByteArrayOutputStream getByteArrayOutputStream() {
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeInt(attributesList.size());
			for (Object attribute : attributesList) {
				MarshalledObject mObject = new MarshalledObject(attribute);
				oos.writeObject(mObject);
			}
			lookupLocators = new LookupLocator[]{new LookupLocator("jini://localhost")};
			oos.writeObject(lookupLocators);
			groups = new String[]{""};
			oos.writeObject(groups);
			return baos;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}



}
