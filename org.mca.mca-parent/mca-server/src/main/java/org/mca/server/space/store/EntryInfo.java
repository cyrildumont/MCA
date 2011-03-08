/**
 * 
 */
package org.mca.server.space.store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.jini.id.Uuid;
import net.jini.io.MarshalledInstance;

import org.mca.entry.Storable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Cyril
 *
 */
public class EntryInfo extends XMLResource{

	private long bits0;
	private long bits1;
	private long expires;
	private String codebase;
	private String className;
	private String[] superclasses;
	private MarshalledInstance[] values;
	private long hash;
	private long[] hashes;

	/**
	 * 
	 * @param entrie
	 */
	public EntryInfo(Node entrie) {
		super(entrie);
	}

	@Override
	public ByteArrayOutputStream getByteArrayOutputStream() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeLong(bits0);
			oos.writeLong(bits1);
			oos.writeLong(expires);
			oos.writeObject(codebase);
			oos.writeObject(className);
			oos.writeObject(superclasses);
			oos.writeObject(values);
			oos.writeLong(hash);
			oos.writeObject(hashes);
			return baos;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void parse(Node entry) {
		try {
			NamedNodeMap attributes = entry.getAttributes();
			bits0 = Long.valueOf(attributes.getNamedItem("bits0").getNodeValue());
			bits1 = Long.valueOf(attributes.getNamedItem("bits1").getNodeValue());
			expires = Long.valueOf(attributes.getNamedItem("expires").getNodeValue());
			codebase = attributes.getNamedItem("codebase").getNodeValue();
			className = attributes.getNamedItem("classname").getNodeValue();
			superclasses = toStringArray(attributes.getNamedItem("superclasses").getNodeValue());
			Class entryClass = Class.forName(className);
			Node entryNode = null;
			NodeList childNodes = entry.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (node.getNodeName().equals(className)) {
					entryNode = node;
				}
			}
			Method parseMethod = entryClass.getMethod("parse", new Class[]{Node.class});
			Storable storableEntry = (Storable)entryClass.newInstance();
			parseMethod.invoke(storableEntry, new Object[]{entryNode});
			values = storableEntry.getValues();
			hash = Long.valueOf(attributes.getNamedItem("hash").getNodeValue());
			hashes = tolongArray(attributes.getNamedItem("hashes").getNodeValue());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	 * @param string
	 * @return
	 */
	private String[] toStringArray(String string){
		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(string, ";");
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		return list.toArray(new String[list.size()]);
		
	}
	
	/**
	 * 
	 * @param string
	 * @return
	 */
	private long[] tolongArray(String string){
		ArrayList<Long> list = new ArrayList<Long>();
		StringTokenizer st = new StringTokenizer(string, ";");
		while (st.hasMoreTokens()) {
			list.add(Long.valueOf(st.nextToken()));
		}
		long[] tab = new long[list.size()];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = list.get(i);
		}
		return tab;
	}

	/**
	 * 
	 */
	public void restore(ObjectInputStream arg0) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void store(ObjectOutputStream oos) throws IOException {
		oos.writeLong(bits0);
		oos.writeLong(bits1);
		oos.writeLong(expires);
		oos.writeObject(codebase);
		oos.writeObject(className);
		oos.writeObject(superclasses);
		oos.writeObject(values);
		oos.writeLong(hash);
		oos.writeObject(hashes);
		
	}

	public Uuid getCookie() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getExpiration() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setExpiration(long arg0) {
		// TODO Auto-generated method stub
		
	}


}
