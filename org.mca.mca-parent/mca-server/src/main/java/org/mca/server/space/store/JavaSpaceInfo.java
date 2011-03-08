package org.mca.server.space.store;

import net.jini.id.Uuid;

public class JavaSpaceInfo {
	
	private long sessionID;
	
	private Uuid uuid;
	
	private JoinState joinState;
	
	private EntryInfo[] entryInfos;
	

	public EntryInfo[] getEntryInfos() {
		return entryInfos;
	}

	public void setEntryInfos(EntryInfo[] entryInfos) {
		this.entryInfos = entryInfos;
	}

	public Uuid getUuid() {
		return uuid;
	}

	public void setUuid(Uuid uuid) {
		this.uuid = uuid;
	}

	public long getSessionID() {
		return sessionID;
	}

	public void setSessionID(long sessionID) {
		this.sessionID = sessionID;
	}

	public JoinState getJoinState() {
		return joinState;
	}

	public void setJoinState(JoinState joinState) {
		this.joinState = joinState;
	}
	

}
