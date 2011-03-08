package org.mca.server.space.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.jini.id.UuidFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.service.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JavaSpaceInfoBuilder {

	private static final String ENTRY = "entry";

	/** Log */
	private final static Log LOG = LogFactory.getLog(JavaSpaceInfoBuilder.class);

	private static final String SESSION_ID = "sessionId";

	private static final String UUID = "uuid";

	private static final String TIME = "time";

	private static final String JOIN_STATE_MANAGER = "joinStateManager";

	private static final String ENTRIES = "entries";

	private JavaSpaceInfo info;

	private Document document;

	/**
	 * 
	 *
	 */
	public JavaSpaceInfoBuilder() {
		info = new JavaSpaceInfo();
	}

	/**
	 * 
	 * @param f
	 * @return
	 * @throws BuilderException
	 */
	public JavaSpaceInfo parse(File f) throws BuilderException{
		if (LOG.isDebugEnabled()) {
			LOG.debug("previousLog file : " + f.getAbsolutePath());
		}
		try {
			FileInputStream inputStream = new FileInputStream(f);
			return parse(inputStream);
		} catch (FileNotFoundException e) {
			LOG.error("File not found : " + f.getName());
			throw new BuilderException();
		}
	}

	/**
	 * 
	 * @param inputStream
	 * @return
	 * @throws BuilderException
	 */
	public JavaSpaceInfo parse(InputStream inputStream) throws BuilderException{	
		DocumentBuilderFactory documentBuilder = DocumentBuilderFactory.newInstance();
		documentBuilder.setIgnoringElementContentWhitespace(true);
		DocumentBuilder builder;
		try {
			builder = documentBuilder.newDocumentBuilder();
			document = builder.parse(inputStream);
			parse();
			return info;
		} catch (ParserConfigurationException e) {
			LOG.error(e.getMessage());
			throw new BuilderException();
		} catch (SAXException e) {
			LOG.error(e.getMessage());
			throw new BuilderException();
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw new BuilderException();
		}
	}

	/**
	 * 
	 *
	 */
	private void parse() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Start of the parsing ...");
		}	
		Element javaspace = document.getDocumentElement();
		parseJavaSpace(javaspace);

		if (LOG.isDebugEnabled()) {
			LOG.debug("End of the parsing ...");
		}	

	}

	/**
	 * @param javaspace
	 */
	private void parseJavaSpace(Element javaspace) {
		NamedNodeMap atts = javaspace.getAttributes();

		Node sessionID = atts.getNamedItem(SESSION_ID); 
		info.setSessionID(Long.valueOf(sessionID.getNodeValue()));
		Node uuid = atts.getNamedItem(UUID); 
		info.setUuid(UuidFactory.create(uuid.getNodeValue()));
		Node time = atts.getNamedItem(TIME); 
		Date previousDate = new Date(Long.valueOf(time.getNodeValue()));
		if (LOG.isInfoEnabled()) {
			LOG.info("Last start at " + previousDate);
		}
		NodeList nodes = javaspace.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String nodeName = node.getNodeName();
			if (nodeName.equals(JOIN_STATE_MANAGER)) {
				parseJoinStateManager(node);
			}
			if (nodeName.equals(ENTRIES)) {
				parseEntries(node);
			}
		}
	}

	/**
	 * 
	 * @param node
	 */
	private void parseEntries(Node entriesNode) {
		NodeList entries = entriesNode.getChildNodes();
		ArrayList<EntryInfo> entryInfos = new ArrayList<EntryInfo>();
		for (int i = 0; i < entries.getLength(); i++) {
			Node entrie = entries.item(i);
			if (entrie.getNodeName().equals(ENTRY)) {
				EntryInfo entryInfo = new EntryInfo(entrie);
				entryInfos.add(entryInfo);
			}
		}
		info.setEntryInfos(entryInfos.toArray(new EntryInfo[entryInfos.size()]));
	}

	/**
	 * 
	 * @param node
	 */
	private void parseJoinStateManager(Node node) {
		JoinState joinState = new JoinState(node);

		info.setJoinState(joinState);
	}

}
