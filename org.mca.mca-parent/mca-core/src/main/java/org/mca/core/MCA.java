package org.mca.core;

import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class MCA {

	/**
	 * 
	 */
	public void start(final String configFile) {
		try {
			LoginContext loginContext = new LoginContext("org.mca.security.MCA");
			loginContext.login();
			Subject.doAsPrivileged(
					loginContext.getSubject(),
					new PrivilegedExceptionAction<Object>(){
						public Object run() throws Exception {
							startComponent(configFile);
							return null;
						}
					},
					null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	/**
	 * @param configFile
	 * @throws Exception
	 */
	private void startComponent(final String configFile) throws Exception {
		ApplicationContext context = new FileSystemXmlApplicationContext("file:" + configFile);
		MCAComponent component = (MCAComponent)context.getBean("component");
		component.init(context);
		component.start();
	}
}
