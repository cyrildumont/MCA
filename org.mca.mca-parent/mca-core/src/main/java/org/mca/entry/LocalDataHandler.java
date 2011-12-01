/**
 * 
 */
package org.mca.entry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Cyril
 *
 */
public class LocalDataHandler extends DataHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String filename;

	private FileOutputStream fos;
	private FileInputStream fis;
	/**
	 * @see org.mca.entry.DataHandler#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {

		try {
			fis = new FileInputStream(this.filename);
			return fis;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			fos = new FileOutputStream(this.filename);
			return fos;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}



	@Override
	public void close() {
		try {
			if (fis != null) {
				fis.close();
				fis = null;
			}else if (fos != null) {
				fos.close();
				fos = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see org.mca.entry.Storable#parse(org.w3c.dom.Node)
	 */
	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		name = attributes.getNamedItem("name").getNodeValue();
		filename = attributes.getNamedItem("filename").getNodeValue();

	}

	/**
	 * @see org.mca.entry.Storable#store(org.w3c.dom.Node)
	 */
	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		node.setAttribute("filename", this.filename);
		parent.appendChild(node);
	}

}
