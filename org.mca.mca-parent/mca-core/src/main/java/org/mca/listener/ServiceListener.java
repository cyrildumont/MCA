package org.mca.listener;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;

public class ServiceListener implements DiscoveryListener {

	private Boolean serviceFind;

	public ServiceListener() {
		try {
			serviceFind = false;
			System.setProperty("java.security.policy","/home/cyril/MCA/conf/policy");
			System.setSecurityManager(new RMISecurityManager());
			LookupDiscovery ld = new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
			ld.addDiscoveryListener(this);
			while(!serviceFind){
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void discarded(DiscoveryEvent event) {

	}

	public void discovered(DiscoveryEvent event) {
		try {
			File file = new File("C:/serviceID.txt");
			FileInputStream fis = new FileInputStream(file);
			DataInputStream dis = new DataInputStream(fis);
			ServiceID serviceID = new ServiceID(dis);
			dis.close();
			fis.close();
			ServiceRegistrar[] registrars = event.getRegistrars();
			ServiceTemplate template = new ServiceTemplate(serviceID,null,null);
			for (ServiceRegistrar registrar : registrars) {
				Object object = registrar.lookup(template);
				serviceFind = true;	
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new ServiceListener();
	}

}
