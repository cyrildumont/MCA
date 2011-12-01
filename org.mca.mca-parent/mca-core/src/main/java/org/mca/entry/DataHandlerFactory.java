package org.mca.entry;

import java.io.File;

public abstract class DataHandlerFactory{
	
	private static final int DEFAULT_PART = 0;
	
	public DataHandler getDataHandler(File file){
		return getDataHandler(file.getName());
	}
	
	public DataHandler getDataHandler(String filename) {
		return getDataHandler(filename, filename);
	}
	
	public DataHandler getDataHandler(String filename, String name){
		return getDataHandler(filename, name, DEFAULT_PART);
	}

	public DataHandler getDataHandler(File file, String name) {
		return getDataHandler(file.getName(), name);
	}

	public DataHandler getDataHandler(String filename, String name, int part) {
		DataHandler dh = generate(filename, name);
		dh.part = part;
		return dh;
	}

	public DataHandler getDataHandler(File file, String name, int part) {
		return getDataHandler(file.getName(), name, part);
	}
	
	protected abstract DataHandler generate(String filename, String name);
	
}
