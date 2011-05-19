package org.mca.server;

import javax.security.auth.Subject;

import com.sun.jini.discovery.ClientSubjectChecker;

public class MCAClientSubjectChecker implements ClientSubjectChecker {

	@Override
	public void checkClientSubject(Subject subject) {
		System.out.println(subject);
	}

}
