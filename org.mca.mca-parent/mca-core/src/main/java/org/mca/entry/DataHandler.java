package org.mca.entry;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Node;

public class DataHandler extends Storable{
	 
	private static final long serialVersionUID = -2138412094431779435L;
	
	public String name;
	
	public String lookup;
	
	public DataHandler() {}

	public InputStream getInputStream(){return null;}
	
	public OutputStream getOutputStream(){return null;}
	
	public void close(){}

	@Override
	public void parse(Node node) {}

	@Override
	public void store(Node parent) {}
}
