package org.mca.test;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.mca.startup.Bootstrap;

public class ComponentStarter {

	public static void main(String[] args) {
		final String file = args[0];
		Bootstrap bootstrap = new Bootstrap();
		try {
			bootstrap.init(new DaemonContext(){
								public String[] getArguments() {
									return new String[]{file};
								}
								public DaemonController getController() {
									return null;
								}
			});
			bootstrap.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
