/**
 * 
 */
package org.mca.entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author cyril
 *
 */
public class Property extends Storable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String name;
	
	public String value;
	
	public Property() {
	
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public Property(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * @see org.mca.entry.Storable#parse(org.w3c.dom.Node)
	 */
	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		name = attributes.getNamedItem("name").getNodeValue();
		value = attributes.getNamedItem("value").getNodeValue();

	}

	/**
	 * @see org.mca.entry.Storable#store(org.w3c.dom.Node)
	 */
	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		node.setAttribute("value", this.value);
		parent.appendChild(node);
	}
	
	@Override
	public String toString() {
		return "[Property] -- [name : " + name + "][value : " + value + "]";
	}

}
