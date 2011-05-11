package org.mca.jmx;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * 
 */
public class JMXComponent {

	private ObjectName objectName;
	
	private MBeanServerConnection mbsc;
	
	public JMXComponent(String objectname, String url, String port){
		try {
			this.objectName = new ObjectName(objectname);
			JMXServiceURL jmxurl = new JMXServiceURL(JMXConstantes.JMX_URL_HEADER + url + ":" + port + JMXConstantes.JMX_URL_FOOTER);
			JMXConnector jmxc = JMXConnectorFactory.connect(jmxurl, null);
			this.mbsc = jmxc.getMBeanServerConnection();
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param operationName
	 * @param params
	 * @param signature
	 * @return
	 */
	public Object invoke(String operationName, Object[] params, String[] signature){
		try {
			return mbsc.invoke(this.objectName, operationName, params, signature);
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (MBeanException e) {
			e.printStackTrace();
			return null;
		} catch (ReflectionException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addListener(NotificationListener listener){
		try {
			mbsc.addNotificationListener(objectName, listener, null, null);
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
