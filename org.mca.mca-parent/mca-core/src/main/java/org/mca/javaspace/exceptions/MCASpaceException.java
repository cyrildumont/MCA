/**
 * 
 */
package org.mca.javaspace.exceptions;

import java.rmi.RemoteException;

/**
 * @author Cyril
 *
 */
public class MCASpaceException extends RemoteException {

	public MCASpaceException() {
		super();
	}
	
	public MCASpaceException(String message) {
		super(message);
	}
}
