package org.mca.listener;

import net.jini.core.lookup.ServiceItem;

import org.mca.model.Lookup;

public class LookupListener extends RegistrarEventListener {

	public LookupListener(Lookup lookup) {
		super(lookup,null,null);
	}

	@Override
	protected Object register(ServiceItem item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object unregister(ServiceItem item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object update(ServiceItem item) {
		// TODO Auto-generated method stub
		return null;
	}

}
