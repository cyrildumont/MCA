package org.mca.test;

import org.mca.jmx.JMXComponent;
import org.mca.jmx.JMXConstantes;


public class WorkerStarter {

	public static void main(String[] args) throws Exception {
		String host = args[0];
		String name = args[1];
		jmx(host, name);
	}

	private static void jmx(String host, String name) {
		try {
			
			JMXComponent component = new JMXComponent(JMXConstantes.JMX_WORKER_NAME,
					host, JMXConstantes.JMX_WORKER_PORT);
			String[] signature = new String[]{"java.lang.String"};
			Object[] params = new Object[]{host};
			component.invoke("connect", params, signature);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
