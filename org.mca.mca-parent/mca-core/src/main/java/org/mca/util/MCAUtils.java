package org.mca.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public abstract class MCAUtils {

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

	 		return null;
		}catch(Exception e){
	        System.out.println("pas de carte reseau");
	        e.printStackTrace();
	        return null;
	    }
	    
	}
}
