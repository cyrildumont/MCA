package org.mca.entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@SuppressWarnings("serial")
public class State extends Storable{

	public ComputationCaseState state;
	
	
	/**
	 * @see org.mca.entry.Storable#parse(org.w3c.dom.Node)
	 */
	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		state = ComputationCaseState.valueOf(attributes.getNamedItem("state").getNodeValue());
	}

	/**
	 * @see org.mca.entry.Storable#store(org.w3c.dom.Node)
	 */
	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("state", this.state.toString());
		parent.appendChild(node);
	}

}
