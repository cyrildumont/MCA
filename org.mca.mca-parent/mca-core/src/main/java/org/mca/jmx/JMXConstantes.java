package org.mca.jmx;

public interface JMXConstantes {

	final public static String JMX_CORE_PORT = "9099";
	final public static String JMX_MASTER_PORT = "9098";
	final public static String JMX_WORKER_PORT = "9097";
	
	final public static String JMX_URL_HEADER = "service:jmx:rmi:///jndi/rmi://";
	final public static String JMX_URL_FOOTER = "/jmxrmi";
	
	final public static String JMX_MASTER_NAME = "MCA:type=ComputingMaster";
	final public static String JMX_WORKER_NAME = "MCA:type=ComputingWorker";
	
	final public static String JMX_EXECUTION_TIME_NAME = "MCA:service=PerformanceMonitor";
}
