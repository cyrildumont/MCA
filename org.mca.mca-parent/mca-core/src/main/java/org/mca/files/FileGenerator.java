/**
 * 
 */
package org.mca.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Cyril
 *
 */
public abstract class FileGenerator extends Thread {
	
	public String srcFile;
	
	public String dirDest;
	
	protected Map<String, String> properties;
	
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public void setSrcFile(String srcFile) {
		this.srcFile = srcFile;
	}

	public void setDirDest(String dirDest) {
		this.dirDest = dirDest;
	}

	abstract public void generate() throws FileGeneratorException; 
	
	@Override
	public synchronized void start() {
		try {
			generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param dir
	 * @param prefix
	 * @param ext
	 * @param nb
	 * @return
	 */
	protected List<File> generateFiles(String dir, String prefix, String ext, int nb){
		List<File> files = new ArrayList<File>();
		File f;
		for (int i = 1; i <= nb; i++) {
			f = new File(dir + "/" + prefix + i + "." + ext);
			files.add(f);
		}
		return files;
		
	}
	
}
