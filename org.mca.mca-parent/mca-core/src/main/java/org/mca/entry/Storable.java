package org.mca.entry;

import java.io.IOException;
import java.lang.reflect.Field;
import java.rmi.MarshalledObject;

import net.jini.core.entry.Entry;
import net.jini.io.MarshalledInstance;

import org.mca.util.ClassUtil;
import org.w3c.dom.Node;

public abstract class Storable implements Entry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6432634931804024297L;

	public Storable(){}
	
	/**
	 * 
	 * @param instances
	 */
	public void init(MarshalledInstance[] instances){
		try{
			Field[] fields = ClassUtil.getFields(getClass());
			int i = 0;
			for (MarshalledInstance instance : instances) {
				if (instance != null) {
					MarshalledObject object = instance.convertToMarshalledObject();
					Object o = object.get();
					fields[i].set(this, o);
				}
				i++;
			}	
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	public MarshalledInstance[] getValues(){
		try {
			Field[] fields = ClassUtil.getFields(getClass());
			MarshalledInstance[] instances = new MarshalledInstance[fields.length];
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				MarshalledInstance instance = new MarshalledInstance(field.get(this));
				instances[i] = instance;
			}
			return instances;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * 
	 * @param inputStream
	 */
	public abstract void parse(Node node);

	/**
	 * 
	 * @param parent
	 */
	public abstract void  store(Node parent);
}
