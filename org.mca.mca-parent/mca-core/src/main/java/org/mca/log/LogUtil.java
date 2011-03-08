package org.mca.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class LogUtil {

	public static void debug(Object o, Class cl){
		Log log = getLog(cl);
		if (log.isDebugEnabled()) {
			log.debug(o);
		}
	}
	
	public static void info(Object o, Class cl){
		Log log = getLog(cl);
		if (log.isInfoEnabled()) {
			log.info(o);
		}
	}
	
	public static void error(Object o, Class cl){
		Log log = getLog(cl);
		if (log.isErrorEnabled()) {
			log.error(o);
		}
	}
	
	
	private static Log getLog(Class cl){
		return LogFactory.getLog(cl);
	}
	
	
}
