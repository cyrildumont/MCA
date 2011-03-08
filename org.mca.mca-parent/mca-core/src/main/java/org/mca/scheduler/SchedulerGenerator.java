package org.mca.scheduler;

import org.mca.scheduler.xml.XMLScheduler;

/**
 * 
 * @author Cyril
 *
 */
public abstract class SchedulerGenerator {

	private String destFile;

	public void generate(){
		XMLScheduler sceduler = generateGraph();
		sceduler.save(destFile);
	}
	
	protected abstract XMLScheduler generateGraph();
	
	public void setDestFile(String destFile) {
		this.destFile = destFile;
	}
	
}
