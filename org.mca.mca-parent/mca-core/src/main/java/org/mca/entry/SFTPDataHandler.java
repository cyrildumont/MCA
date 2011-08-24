/**
 * 
 */
package org.mca.entry;

import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * @author Cyril
 *
 */
public class SFTPDataHandler extends DataHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9132346207185100637L;
	
	public String login;
	public String password;
	public String server;
	public String filename;
	
	private Session session;
	
	/**
	 * @see org.mca.entry.DataHandler#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		try {
			ChannelSftp c = getChannelSFTP();
			InputStream inputStream = c.get(this.filename);
			return inputStream;
		} catch (JSchException e) {
			e.printStackTrace();
			return null;
		} catch (SftpException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			ChannelSftp c = getChannelSFTP();
			OutputStream outputStream = c.put(this.filename);
			return outputStream;
		} catch (JSchException e) {
			e.printStackTrace();
			return null;
		} catch (SftpException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	

	@Override
	public void close() {
		session.disconnect();
	}

	private ChannelSftp getChannelSFTP() throws JSchException {
		JSch jsch = new JSch();
		session = jsch.getSession(this.login,this.server, 22);
		session.setPassword(this.password);
		java.util.Properties config=new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();
		Channel channel=session.openChannel("sftp");
		channel.connect();
		ChannelSftp c=(ChannelSftp)channel;
		return c;
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
