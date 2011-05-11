package org.mca.listener;

public class RegistrarEvent<T> {

	private RegistrarEventType type;
	
	private T service;
	
	public RegistrarEvent(RegistrarEventType type, T service) {
		this.type = type;
		this.service = service;
	}

	public T getService() {
		return service;
	}
	
	public RegistrarEventType getType() {
		return type;
	}
}
