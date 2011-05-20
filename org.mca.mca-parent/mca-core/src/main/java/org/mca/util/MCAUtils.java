package org.mca.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public abstract class MCAUtils {


	public static NetworkInterface[] getNetworkInterfaces(){
		List<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface interfaceN = (NetworkInterface)en.nextElement(); 
				Enumeration<InetAddress> ienum = interfaceN.getInetAddresses();
				while (ienum.hasMoreElements()) {
					InetAddress ia = ienum.nextElement();
					String adress = ia.getHostAddress().toString();     
					System.out.println(adress);
				}
				System.out.println("-------");
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
}
