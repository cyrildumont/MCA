/**
 * 
 */
package org.mca.javaspace;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collection;

import net.jini.core.entry.Entry;
import net.jini.core.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mca.entry.Barrier;
import org.mca.entry.ComputationCase;
import org.mca.entry.ComputationCaseState;
import org.mca.entry.DataHandler;
import org.mca.entry.MCAProperty;
import org.mca.javaspace.exceptions.CaseNotFoundException;
import org.mca.javaspace.exceptions.EntryNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.javaspace.exceptions.NoDuplicatedTaskException;
import org.mca.javaspace.exceptions.NoJavaSpaceFoundException;
import org.mca.log.LogUtil;
import org.mca.math.Data;
import org.mca.scheduler.Task;
import org.mca.transaction.TransactionManager;

/**
 * @author Cyril
 *
 */
public class MCASpace extends JavaSpaceParticipant{

	/** Log */
	private final static Log LOG = LogFactory.getLog(MCASpace.class);

	public MCASpace(String host) throws NoJavaSpaceFoundException {
		this.host = host;

		connectToSpace(host);
	}
	
	/**
	 * 
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}
	
	/**
	 * 
	 * @param name
	 * @param description
	 * @return
	 * @throws MCASpaceException
	 */
	
	public ComputationCase addCase(String name, String description) throws MCASpaceException {
		ComputationCase computationCase = new ComputationCase();
		computationCase.setName(name);
		computationCase.setDescription(description);
		return addCase(computationCase);
		
	}
	
	/**
	 * 
	 * @param computationCase
	 * @return
	 * @throws MCASpaceException
	 */
	public ComputationCase addCase(ComputationCase computationCase) throws MCASpaceException {
		LogUtil.debug("add case [" + computationCase + "]  on the MCASpace",getClass());
		try {
			Transaction t = TransactionManager.create(host);
			computationCase.host = host;
			computationCase.transaction = t;
			computationCase.setState(ComputationCaseState.STARTED);
			writeEntry(computationCase, null);
			return computationCase;
		}catch (Exception e) {
			if(e.getCause() instanceof NoDuplicatedTaskException){
				LOG.error(computationCase.name + " is already in the space");
			}else{
				e.printStackTrace();
			}
			throw new MCASpaceException();
		}
	}
	
		
	/**
	 * 
	 * @return
	 * @throws MCASpaceException 
	 */
	public ComputationCase getCase(String name) throws MCASpaceException, CaseNotFoundException{
		ComputationCase caseTemplate = new ComputationCase();
		caseTemplate.name = name;
		Entry result;
		try {
			result = readEntry(caseTemplate, null);
			return (ComputationCase)result;
		} catch (EntryNotFoundException e) {
			throw new CaseNotFoundException();
		}
		
	}

	/**
	 * 
	 * @return
	 * @throws MCASpaceException 
	 * @throws CaseNotFoundException 
	 */
	public void removeCase(String name) 
			throws MCASpaceException, CaseNotFoundException{
		ComputationCase caseTemplate = new ComputationCase();
		caseTemplate.name = name;
		try {
			caseTemplate = (ComputationCase)takeEntry(caseTemplate, null);
			Transaction transaction = caseTemplate.transaction;
			Collection<Entry> templates = new ArrayList<Entry>();
			templates.add(new Task());
			templates.add(new MCAProperty());
			templates.add(new Barrier());
			templates.add(new DataHandler());
			templates.add(new Data());
			Collection<Entry> results = takeEntry(templates, transaction);
			LogUtil.debug("[" + host + "] " + results.size() + " entries removed.", getClass());
		} catch (EntryNotFoundException e) {
			throw new CaseNotFoundException();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<ComputationCase> getCases(){
		Collection<ComputationCase> result = new ArrayList<ComputationCase>();
		Collection<Entry> templates = new ArrayList<Entry>();
		templates.add(new ComputationCase());
		try {
			Collection<Entry> temp = readEntry(templates, null);
			for (Entry entry : temp) {
				result.add((ComputationCase)entry);
			}
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
		return result;
	}

	
	/**
	 * 
	 * @param remote
	 * @throws MCASpaceException
	 */
	public void registerForCases(Remote remote) throws MCASpaceException{
		ComputationCase template = new ComputationCase();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		entries.add(template);
		registerForAvailabilityEvent(entries, null, false, remote, Long.MAX_VALUE, null);
	}
	
}

