package org.mca.listener;

import java.io.IOException;

import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;

public class MCASpaceListener implements DiscoveryListener {

	public MCASpaceListener() {
		try {
			LookupDiscovery discovery = new LookupDiscovery(new String[]{"MCA"});
			discovery.addDiscoveryListener(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void discarded(DiscoveryEvent event) {
		System.out.println(event);
	}

	@Override
	public void discovered(DiscoveryEvent event) {
		System.out.println(event);
	}

}
