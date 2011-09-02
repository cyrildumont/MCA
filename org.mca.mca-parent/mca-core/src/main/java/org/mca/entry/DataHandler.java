package org.mca.entry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.io.Util;
import org.w3c.dom.Node;

public class DataHandler extends Storable{

	private static final long serialVersionUID = -2138412094431779435L;

	public String name;

	public String worker;

	public DataHandler() {}

	/**
	 * 
	 * @param dir
	 * @throws IOException
	 */
	public File download(String dir) throws IOException{
		String dest = dir + "/" + name +".dat";
		final File file = new File(dest);
		new Thread(new Runnable() {	
			@Override
			public void run(){
				try {
					FileOutputStream out = new FileOutputStream(file);
					InputStream stream = getInputStream();
					Util.copyStream(stream, out, 1024);
					stream.close();
					close();
				}catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}).start();
		return file;
	}

	/**
	 * 
	 * @param input
	 */
	public void upload(final InputStream input) throws IOException{
		new Thread(new Runnable() {	
			@Override
			public void run(){
				try {
					OutputStream stream = getOutputStream();
					Util.copyStream(input, stream);
					stream.close();
					close();
				}catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}).start();
	}

	protected InputStream getInputStream(){return null;}

	protected OutputStream getOutputStream(){return null;}

	protected void close(){}

	@Override
	public void parse(Node node) {}

	@Override
	public void store(Node parent) {}

}
