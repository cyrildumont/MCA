package org.mca.data;

import java.util.logging.Logger;

import org.mca.ft.Checkpoint;
import org.mca.ft.FTManager;

public abstract class DataPartLocal implements DataPart {

	private static final String COMPONENT_NAME = "org.mca.data.DataPartLocal";

	protected static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	private FTManager ftManager;

	private DDataStructure parent;
	
	public DataPartLocal() throws Exception {
		ftManager = FTManager.getInstance();
	}
	
	protected void sendData(String name, Object value) throws Exception{
		Checkpoint checkpoint = parent.getLastCheckpoint(); 
		Integer checkpointId = checkpoint != null ? checkpoint.getId() : null;
		ftManager.saveData(name, value, checkpointId);
		logger.fine("DataPartLocal - [" + name + "] saved");
	}
	
	public abstract DataPartInfo getInfos();
	
	public abstract Object getValues();
	
	@Override
	public void setParent(DDataStructure parent) {
		this.parent = parent;
	}
}
