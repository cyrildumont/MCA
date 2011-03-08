package org.mca.server.space.store;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.StringTokenizer;

import net.jini.id.Uuid;

import org.apache.commons.io.FilenameUtils;
import org.mca.service.BuilderException;

import com.sun.jini.outrigger.Recover;

public class JavaSpaceRegenerator {

	private String path;

	private JavaSpaceInfo info;

	/**
	 * 
	 * @param path
	 */
	public JavaSpaceRegenerator(String path) {
		this.path = path;
		File previousLog = getPreviousLog();
		if (previousLog != null) {
			JavaSpaceInfoBuilder builder = new JavaSpaceInfoBuilder();
			try {
				info = builder.parse(previousLog);
			} catch (BuilderException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * @param space
	 * @param MCASpace
	 * @return
	 */
	public Uuid regenerate(Recover space, MCASpaceOps MCASpace){
		if (info != null) {
			space.recoverSessionId(info.getSessionID());
			Uuid uuid = info.getUuid();
			space.recoverUuid(uuid);
			MCASpace.setUuid(uuid);

			try {
				space.recoverJoinState(info.getJoinState());
				EntryInfo[] entries = info.getEntryInfos();
				if (entries != null) {
					for (EntryInfo entry : entries) {
						space.recoverWrite(entry, null);
						MCASpace.writeOp(entry, null);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return uuid;
		}else{
			return null;
		}
	}

	/**
	 * 
	 *
	 */
	private File getPreviousLog(){
		File previousFile = null;
		File directory = new File(path);
		File[] files = directory.listFiles(new FileFilter(){
			public boolean accept(File pathname) {
				if (pathname.isFile() && pathname.getName().startsWith("javaspace_")) {
					return true;
				}
				return false;
			}
		}
		);
		Date date = new Date(0);

		for (File file : files) {
			Date dateFile = getDateFile(file);
			if (date.before(dateFile)) {
				previousFile = file;
				date = dateFile;
			}
		}
		return previousFile;
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	private Date getDateFile(File file) {
		String filename = file.getName();
		String basename = FilenameUtils.getBaseName(filename);
		StringTokenizer token = new StringTokenizer(basename,"_");
		token.nextToken();
		String sDate = token.nextToken();

		Date dateFile = new Date(Long.valueOf(sDate));
		return dateFile;
	}

}
