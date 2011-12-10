package org.mca.data;

import java.util.logging.Logger;

import net.jini.space.JavaSpace05;

import org.mca.ft.Checkpoint;
import org.mca.javaspace.JavaSpaceParticipant;

/**
 * 
 * @author cyril
 *
 */
public abstract class DataPartRemote extends JavaSpaceParticipant implements DataPart {

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_NAME = "org.mca.data.DataPartRemote";

	protected static final Logger logger = Logger.getLogger(COMPONENT_NAME);

	private DDataStructure parent;
	
	protected int part;
	
	public DataPartRemote(int part, JavaSpace05 javaspace) throws Exception{
		this.part = part;
		setSpace(javaspace);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	protected Object recv(String name) {
		while (space == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Checkpoint checkpoint = parent.getLastCheckpoint(); 
		Integer checkpointId = checkpoint != null ? checkpoint.getId() : null;
		DData template = new DData(name,checkpointId);
		DData data = null;
		try {		
			data = readEntry(template, null, Long.MAX_VALUE);
			return data.value;
		} catch (Exception e) {
			logger.throwing(getClass().getName(), "recv", e);
			return null;
		}
	}
	
	@Override
	public void setParent(DDataStructure parent) {
		this.parent = parent;
	}

}
