package org.mca.computation;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.computation.exception.NoProjectFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ComputationCaseFactory {

	private String projectName;
	private String mcaHome;
	/** Log */
	private final static Log LOG = LogFactory.getLog(ComputationCaseFactory.class);

	public ComputationCase getComputationCase(String projectName) throws NoProjectFoundException{

		this.projectName = projectName;
		this.mcaHome = System.getProperty("mca.home");
		if (mcaHome == "") {
			LOG.error("you must define a MCA HOME");
		}
		String configFile = getConfigFile();
		ApplicationContext context = new  FileSystemXmlApplicationContext("file:" + configFile);
		ComputationCase computationCase = (ComputationCase)context.getBean("computationCase");
		computationCase.setProjectName(projectName);
		return computationCase;
	}

	/**
	 * 
	 * @param projectName
	 * @return
	 */
	private String getConfigFile() throws NoProjectFoundException{
		String configFile = mcaHome +"/cases/" + this.projectName +"/conf/mca.xml";
		File file = new File(configFile);
		if (!file.exists()) {
			LOG.error("configFile [" + configFile + "] not exists");
			throw new NoProjectFoundException();
		}
		return configFile;
	}
}
