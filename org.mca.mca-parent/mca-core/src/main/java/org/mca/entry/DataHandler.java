package org.mca.entry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.io.Util;
import org.w3c.dom.Node;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DataHandler extends Storable{

	private static final long serialVersionUID = 1L;

	protected static final String TEMP_FILE_EXT = ".dat";
	
	public String name;
	
	public Integer part;

	public String worker;

	public DataHandler() {}

	public DataHandler(String name, Integer part) {
		this.name = name;
		this.part = part;
	}

	/**
	 * 
	 * @param dir
	 * @throws IOException
	 */
	public File download(String dir) throws IOException{
		String dest = dir + "/" + generateBaseFileName() + TEMP_FILE_EXT;
		final File file = new File(dest);
		FileOutputStream out = new FileOutputStream(file);
		InputStream stream = getInputStream();
		Util.copyStream(stream, out, 1024);
		stream.close();
		out.close();
		close();
		return file;
	}

	/**
	 * 
	 * @return
	 */
	protected String generateBaseFileName(){
		return part == null ? name : name + "-" + part;  
	}
	
	/**
	 * 
	 * @param input
	 */
	public void upload(InputStream input) throws IOException{
		OutputStream stream = getOutputStream();
		Util.copyStream(input, stream);
		stream.close();
		close();
	}

	
	protected InputStream getInputStream(){throw new NotImplementedException();}

	protected OutputStream getOutputStream(){throw new NotImplementedException();}
	
	protected void close(){}

	@Override
	public void parse(Node node) {}

	@Override
	public void store(Node parent) {}

	@Override
	public String toString() {
		return "DataHandler [name=" + name + ", part=" + part + ", worker="
				+ worker + "]";
	}
	
	

}
