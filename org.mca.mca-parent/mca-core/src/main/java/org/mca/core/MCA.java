package org.mca.core;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class MCA {

	/**
	 * 
	 */
	public void start(String configFile){
		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + configFile);
		MCAComponent component = (MCAComponent)context.getBean("component");
		context.getBean("exporter");
		while( true ) {
			try {
				Thread.sleep( 100000 );
			} catch( InterruptedException ex ) {
			}
		}
	}
}
