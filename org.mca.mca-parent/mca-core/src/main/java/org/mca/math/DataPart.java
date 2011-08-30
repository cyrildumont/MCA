package org.mca.math;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataPart<E> extends Remote {

	public Object getValues() throws RemoteException;
}
