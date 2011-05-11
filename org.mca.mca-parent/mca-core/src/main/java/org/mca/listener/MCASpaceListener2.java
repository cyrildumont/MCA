package org.mca.listener;

import java.util.Observer;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.space.JavaSpace;

import org.mca.javaspace.ComputationCase;


public class MCASpaceListener2 extends RegistrarEventListener<ComputationCase>{

	public MCASpaceListener2(String host, Observer observer) {
		super(host,JavaSpace.class,null,observer);
	}
	
	private ComputationCase convert(ServiceItem item){
		ComputationCase cc = null;
		Entry[] entries = item.attributeSets;
//		for (Entry entry : entries) {
//			if(entry instanceof Name){
//				Name name = (Name)entry;
//				cc.setName(name.name);
//			}else
//				cc.setName("");
//		}
		return cc;
		
	}
	
	
	@Override
	protected ComputationCase unregister(ServiceID uuid) {
		ComputationCase s = null;
		return s;
	}


	@Override
	protected ComputationCase update(ServiceID uuid, ServiceItem item) {
		return convert(item);
	}


	@Override
	protected ComputationCase register(ServiceID uuid, ServiceItem item) {
		return convert(item);
	}
	
	public static void main(String[] args) {
		MCASpaceListener2 l = new MCASpaceListener2("localhost", null);
		new Thread(l).start();
	}


}
