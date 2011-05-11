package org.mca.auth;

import java.security.Principal;

public class MCAPrincipal implements Principal{

	private String name;

	public MCAPrincipal(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MCAPrincipal)) {
			return false;
		}
		
		return ((MCAPrincipal) o).getName().equals(name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	

}
