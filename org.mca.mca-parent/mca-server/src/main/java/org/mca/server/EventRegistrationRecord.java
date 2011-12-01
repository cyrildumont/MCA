package org.mca.server;

import net.jini.id.Uuid;

import com.sun.jini.landlord.LeasedResource;

public class EventRegistrationRecord implements LeasedResource {

	private long expiration;
	
	private Uuid cookie;
	
	public EventRegistrationRecord(Uuid cookie){
		this.cookie = cookie;
	}
	
	@Override
	public Uuid getCookie() {
		return cookie;
	}

	@Override
	public long getExpiration() {	
		return expiration;
	}

	@Override
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

}
