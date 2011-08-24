
package org.mca.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import sun.misc.Launcher;

/**
 * 
 * @author Cyril
 */
@ManagedResource(objectName = "MCA:type=StandardClassLoader")
public class StandardClassLoader
extends URLClassLoader implements StandardClassLoaderMBean
{

	public StandardClassLoader(URL repositories[]) {
		super(repositories);
	}

	public StandardClassLoader(URL repositories[], ClassLoader parent) {
		super(repositories, parent);
	}

	@ManagedAttribute
	public String[] getClassLoader() {
		URL[] urls = getURLs();
		String[] sUrls = new String[urls.length];
		int i=0;
		for (URL url : urls) {
			sUrls[i++] = url.getPath();
		}
		return sUrls;
	}

	public void addURL(String dir) {
		try {
			File directory = new File(dir);
			String[] filenames = directory.list();
			for (int j = 0; j < filenames.length; j++) {
				String filename = filenames[j].toLowerCase();
				if (!filename.endsWith(".jar"))
					continue;
				File file = new File(directory, filenames[j]);
				file = new File(file.getCanonicalPath());
				if (!file.exists() || !file.canRead()){
					continue;
				}
				URL url = file.toURL();
				super.addURL(url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

