package org.mca.util;

import java.io.File;

import etm.core.configuration.EtmManager;
import etm.core.configuration.XmlEtmConfigurator;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;

public abstract class BenchUtil {

	private static EtmMonitor monitor;
	
	private static boolean benchMode = false;

	public static void activateBench(boolean activate){
		benchMode = activate;
		if(benchMode) setup();
	}
	
	private static final String JETM_CONFIG_FILE = 
		System.getProperty("mca.home") + "/conf/jetm-config.xml";
	
	private static void setup() {
		XmlEtmConfigurator.configure(new File(JETM_CONFIG_FILE));
		monitor = EtmManager.getEtmMonitor();
		monitor.start();
	}

	private static void tearDown() {
		monitor.stop();
	}
	
	public static EtmPoint start(String pointName){
		if (!benchMode) return null;
		return monitor.createPoint(pointName);
	}
	
	public static void stop(EtmPoint point){
		if (benchMode) point.collect();
	}
}
