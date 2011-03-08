package org.mca.entry;

import java.io.File;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ScanDir extends Observable implements Runnable{


	/** Log */
	private final static Log LOG = LogFactory.getLog(ScanDir.class);
	
	private String dirName;
	
	private HashSet<String> oldFiles;
	
	public ScanDir(String dirName) {
		this.dirName = dirName;
		this.oldFiles = new HashSet<String>();
		
	}
	
	public ScanDir(String dirName, Observer observer) {
		this.dirName = dirName;
		this.oldFiles = new HashSet<String>();
		addObserver(observer);
	}

	public void run() {
		//while(true){
			File dir = new File(dirName);
			File[] files = dir.listFiles();
			if(files != null){
				for (File file : files) {
					if (!oldFiles.contains(file.getName())) {
						LOG.debug("Nouveau fichier : " + file);
						oldFiles.add(file.getName());
						setChanged();
						notifyObservers(file);
					}
					
				}
			}
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		//}

	}

	public static void main(String[] args) {
		ScanDir scanDir = new ScanDir("c:/temp/test");
		scanDir.run();
	}

}
