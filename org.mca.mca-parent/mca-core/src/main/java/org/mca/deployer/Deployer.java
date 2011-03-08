package org.mca.deployer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Deployer {

	public static void main(String[] args) {

		String casesBase = "/Users/Cyril/MCA/cases/";
		String file = "runge.car";
		String caseBase = extractCar(casesBase, file);
		File dir = new File(caseBase + "/data");
		dir.mkdir();
	}

	/**
	 * 
	 * @param casesBase
	 * @param file
	 */
	private static String extractCar(String casesBase, String file) {
		int index = file.lastIndexOf(".");
		String caseName = file.substring(0, index);

		File caseBase = new File(casesBase + caseName);
		if(caseBase.exists())
			caseBase.delete();
		caseBase.mkdir();
		File car = new File(casesBase, file);
		try {
			JarFile jarFile = new JarFile(car);
			Enumeration<JarEntry> jarEntries = jarFile.entries();
			InputStream input = null;
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
				String name = jarEntry.getName();
				System.out.println(name);
				int last = name.lastIndexOf('/');
				if (last >= 0) {
					File parent = new File(caseBase,
							name.substring(0, last));
					parent.mkdirs();
				}
				if (name.endsWith("/")) {
					continue;
				}
				input = jarFile.getInputStream(jarEntry);

				File expandedFile = expand(input, caseBase, name);
				long lastModified = jarEntry.getTime();
				if ((lastModified != -1) && (lastModified != 0) && (expandedFile != null)) {
					expandedFile.setLastModified(lastModified);
				}

				input.close();
				input = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return caseBase.getAbsolutePath();
	}

	/**
	 * 
	 * @param input
	 * @param docBase
	 * @param name
	 * @return
	 * @throws IOException
	 */
	protected static File expand(InputStream input, File docBase, String name)
	throws IOException {

		File file = new File(docBase, name);
		BufferedOutputStream output = null;
		try {
			output = 
				new BufferedOutputStream(new FileOutputStream(file));
			byte buffer[] = new byte[2048];
			while (true) {
				int n = input.read(buffer);
				if (n <= 0)
					break;
				output.write(buffer, 0, n);
			}
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}

		return file;
	}
}
