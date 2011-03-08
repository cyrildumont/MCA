package org.mca.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.jmx.JMXComponent;
import org.mca.jmx.JMXConstantes;

public class ExecutionTimeCollector {


	/** Log */
	private final static Log LOG = LogFactory.getLog(ExecutionTimeCollector.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 2){
			LOG.error("ExecutionTimeCollector <host> <port>");
			System.exit(-1);
		}
		String host = args[0];
		String port = args[1];
		
		try {

			JMXComponent component = new JMXComponent(JMXConstantes.JMX_EXECUTION_TIME_NAME, host, port);
			String[] signature = new String[]{};
			Object[] params = new Object[]{};
			Object result = component.invoke("renderResultsAsText", params, signature);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
