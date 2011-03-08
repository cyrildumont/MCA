package org.mca.entry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public class URLDataHandler extends DataHandler{

	public URLDataHandler() {}
	
	public String url;
	
	public InputStream getInputStream(){
		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			InputStream stream = uc.getInputStream();
			return stream;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
	}

	

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void parse(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		name = attributes.getNamedItem("name").getNodeValue();	
		url = attributes.getNamedItem("url").getNodeValue();
		lookup = attributes.getNamedItem("lookup").getNodeValue();
	}


	@Override
	public void store(Node parent) {
		Document doc = parent.getOwnerDocument();
		Element node = doc.createElement(this.getClass().getName());
		node.setAttribute("name", this.name);
		node.setAttribute("url", this.url);
		node.setAttribute("lookup", this.lookup);
		parent.appendChild(node);
		
	}


	@Override
	public OutputStream getOutputStream() {
		throw new Error("No outputStream for URLDataHandler");
	}
}
