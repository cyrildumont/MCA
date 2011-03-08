package org.mca.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.jmx.JMXComponent;

public class JMX {


	/** Log */
	private final static Log LOG = LogFactory.getLog(JMX.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 4){
			LOG.error("JMX <host> <port> <name> <method>");
			System.exit(-1);
		}
		String host = args[0];
		String port = args[1];
		String name = args[2];
		String method = args[3];
		
		try {

			JMXComponent component = new JMXComponent(name, host, port);
			String[] signature = new String[]{};
			Object[] params = new Object[]{};
			Object result = component.invoke(method, params, signature);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
