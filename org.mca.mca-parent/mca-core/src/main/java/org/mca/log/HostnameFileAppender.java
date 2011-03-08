package org.mca.log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.DailyRollingFileAppender;

public class HostnameFileAppender extends DailyRollingFileAppender{

	@Override
	public void setFile(String file) {
		super.setFile(file);
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			int index =  fileName.lastIndexOf(".");
			fileName = fileName.substring(0, index) + "_" + hostname + fileName.substring(index);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
}
