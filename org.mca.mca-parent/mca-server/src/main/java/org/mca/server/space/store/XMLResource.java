package org.mca.server.space.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.w3c.dom.Node;

import com.sun.jini.outrigger.StorableResource;
import com.sun.jini.outrigger.StoredResource;

public abstract class XMLResource implements StoredResource,StorableResource{
	
	/**
	 * 
	 *
	 */
	public XMLResource(Node node) {
		parse(node);
	}
	
	public abstract ByteArrayOutputStream getByteArrayOutputStream();
	
	/**
	 * 
	 * @param inputStream
	 */
	public abstract void parse(Node node);
	
	/**
	 * 
	 */
	public void restore(StorableResource resource) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baos = getByteArrayOutputStream();
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		resource.restore(ois);
		ois.close();	
	}

}
