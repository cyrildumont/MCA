package org.mca.server.space.store;

import java.lang.reflect.Field;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XMLUtil {

	public static void encodeObject(Object object, Element node){
		Class classObject = object.getClass();
		Field[] fields = classObject.getFields();
		for (Field field : fields) {

			try {
				String name = field.getName();
				Object value = field.get(object);
				node.setAttribute(name, String.valueOf(value));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param classname
	 * @return
	 */
	public static Object decode(Element node, String classname){
		try {
			Class classObject = Class.forName(classname);
			Object object = classObject.newInstance();
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node child = attributes.item(i);
				String nodeName = child.getNodeName();
				Field field = classObject.getField(nodeName);
				field.setAccessible(true);
				field.set(object, child.getNodeValue());
			}
			return object;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchFieldException e) {
			e.printStackTrace( );
			return null;
		}
		
		
	}

}
