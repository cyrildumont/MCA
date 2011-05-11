package org.mca.listener;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;

import org.mca.model.Lookup;

public class LookupListener extends RegistrarEventListener {

	public LookupListener(Lookup lookup) {
		super(lookup,null,null);
	}

	@Override
	protected Object register(ServiceID uuid, ServiceItem item) {
		System.out.println("register");
		return null;
	}

	@Override
	protected Object unregister(ServiceID uuid) {
		System.out.println("register");
		return null;
	}

	@Override
	protected Object update(ServiceID uuid, ServiceItem item) {
		// TODO Auto-generated method stub
		return null;
	}
	


}
