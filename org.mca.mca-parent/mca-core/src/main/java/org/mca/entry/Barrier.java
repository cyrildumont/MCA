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
public class Barrier extends Storable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String name;
	
	public Integer counter;
	
	
	public Barrier() {

	}
	
	/**
	 * 
	 * @param name
	 * 
	 */
	public Barrier(String name, Integer counter) {
		this.name = name;
		this.counter = counter;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer counter) {
		this.counter = counter;
	}
	
	public void increment(){
		counter++;
	}

	/**
	 * @see org.mca.entry.Storable#parse(org.w3c.dom.Node)
	 */
	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		name = attributes.getNamedItem("name").getNodeValue();
		counter = Integer.valueOf(attributes.getNamedItem("counter").getNodeValue());

	}

	/**
	 * @see org.mca.entry.Storable#store(org.w3c.dom.Node)
	 */
	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		node.setAttribute("counter", counter.toString());
		parent.appendChild(node);
	}

}
