package org.mca.server;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.service.BuilderException;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import com.sun.jini.tool.ClassServer;

/**
 * 
 */

/**
 * @author Cyril
 *
 */
@ManagedResource(objectName = "MCA:type=CodebaseServer")
public class CodebaseServer extends Thread{

	private ClassServer classServer;
	private boolean verbose;
	private int port;
	private List<String> dirs;
	private boolean trees;
	private boolean stoppable;

	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			String dirList = getDirlist();
			classServer = new ClassServer(port, dirList, trees, verbose, stoppable);
			classServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setDirs(List<String> dirs) {
		this.dirs = dirs;
	}
	
	@ManagedAttribute
	public boolean getVerbose(){
		return verbose;
	}


	/**
	 * @return the dirlist
	 */
	public String getDirlist() {
		StringBuffer buffer = new StringBuffer();
		String pathSeparator = File.pathSeparator;
		for (String dir : dirs) {
			buffer.append(dir + pathSeparator);
		}
		return buffer.toString();
	}


	public void setClassServer(ClassServer classServer) {
		this.classServer = classServer;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void setStoppable(boolean stoppable) {
		this.stoppable = stoppable;
	}
	
	public void setTrees(boolean trees) {
		this.trees = trees;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * @return the port
	 */
	@ManagedAttribute
	public int getPort() {
		return port;
	}

	/**
	 * @return the stoppable
	 */
	@ManagedAttribute
	public boolean getStoppable() {
		return stoppable;
	}

	/**
	 * @return the trees
	 */
	@ManagedAttribute
	public boolean getTrees() {
		return trees;
	}

}
