/**
 * 
 */
package org.mca.startup;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.mca.loader.StandardClassLoader;


/**
 * @author Cyril
 *
 */
public class ClassLoaderFactory {

	public static ClassLoader createClassLoader(String... locations){

		try {
			URL[] repositories = generateUrls(locations);
			StandardClassLoader classloader = new StandardClassLoader(repositories);
			return classloader;
		} catch (IOException e) {
			return null;	
		}

	}


	private static URL[] generateUrls(String... locations) throws IOException {
		ArrayList<URL> list = new ArrayList<URL>();
		for (String location : locations) {
			list.addAll(getURLs(location));
		}

		URL[] repositories = (URL[]) list.toArray(new URL[list.size()]);
		return repositories;

	}


	private static ArrayList<URL> getURLs(String location) throws IOException,
	MalformedURLException {
		ArrayList<URL> list = new ArrayList<URL>();
		File directory = new File(location);
		URL dir = directory.toURL();
		list.add(dir);
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
			list.add(url);
		}
		return list;
	}


	public static ClassLoader createClassLoader(ClassLoader parent,
			String... locations) {

		try {
			URL[] repositories = generateUrls(locations);
			StandardClassLoader classloader = new StandardClassLoader(repositories,parent);
			return classloader;
		} catch (IOException e) {
			return null;	
		}
	}

}
