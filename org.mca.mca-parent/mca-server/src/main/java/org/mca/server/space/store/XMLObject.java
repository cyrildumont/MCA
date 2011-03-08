package org.mca.server.space.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.w3c.dom.Node;

import com.sun.jini.outrigger.StorableObject;
import com.sun.jini.outrigger.StoredObject;

public abstract class XMLObject implements StoredObject{
	
	/**
	 * 
	 *
	 */
	public XMLObject(Node node) {
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
	public void restore(StorableObject object) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baos = getByteArrayOutputStream();
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		object.restore(ois);
		ois.close();	
	}

}
