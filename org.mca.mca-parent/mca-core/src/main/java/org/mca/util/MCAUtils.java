package org.mca.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.jini.core.constraint.MethodConstraints;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.ServerEndpoint;
import net.jini.jeri.ssl.SslServerEndpoint;
import net.jini.jeri.tcp.TcpServerEndpoint;

public abstract class MCAUtils {


	private static final String SSL_SYSTEM_PROPERTY = "org.mca.ssl";

	public static NetworkInterface[] getNetworkInterfaces(){
		List<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface interfaceN = (NetworkInterface)en.nextElement(); 
				Enumeration<InetAddress> ienum = interfaceN.getInetAddresses();
				if (ienum.hasMoreElements())interfaces.add(interfaceN);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return (NetworkInterface[])interfaces.toArray(new NetworkInterface[interfaces.size()]);
	}


	/**
	 * 
	 * @return
	 */

	public static String getIP(){
		try{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface interfaceN = (NetworkInterface)interfaces.nextElement(); 
				Enumeration<InetAddress> ienum = interfaceN.getInetAddresses();
				while (ienum.hasMoreElements()) {
					InetAddress ia = ienum.nextElement();
					String adress = ia.getHostAddress().toString();     
					if(adress.length() < 16 && !adress.startsWith("127") && !(adress.indexOf(":") > 0)){
						return adress;        
					}
				}
			}
			return "127.0.0.1";
		}catch(Exception e){
			System.out.println("pas de carte reseau");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param permissionClass
	 * @return
	 */
	public static Exporter getServiceExporter(Class<?> permissionClass){

		ServerEndpoint serviceEndpoint = null;
		MethodConstraints serviceConstraints = null;


		serviceEndpoint = SslServerEndpoint.getInstance(MCAUtils.getIP(),0);
		//			serviceConstraints = 
		//				new BasicMethodConstraints(
		//						new InvocationConstraints(
		//								new InvocationConstraint[]{Integrity.YES}, null
		//						)
		//				);

		BasicILFactory 	serviceILFactory =
			new BasicILFactory(serviceConstraints, permissionClass);

		return new BasicJeriExporter(serviceEndpoint, serviceILFactory);
	}
}
