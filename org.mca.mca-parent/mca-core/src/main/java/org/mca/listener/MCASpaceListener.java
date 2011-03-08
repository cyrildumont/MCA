package org.mca.listener;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.space.AvailabilityEvent;

import org.mca.entry.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.javaspace.exceptions.NoJavaSpaceFoundException;
import org.mca.log.LogUtil;

/**
 * 
 * @author cyril
 *
 */
public class MCASpaceListener implements RemoteEventListener {

	private MCASpace space;
	
	private Map<String, ComputationCase> cases;
	
	public MCASpaceListener(String host) throws NoJavaSpaceFoundException {
		space = new MCASpace(host);
		cases = new HashMap<String, ComputationCase>();
	}
	
	public void start() throws MCASpaceException{
		Collection<ComputationCase> cases = space.getCases();
		for (ComputationCase computationCase : cases) {
			addCase(computationCase);
		}
		space.registerForCases(this);
	}
	
	/**
	 * 
	 * @param computationCase
	 */
	private void addCase(ComputationCase computationCase) {
		this.cases.put(computationCase.name, computationCase);
		new ComputationCaseListener(computationCase);
		LogUtil.debug("[" + computationCase.name + "] added",getClass());
	}

	@Override
	public void notify(RemoteEvent event) throws UnknownEventException,
			RemoteException {
		AvailabilityEvent availabilityEvent = (AvailabilityEvent)event;
		try {
			ComputationCase cc = (ComputationCase)availabilityEvent.getEntry();
			addCase(cc);
		} catch (UnusableEntryException e) {
			e.printStackTrace();
		}
		

	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MCASpaceListener listener;
		try {
			listener = new MCASpaceListener("localhost");
			listener.start();
		} catch (NoJavaSpaceFoundException e) {
			e.printStackTrace();
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
		
	}
}
