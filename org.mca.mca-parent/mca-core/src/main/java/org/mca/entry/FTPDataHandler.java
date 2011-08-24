/**
 * 
 */
package org.mca.entry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Cyril
 *
 */
public class FTPDataHandler extends DataHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -551412548569786714L;

	public String login;
	public String password;
	public String server;
	public String filename;

	private FTPClient ftp;
	/**
	 * @see org.mca.entry.DataHandler#getInputStream()
	 */
	@Override
	protected InputStream getInputStream() {
		try {
			ftp = new FTPClient();
			ftp.connect(this.server);
			ftp.login(this.login, this.password);
			InputStream inputStream = ftp.retrieveFileStream(this.filename);
			return inputStream;
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected OutputStream getOutputStream() {
		try {
			ftp = new FTPClient();
			ftp.connect(this.server);
			ftp.login(this.login, this.password);
			OutputStream outputStream = ftp.storeFileStream(this.filename);
			if (outputStream == null )
				outputStream = ftp.appendFileStream(this.filename);
			return outputStream;
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	protected void close() {
		try {
			ftp.disconnect();
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
		login = attributes.getNamedItem("login").getNodeValue();
		password = attributes.getNamedItem("password").getNodeValue();
		server = attributes.getNamedItem("server").getNodeValue();
		filename = attributes.getNamedItem("filename").getNodeValue();
		worker = attributes.getNamedItem("worker").getNodeValue();
	}

	/**
	 * @see org.mca.entry.Storable#store(org.w3c.dom.Node)
	 */
	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		node.setAttribute("login", this.login);
		node.setAttribute("password", this.password);
		node.setAttribute("server", this.server);
		node.setAttribute("filename", this.filename);
		node.setAttribute("worker", this.worker);
		parent.appendChild(node);
	}

}
